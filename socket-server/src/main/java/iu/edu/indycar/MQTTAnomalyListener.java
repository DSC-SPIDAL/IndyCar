package iu.edu.indycar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;

public class MQTTAnomalyListener {

  private final static Logger LOG = LogManager.getLogger(MQTTAnomalyListener.class);

  private static final String CONNECTION_URL = "tcp://j-093.juliet.futuresystems.org:61613";
  private static final String SUBSCRIPTION = "streaming_output";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "xyi5b2YUcw8CHhAE";

  private ServerBoot serverBoot;

  public MQTTAnomalyListener(ServerBoot serverBoot) {
    this.serverBoot = serverBoot;
  }

  public void start() throws MqttException {
    MqttClient client = new MqttClient(CONNECTION_URL, MqttClient.generateClientId());
    MqttConnectOptions connOpts = setUpConnectionOptions(USERNAME, PASSWORD);

    client.setCallback(new MqttCallback() {
      @Override
      public void connectionLost(Throwable throwable) {

      }

      @Override
      public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        String csv = new String(mqttMessage.getPayload());
        System.out.println(csv);
//        String[] split = csv.split(",");
//
//        AnomalyMessage anomalyMessage = new AnomalyMessage();
//        anomalyMessage.setIndex(0);
//        anomalyMessage.setAnomalyType(split[1]);
//        anomalyMessage.setCarNumber(Integer.valueOf(split[0]));
//        anomalyMessage.setRawData(Double.valueOf(split[2]));
//        anomalyMessage.setAnomaly(Double.valueOf(split[3]));
//
//        serverBoot.publishAnomalyEvent(anomalyMessage);
        //LOG.info("Message arrived {} : {}", s, new String(mqttMessage.getPayload()));
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

      }
    });
    client.connect(connOpts);

    client.subscribe(SUBSCRIPTION);

  }

  private static MqttConnectOptions setUpConnectionOptions(String username, String password) {
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);
    connOpts.setUserName(username);
    connOpts.setPassword(password.toCharArray());
    return connOpts;
  }
}
