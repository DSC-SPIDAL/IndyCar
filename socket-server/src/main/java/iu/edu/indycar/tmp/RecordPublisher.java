package iu.edu.indycar.tmp;

import iu.edu.indycar.ServerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;

public class RecordPublisher implements MqttCallback {

    private final Logger LOG = LogManager.getLogger(RecordPublisher.class);

    private MqttClient client;

    private StreamResetListener listener;

    public RecordPublisher(StreamResetListener listener) {
        this.listener = listener;
    }

    public void connectToBroker() {
        MqttConnectOptions conn = new MqttConnectOptions();

        conn.setAutomaticReconnect(true);
        conn.setCleanSession(true);
        conn.setConnectionTimeout(30);
        conn.setKeepAliveInterval(300);
        conn.setMaxInflight(1000);
        conn.setUserName(ServerConstants.USERNAME);
        conn.setPassword(ServerConstants.PASSWORD.toCharArray());
        conn.setSocketFactory(new MQTTSocketFactory());

        try {
            client = new MqttClient(
                    ServerConstants.CONNECTION_URL,
                    MqttClient.generateClientId()
            );
            client.setCallback(this);
            client.connect(conn);
            client.subscribe(ServerConstants.STATUS_TOPIC);
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

    public void sendRaceEnded() throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload("OK".getBytes());
        mqttMessage.setQos(2);
        client.publish(ServerConstants.STATUS_TOPIC, mqttMessage);
    }

    @Override
    public void connectionLost(Throwable cause) {
        LOG.error("lost connection to broker...", cause);
        try {
            client.reconnect();
        } catch (MqttException e) {
            LOG.error("Error occurred when trying to reconnect");
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // TODO Auto-generated method stub
        if (ServerConstants.STATUS_TOPIC.equals(topic)) {
            String msg = new String(message.getPayload());
            if ("START".equals(msg)) {
                if (listener != null) {
                    listener.reset();
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }
}