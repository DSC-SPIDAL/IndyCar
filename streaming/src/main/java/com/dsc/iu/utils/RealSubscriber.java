package com.dsc.iu.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RealSubscriber implements MqttCallback {
	static PrintWriter pw;
	
	public static void main(String[] args) throws IOException {
		File f = new File("/scratch/sahil/recout/streamingtopic.csv");
		pw = new PrintWriter(f);
		RealSubscriber rs = new RealSubscriber();
		MqttConnectOptions conn = rs.getconnectObject();
		rs.subscribe(conn);
		
	}
	
	private MqttConnectOptions getconnectObject() {
		MqttConnectOptions conn = new MqttConnectOptions();
		conn.setMaxInflight(OnlineLearningUtils.inflightMsgRate);
		conn.setAutomaticReconnect(true);
		conn.setCleanSession(true);
		conn.setConnectionTimeout(30);
		conn.setKeepAliveInterval(30);
		conn.setUserName(OnlineLearningUtils.mqttadmin);
		conn.setPassword(OnlineLearningUtils.mqttpwd.toCharArray());
		
		return conn;
	}
	
	private void subscribe(MqttConnectOptions conn) {
		try {
			MqttClient mqttClient = new MqttClient(OnlineLearningUtils.brokerurl, MqttClient.generateClientId());
			mqttClient.setCallback(this);
			mqttClient.connect(conn);
			mqttClient.subscribe(OnlineLearningUtils.sinkoutTopic, OnlineLearningUtils.QoS);
		} catch(MqttException m) {
			m.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		pw.println(new String(message.getPayload()) + "," + System.currentTimeMillis());
		pw.flush();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}
}
