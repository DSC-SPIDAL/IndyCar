package iu.edu.indycar;

import iu.edu.indycar.tmp.RecordPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Timer;
import java.util.TimerTask;

public class WebsocketServer {

    private final static Logger LOG = LogManager.getLogger(WebsocketServer.class);

    public static void startPositionStreamer(ServerBoot serverBoot, RecordPublisher recordPublisher, MQTTTelemetryListener mqttAnomalyListener, String path) {
        PositionStreamer positionStreamer = new PositionStreamer(serverBoot, recordPublisher, (tag) -> {
            LOG.info("Race ended.. Restarting...");
            serverBoot.reset();
            mqttAnomalyListener.reset();
            startPositionStreamer(serverBoot, recordPublisher, mqttAnomalyListener, path);
        });
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

        String filePath = args.length == 0 ? "/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log" : args[0];

        ServerBoot serverBoot = new ServerBoot("0.0.0.0", 5000);

        RecordPublisher recordPublisher = new RecordPublisher();
        recordPublisher.connectToBroker();

        MQTTTelemetryListener mqttAnomalyListener = new MQTTTelemetryListener(serverBoot);
        serverBoot.start();
        mqttAnomalyListener.start();
        startPositionStreamer(serverBoot, recordPublisher, mqttAnomalyListener, filePath);

//
//    TimerTask tt = new TimerTask() {
//      @Override
//      public void run() {
//        AnomalyMessage am = new AnomalyMessage();
//        am.setRawData(1);
//        am.setCarNumber(19);
//        am.setIndex(0);
//        am.setAnomalyType("SPEED");
//        serverBoot.publishAnomalyEvent(am);
//      }
//    };
//
//    new Timer().schedule(tt, 0, 1000);
    }
}
