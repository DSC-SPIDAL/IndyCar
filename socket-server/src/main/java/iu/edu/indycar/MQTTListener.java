package iu.edu.indycar;

import iu.edu.indycar.models.AnomalyMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;

public class MQTTListener {

  private final static Logger LOG = LogManager.getLogger(MQTTListener.class);

  private static final String CONNECTION_URL = "tcp://localhost:61613";
  private static final String SUBSCRIPTION = "streaming_output";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "password";

  private ServerBoot serverBoot;

  public MQTTListener(ServerBoot serverBoot) {
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
        String[] split = csv.split(",");

        AnomalyMessage anomalyMessage = new AnomalyMessage();
        anomalyMessage.setIndex(0);
        anomalyMessage.setAnomalyType(split[1]);
        anomalyMessage.setCarNumber(Integer.valueOf(split[0]));
        anomalyMessage.setRawData(Double.valueOf(split[2]));
        anomalyMessage.setAnomaly(Double.valueOf(split[3]));

        serverBoot.publishEvent(anomalyMessage);
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
