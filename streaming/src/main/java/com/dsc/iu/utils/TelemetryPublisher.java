package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TelemetryPublisher {
	static MqttMessage mqttmsg;
	
	public static void main(String[] args) {
		try {
			MqttClient mqtt = new MqttClient("tcp://127.0.0.1:61613", MqttClient.generateClientId());
			
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			String line, data;
			while((line=rdr.readLine()) != null) {
				data = "5/28/17 " + line.split("�")[2] + "," + line.split("�")[line.split("�").length -3];
				byte[] arr = data.getBytes();
				mqttmsg = new MqttMessage();
				mqttmsg.setQos(2);
				mqttmsg.setPayload(arr);
				mqtt.publish("telemetry_data", mqttmsg);
			}
			
			rdr.close();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(MqttException m) {
			m.printStackTrace();
		}
	}
}
