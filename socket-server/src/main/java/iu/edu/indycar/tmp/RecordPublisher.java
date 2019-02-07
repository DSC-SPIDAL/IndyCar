package iu.edu.indycar.tmp;

import iu.edu.indycar.ServerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;

public class RecordPublisher implements MqttCallback {

    private final Logger LOG = LogManager.getLogger(RecordPublisher.client);

    private static MqttClient client;

    public void connectToBroker() {
        MqttConnectOptions conn = new MqttConnectOptions();

        //changing # of inflight messages from default 10 to 500
        conn.setMaxInflight(500);

        conn.setAutomaticReconnect(true);
        conn.setCleanSession(true);
        conn.setConnectionTimeout(30);
        conn.setKeepAliveInterval(30);
        conn.setMaxInflight(1000);
        conn.setUserName(ServerConstants.USERNAME);
        conn.setPassword(ServerConstants.PASSWORD.toCharArray());

        try {
            client = new MqttClient(
                    ServerConstants.CONNECTION_URL,
                    MqttClient.generateClientId()
            );
            client.setCallback(this);
            client.connect(conn);
        } catch (MqttException m) {
            m.printStackTrace();
        }
    }

    public void publishRecord(String carNumber, String record) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(record.getBytes());
        mqttMessage.setQos(0);
        //LOG.info("Publishing {} to {}", record, carNumber);
        if (ServerConstants.DEBUG_MODE) {
            client.publish(ServerConstants.DEBUG_TOPIC, mqttMessage);
        } else {
            client.publish(carNumber, mqttMessage);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        LOG.error("lost connection to broker...", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }
}