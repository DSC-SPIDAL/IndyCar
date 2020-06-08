package iu.edu.indycar.mqtt;

import iu.edu.indycar.ServerConstants;
import iu.edu.indycar.TelemetryListener;
import iu.edu.indycar.tmp.StreamResetListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTClient implements MqttCallback {

    private final Logger LOG = LogManager.getLogger(MQTTClient.class);
    private TelemetryListener telemetryListener;

    private MqttClient client;

    private StreamResetListener streamResetListener;

    public MQTTClient(StreamResetListener streamResetListener) {
        this.streamResetListener = streamResetListener;
    }

    public void setTelemetryListener(TelemetryListener telemetryListener) {
        LOG.info("Setting telemetry listener....");
        this.telemetryListener = telemetryListener;
    }

    public void connectToBroker() {
        MqttConnectOptions conn = new MqttConnectOptions();

        conn.setAutomaticReconnect(true);
        conn.setCleanSession(true);
        conn.setConnectionTimeout(30);
        conn.setKeepAliveInterval(10);
        conn.setMaxInflight(1000);
        conn.setUserName(ServerConstants.USERNAME);
        conn.setPassword(ServerConstants.PASSWORD.toCharArray());
        conn.setSocketFactory(new MQTTSocketFactory());

        try {
            MqttClientPersistence clientPersistence = new MemoryPersistence();

            client = new MqttClient(
                    ServerConstants.CONNECTION_URL,
                    MqttClient.generateClientId(),
                    clientPersistence
            );

            client.setCallback(this);
            client.connect(conn);
            client.subscribe(ServerConstants.STATUS_TOPIC);
            client.subscribe(
                    ServerConstants.DEBUG_MODE ? ServerConstants.DEBUG_TOPIC : ServerConstants.ANOMALY_TOPIC
            );
        } catch (MqttException m) {
            LOG.error("Error in connecting to the broker", m);
        }
    }

    public void publishRecord(String carNumber, String record) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(record.getBytes());
        mqttMessage.setQos(0);

        if (ServerConstants.DEBUG_MODE) {
            client.publish(ServerConstants.DEBUG_TOPIC, mqttMessage);
        } else {
            client.publish(ServerConstants.PUBLISH_TOPIC_PREFIX+carNumber, mqttMessage);
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
            if (!client.isConnected()) {
                client.reconnect();
            }
        } catch (MqttException e) {
            LOG.error("Error occurred when trying to reconnect");
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if (ServerConstants.STATUS_TOPIC.equals(topic)) {
            String msg = new String(message.getPayload());
            LOG.info("Message received to status topic : {}", msg);
            if ("START".equals(msg)) {
                if (streamResetListener != null) {
                    streamResetListener.reset();
                }
            }
        } else if (this.telemetryListener != null && (ServerConstants.ANOMALY_TOPIC.equals(topic)
                || ServerConstants.DEBUG_TOPIC.equals(topic))) {
            this.telemetryListener.onTelemetryMessage(message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }
}
