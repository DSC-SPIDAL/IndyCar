package iu.edu.indycar;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class WebsocketServer {

    public static void main(String[] args) throws MqttException {


        ServerBoot serverBoot = new ServerBoot("localhost", 9092);
        MQTTAnomalyListener mqttAnomalyListener = new MQTTAnomalyListener(serverBoot);
        PositionStreamer positionStreamer = new PositionStreamer(serverBoot);

        serverBoot.start();
        //mqttAnomalyListener.start();
        positionStreamer.start();
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
