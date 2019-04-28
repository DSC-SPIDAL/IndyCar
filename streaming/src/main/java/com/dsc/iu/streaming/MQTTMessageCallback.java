package com.dsc.iu.streaming;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MQTTMessageCallback {
    void onMessage(String topic, MqttMessage mqttMessage);
}
