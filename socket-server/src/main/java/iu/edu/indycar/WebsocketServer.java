package iu.edu.indycar;

import iu.edu.indycar.streamer.StreamEndListener;
import iu.edu.indycar.tmp.LatencyCalculator;
import iu.edu.indycar.tmp.PingLatency;
import iu.edu.indycar.tmp.RecordPublisher;
import iu.edu.indycar.tmp.StreamResetListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WebsocketServer implements StreamResetListener, StreamEndListener {

    private final static Logger LOG = LogManager.getLogger(WebsocketServer.class);
    private String logFilePath;

    private ServerBoot serverBoot;
    private RecordPublisher recordPublisher;
    private MQTTTelemetryListener mqttTelemetryListener;
    private PositionStreamer positionStreamer;

    public WebsocketServer(String logFilePath) {
        this.logFilePath = logFilePath;
        this.serverBoot = new ServerBoot("0.0.0.0", ServerConstants.WS_PORT);
        this.recordPublisher = new RecordPublisher(this);
        this.mqttTelemetryListener = new MQTTTelemetryListener(
                serverBoot,
                ServerConstants.DEBUG_MODE ?
                        ServerConstants.DEBUG_TOPIC : ServerConstants.ANOMALY_TOPIC
        );

    }

    private void startNewStreamingSession() {
        this.positionStreamer = new PositionStreamer(
                serverBoot,
                recordPublisher,
                this
        );
        this.positionStreamer.start(this.logFilePath);
        LOG.info("Reloading client browsers in 30 seconds...");
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                serverBoot.sendReloadEvent();
            }
        };
        new Timer().schedule(tt, 30000);
    }

    public void start() throws MqttException {
        this.recordPublisher.connectToBroker();
        this.serverBoot.start();
        this.mqttTelemetryListener.start();
        this.startNewStreamingSession();

        //close in 10 minutes in debug mode
        if (ServerConstants.DEBUG_MODE) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        PingLatency.writeToFile();
                        LatencyCalculator.writeToFile();
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 10 * 60 * 1000);
        }
    }

    public static void main(String[] args) throws MqttException {
        String filePath = args.length == 0 ? ServerConstants.LOG_FILE : args[0];
        ServerConstants.DEBUG_CARS = args.length < 2 ? ServerConstants.DEBUG_CARS : Integer.valueOf(args[1]);

        WebsocketServer websocketServer = new WebsocketServer(filePath);
        websocketServer.start();
    }

    @Override
    public void reset() {
        LOG.info("Reset signal received from storm... Restarting stream...");
        this.startNewStreamingSession();
    }

    @Override
    public void onStreamEnd(String s) {
        LOG.info("Race ended.. Restarting...");
        LatencyCalculator.clear();
        this.serverBoot.reset();
        this.mqttTelemetryListener.reset();
        this.positionStreamer.stop();

        LOG.info("Sending race end signal to storm");
        try {
            this.recordPublisher.sendRaceEnded();
            LOG.info("Waiting for storm to restart....");
        } catch (MqttException e) {
            LOG.error("Error in sending race end signal to storm", e);
        }
    }
}
