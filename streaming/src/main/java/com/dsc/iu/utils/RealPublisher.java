package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RealPublisher {
	
	public static void main(String[] args) throws IOException {
		List<String> carlist = new LinkedList<String>();
    		
		carlist.add("20");
		
//		carlist.add("13");
//		carlist.add("19");
//		carlist.add("20");
//		carlist.add("21");
//		carlist.add("24");
//		carlist.add("26");
//		carlist.add("33");
//		carlist.add("98");
		
//		carlist.add("20");carlist.add("21");carlist.add("13");carlist.add("98");carlist.add("19");carlist.add("33");carlist.add("24");carlist.add("26");
//		carlist.add("7");carlist.add("6");carlist.add("60");carlist.add("27");carlist.add("22");carlist.add("18");carlist.add("3");carlist.add("4");
//		carlist.add("28");carlist.add("32");carlist.add("59");carlist.add("25");carlist.add("64");carlist.add("10");carlist.add("15");carlist.add("17");
//		carlist.add("12");carlist.add("1");carlist.add("9");carlist.add("14");carlist.add("23");carlist.add("30");carlist.add("29");carlist.add("88");
//		carlist.add("66");
		
		Iterator<String> itr = carlist.iterator();
		while(itr.hasNext()) {
			Thread obj = new Thread(new ParallelPublishing(itr.next()));
			obj.start();
		}
	}
}

class ParallelPublishing implements Runnable, MqttCallback {
	
	private String carnum;
	private MqttMessage msgobj = new MqttMessage();
	private MqttClient client;
	
	public ParallelPublishing(String carnum) {
		this.carnum = carnum;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			int counter=0;
//			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/scratch/sahil/recin/car-"+carnum+".csv"))));
			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/scratch_ssd/rbapat/car-"+carnum+".csv"))));
			MqttConnectOptions conn = new MqttConnectOptions();
			conn.setMaxInflight(OnlineLearningUtils.inflightMsgRate);
			conn.setAutomaticReconnect(true);
			conn.setCleanSession(true);
			conn.setConnectionTimeout(30);
			conn.setKeepAliveInterval(30);
			conn.setUserName(OnlineLearningUtils.mqttadmin);
			conn.setPassword(OnlineLearningUtils.mqttpwd.toCharArray());
			
			try {
				client = new MqttClient(OnlineLearningUtils.brokerurl, MqttClient.generateClientId());
				client.setCallback(this);
				client.connect(conn);
				System.out.println("established MQTT connection!");
			} catch(MqttException m) {
				m.printStackTrace();
			}
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//			File f = new File("/scratch/sahil/IPBroadcaster_Input_2018-05-27_0.log");
			File f = new File("/scratch_ssd/rbapat/IPBroadcaster_Input_2018-05-27_0.log");
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			System.out.println("going to start reading at:"+System.currentTimeMillis());
			String line;
			long previousrecordTs=Long.MIN_VALUE;
			long currentrecTs=Long.MIN_VALUE;
			String prevrecord="";
			while((line=rdr.readLine()) != null) {
				if(line.startsWith("$P") && line.split("�")[2].matches("\\d+:\\d+:\\d+.\\d+") && line.split("�")[1].equalsIgnoreCase(carnum)) {
					counter++;
					try {
						currentrecTs = df.parse("2018-05-27 " + line.split("�")[2]).getTime();
					} catch(ParseException p) {
						p.printStackTrace();
					}
					
					String payload = line.split("�")[4] + "," + line.split("�")[5] + "," + line.split("�")[6] + "," + counter 
									+ "," + line.split("�")[3] + "," + "2018-05-27 " + line.split("�")[2];
					
					if(currentrecTs > previousrecordTs) {
						wrtr.write(payload + "," + System.currentTimeMillis() + "\n");
						wrtr.flush();
						
						msgobj.setQos(OnlineLearningUtils.QoS);
						msgobj.setPayload(payload.getBytes());
						try {
							client.publish(carnum, msgobj);
						} catch(MqttException m) {
							m.printStackTrace();
						}
					}
				
					if(previousrecordTs != Long.MIN_VALUE) {
						try {
							if(previousrecordTs > currentrecTs) {
								System.out.println("previous record: " +prevrecord);
								System.out.println("current record: " +payload + " for car " + carnum);
								System.out.println(currentrecTs + "," + previousrecordTs);
							}
//							System.out.println("going to sleep for " + (currentrecTs - previousrecordTs) + " ms");
							
							if(currentrecTs > previousrecordTs) {
								prevrecord = payload;
								previousrecordTs = currentrecTs;
								Thread.sleep(currentrecTs - previousrecordTs);
							}
						} catch(InterruptedException in) {
							in.printStackTrace();
						}
					}
				}
			}
			
			rdr.close();
			System.out.println("count value for car " + carnum + " is:" + counter);
			System.out.println("completed reading file for car: " + carnum);
			System.out.println("timestamp logged when done reading above file:"+System.currentTimeMillis());
			wrtr.flush();
			wrtr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		
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