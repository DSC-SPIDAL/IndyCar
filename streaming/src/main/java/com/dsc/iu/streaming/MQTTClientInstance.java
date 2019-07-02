package com.dsc.iu.streaming;

import com.dsc.iu.utils.OnlineLearningUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MQTTClientInstance implements MqttCallback, Serializable {

    private final static Logger LOGGER = LogManager.getLogger(MQTTClientInstance.class);

    private static volatile MQTTClientInstance INSTANCE;

    private MqttClient mqttClient;

    private Map<String, List<MQTTMessageCallback>> callbacks;


    private MQTTClientInstance() {
        this.callbacks = new ConcurrentHashMap<>();

        MqttConnectOptions conn = new MqttConnectOptions();
        //setting maximum # of in-flight messages
        conn.setMaxInflight(OnlineLearningUtils.inflightMsgRate);

        conn.setAutomaticReconnect(true);
        conn.setCleanSession(true);
        conn.setConnectionTimeout(30);
        conn.setKeepAliveInterval(30);
        conn.setUserName(OnlineLearningUtils.mqttadmin);
        conn.setPassword(OnlineLearningUtils.mqttpwd.toCharArray());
        conn.setSocketFactory(new MQTTSocketFactory());

        try {
            //publish HTM data
            this.mqttClient = new MqttClient(OnlineLearningUtils.brokerurl,
                    MqttClient.generateClientId(), new MemoryPersistence());
            mqttClient.setCallback(this);
            mqttClient.connect(conn);
        } catch (MqttException m) {
            m.printStackTrace();
        }

    }

    public void subscribe(String topic, MQTTMessageCallback callback) throws MqttException {
        this.callbacks.computeIfAbsent(topic,
                t -> Collections.synchronizedList(new ArrayList<>())).add(callback);
        mqttClient.subscribe(topic, OnlineLearningUtils.QoS);
    }

    public synchronized static MQTTClientInstance getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MQTTClientInstance();
        }
        return INSTANCE;
    }


    @Override
    public void connectionLost(Throwable cause) {
        LOGGER.warn("Disconnected from broker. Trying to reconnect...", cause);
        if (!this.mqttClient.isConnected()) {
            try {
                this.mqttClient.reconnect();
            } catch (MqttException e) {
                LOGGER.error("Failed to reconnect to broker...", e);
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            if (!this.callbacks.containsKey(topic)) {
                LOGGER.warn("Dropping message due to absence of listeners for {}", topic);
                return;
            }
            for (MQTTMessageCallback mqttMessageCallback : this.callbacks.get(topic)) {
                mqttMessageCallback.onMessage(topic, message);
            }
        } catch (Exception ex) {
            LOGGER.warn("Error in distributing message", ex);
        }
    }

    public void sendMessage(String topic, MqttMessage message) throws MqttException {
        this.mqttClient.publish(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private Object readResolve() {
        return INSTANCE;
    }
}
