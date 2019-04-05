package com.dsc.iu.stream.app;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.dsc.iu.utils.OnlineLearningUtils;

public class TopologyRestarter implements MqttCallback {
	public static ConcurrentLinkedQueue<String> q;
	
	public MqttClient connectbroker() {
		MqttConnectOptions conn = new MqttConnectOptions();
		conn.setMaxInflight(OnlineLearningUtils.inflightMsgRate);
		conn.setAutomaticReconnect(true);
		conn.setCleanSession(true);
		conn.setConnectionTimeout(30);
		conn.setKeepAliveInterval(30);
		conn.setUserName(OnlineLearningUtils.mqttadmin);
		conn.setPassword(OnlineLearningUtils.mqttpwd.toCharArray());
		
		MqttClient client;
		try {
			client = new MqttClient(OnlineLearningUtils.brokerurl, MqttClient.generateClientId());
			client.setCallback(this);
			client.connect(conn);
			client.subscribe(OnlineLearningUtils.restart_topic, OnlineLearningUtils.QoS);
			return client;
		} catch(MqttException m) {
			m.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		q = new ConcurrentLinkedQueue<String>();
		
		MqttMessage msgobj = new MqttMessage();
		TopologyRestarter ob = new TopologyRestarter();
		MqttClient client = ob.connectbroker();
		
		while(true) {
			if(q.size() >0) {
				String msg = q.poll();
				if(msg.equalsIgnoreCase("OK")) {
					ProcessBuilder procbuildr = new 	ProcessBuilder("/scratch_ssd/sahil/apache-storm-1.0.4/bin/storm", "kill", "PRODUCTION-33-CARS", "-w", "0");
					Process p = procbuildr.start();
					Thread.sleep(20000);
					System.out.println("going to kill topology");
					//Indycar500-33-PRODUCTION-1.0-SNAPSHOT-jar-with-dependencies.jar
					procbuildr = new ProcessBuilder("/scratch_ssd/sahil/apache-storm-1.0.4/bin/storm", "jar", "/scratch_ssd/sahil/Indycar500-33-PRODUCTION-1.0-SNAPSHOT-jar-with-dependencies.jar", 
													"org.apache.storm.flux.Flux", "--remote", "/scratch_ssd/sahil/production-33-CARS.yaml");
					p = procbuildr.start();
					Thread.sleep(20000);
					System.out.println("started topology again");
					
					msgobj.setPayload("START".getBytes());
					msgobj.setQos(OnlineLearningUtils.QoS);
					try {
						client.publish(OnlineLearningUtils.restart_topic, msgobj);
						
					} catch(MqttException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		q.add(new String(message.getPayload()));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}
}
