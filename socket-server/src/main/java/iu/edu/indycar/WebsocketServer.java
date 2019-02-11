package iu.edu.indycar;

import iu.edu.indycar.tmp.LatencyCalculator;
import iu.edu.indycar.tmp.PingLatency;
import iu.edu.indycar.tmp.RecordPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WebsocketServer {

    private final static Logger LOG = LogManager.getLogger(WebsocketServer.class);

    public static void startPositionStreamer(
            ServerBoot serverBoot,
            RecordPublisher recordPublisher,
            MQTTTelemetryListener mqttAnomalyListener,
            String path) {
        PositionStreamer positionStreamer = new PositionStreamer(
                serverBoot,
                recordPublisher,
                (tag) -> {
                    LOG.info("Race ended.. Restarting...");
                    LatencyCalculator.clear();
                    serverBoot.reset();
                    mqttAnomalyListener.reset();
                    startPositionStreamer(serverBoot, recordPublisher, mqttAnomalyListener, path);
                }
        );
        positionStreamer.start(path);
        LOG.info("Reloading client browsers in 30 seconds...");
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                serverBoot.sendReloadEvent();
            }
        };
        new Timer().schedule(tt, 30000);
    }

    public static void main(String[] args) throws MqttException {

        String filePath = args.length == 0 ? ServerConstants.LOG_FILE : args[0];
        ServerConstants.DEBUG_CARS = args.length < 2 ? ServerConstants.DEBUG_CARS : Integer.valueOf(args[1]);

        ServerBoot serverBoot = new ServerBoot("0.0.0.0", 61521);

        RecordPublisher recordPublisher = new RecordPublisher();
        recordPublisher.connectToBroker();

        MQTTTelemetryListener mqttAnomalyListener = new MQTTTelemetryListener(
                serverBoot,
                ServerConstants.DEBUG_MODE ?
                        ServerConstants.DEBUG_TOPIC : ServerConstants.ANOMALY_TOPIC
        );
        serverBoot.start();
        mqttAnomalyListener.start();
        startPositionStreamer(serverBoot, recordPublisher, mqttAnomalyListener, filePath);

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
