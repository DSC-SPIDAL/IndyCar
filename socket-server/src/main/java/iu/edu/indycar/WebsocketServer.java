package iu.edu.indycar;

import iu.edu.indycar.mqtt.MQTTClient;
import iu.edu.indycar.prediction.NewPredictor;
import iu.edu.indycar.prediction.RankPrediction;
import iu.edu.indycar.streamer.StreamEndListener;
import iu.edu.indycar.tmp.*;
import iu.edu.indycar.ws.ServerBoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WebsocketServer implements StreamResetListener, StreamEndListener {

    private final static Logger LOG = LogManager.getLogger(WebsocketServer.class);
    private String logFilePath;

    private ServerBoot serverBoot;
    private MQTTClient mqttClient;
    private TelemetryListener telemetryListener;
    private PositionStreamer positionStreamer;
    private RankPrediction rankPrediction;

    private Timer timer = new Timer();

    private TimerTask stormBackup;

    public WebsocketServer(String logFilePath) {
        this.logFilePath = logFilePath;
        this.serverBoot = new ServerBoot("0.0.0.0", ServerConstants.WS_PORT);
        this.mqttClient = new MQTTClient(this);
        this.rankPrediction = new RankPrediction(this.serverBoot);
    }

    private void startNewStreamingSession() {
        this.telemetryListener = new TelemetryListener(
                serverBoot
        );
        this.mqttClient.setTelemetryListener(this.telemetryListener);

        this.positionStreamer = new PositionStreamer(
                serverBoot,
                mqttClient,
                this,
                this.rankPrediction
        );

        this.positionStreamer.start(this.logFilePath);
        this.telemetryListener.start();

        LOG.info("Reloading client browsers in 30 seconds...");
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                serverBoot.sendReloadEvent();
            }
        };
        this.timer.schedule(tt, 30000);

        //loading anomaly labels
        File anomalyLabelsFile = new File("anomaly_labels.csv");
        if (anomalyLabelsFile.exists()) {
            LOG.info("Anomaly labels found. Loading...");
            try {
                AnomalyLabelsBank.loadLabelsFromCSV("anomaly_labels.csv");
            } catch (IOException e) {
                LOG.warn("Error in loading anomaly labels", e);
            }
        }
    }

    public void start() {
        this.mqttClient.connectToBroker();
        this.serverBoot.start();

        this.startNewStreamingSession();

        //close in 10 minutes in debug mode
        if (ServerConstants.DEBUG_MODE) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        PingLatency.writeToFile();
                        //LatencyCalculator.writeToFile();
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 10 * 60 * 1000);
        }
    }

    public static void main(String[] args) {
        String filePath = args.length == 0 ? ServerConstants.LOG_FILE : args[0];
        ServerConstants.CONNECTION_URL = args.length < 2? ServerConstants.CONNECTION_URL : args[1];
        ServerConstants.NO_OF_STREAMING_CARS = args.length < 3 ? ServerConstants.NO_OF_STREAMING_CARS : Integer.valueOf(args[1]);

        WebsocketServer websocketServer = new WebsocketServer(filePath);
        websocketServer.start();
    }

    @Override
    public void reset() {
        LOG.info("Reset signal received from storm... Restarting stream...");
        System.exit(0);
//        this.startNewStreamingSession();
//        if (this.stormBackup != null) {
//            LOG.info("Cancelling storm backup task");
//            this.stormBackup.cancel();
//            this.stormBackup = null;
//        }
    }

    @Override
    public void onStreamEnd(String s) {
        LOG.info("Race ended.. Restarting...");

//        try {
//            LatencyCalculator.writeToFile();
//        } catch (IOException e) {
//            LOG.warn("Error in writing latency values to file", e);
//        }

        LatencyCalculator.clear();
        this.serverBoot.reset();

        this.positionStreamer.stop();
        this.telemetryListener.close();
        this.rankPrediction.clear();

        AnomalyLabelsBank.reset();

        LOG.info("Sending race end signal to storm");
        try {
            this.mqttClient.sendRaceEnded();
            LOG.info("Waiting for storm to restart....");
        } catch (MqttException e) {
            LOG.error("Error in sending race end signal to storm", e);
        }

        LOG.info("Registering storm backup to trigger in 5 minutes");
        this.stormBackup = new TimerTask() {
            @Override
            public void run() {
                LOG.info("Starting new backup session....");
                reset();
                //startNewStreamingSession();
            }
        };
        this.timer.schedule(this.stormBackup, 1000 * 60 * 5);
    }
}
