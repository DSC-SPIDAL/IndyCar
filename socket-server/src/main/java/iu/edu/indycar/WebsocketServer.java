package iu.edu.indycar;

import org.eclipse.paho.client.mqttv3.MqttException;

public class WebsocketServer {

  public static void main(String[] args) throws MqttException {


    ServerBoot serverBoot = new ServerBoot("localhost", 9092);

    MQTTListener mqttListener = new MQTTListener(serverBoot);
    serverBoot.start();
    mqttListener.start();
//
//    TimerTask tt = new TimerTask() {
//      @Override
//      public void run() {
//        AnomalyMessage am = new AnomalyMessage();
//        am.setRawData(1);
//        am.setCarNumber(19);
//        am.setIndex(0);
//        am.setAnomalyType("SPEED");
//        serverBoot.publishEvent(am);
//      }
//    };
//
//    new Timer().schedule(tt, 0, 1000);
  }
}
