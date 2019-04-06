package com.dsc.iu.stream.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class IndycarSpout extends BaseRichSpout implements MqttCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String topic;
	private ConcurrentLinkedQueue<String> nonblockingqueue;
	private SpoutOutputCollector collector;
	private String data;
	
	//topic name: #car-number#
	public IndycarSpout(String topic) {
		this.topic = topic;
	}

	@Override
	public void nextTuple() {
		if(nonblockingqueue.size()>0) {
			data = nonblockingqueue.poll();
			//telemetry_log_time,speed,RPM,throttle
//			System.out.println("@@@@@@@@@@@@@@@@@@@@indycarspout: " + data.split(",")[0] + ","+data.split(",")[1]+","+data.split(",")[2]+","+data.split(",")[3]);
//			collector.emit(new Values(data.split(",")[0],data.split(",")[1],data.split(",")[2],data.split(",")[3]));
			
			//speed,rpm,throttle only
			System.out.println("@@@@@@@@@@@@@@@@@@@@indycarspout," + topic + "," + data.split(",")[0] + ","+data.split(",")[1]+","+data.split(",")[2]);
			collector.emit(new Values(data.split(",")[0],data.split(",")[1],data.split(",")[2]));
			
			//setting an input rate of 100 msg/sec
			try {
				Thread.sleep(10);
			} catch(InterruptedException i) {
				i.printStackTrace();
			}
		}
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		nonblockingqueue = new ConcurrentLinkedQueue<String>();
		collector = arg2;
		
		MqttConnectOptions conn = new MqttConnectOptions();
		//setting maximum # ofinflight messages
		conn.setMaxInflight(500);
		
		conn.setAutomaticReconnect(true);
		conn.setCleanSession(true);
		conn.setConnectionTimeout(30);
		conn.setKeepAliveInterval(30);
		conn.setUserName("admin");
		conn.setPassword("password".toCharArray());
		
		try {
//			MqttClient mqttClient = new MqttClient("tcp://127.0.0.1:61613", MqttClient.generateClientId());
			MqttClient mqttClient = new MqttClient("tcp://10.16.0.73:61613", MqttClient.generateClientId());
			mqttClient.setCallback(this);
			mqttClient.connect(conn);
			mqttClient.subscribe(topic, 2);
		} catch(MqttException m) {m.printStackTrace();}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
//		arg0.declare(new Fields("telemetry_log_time","speed","RPM","throttle"));
		arg0.declare(new Fields("speed","RPM","throttle"));
	}

	@Override
	public void connectionLost(Throwable arg0) {
		arg0.printStackTrace();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		nonblockingqueue.add(new String(arg1.getPayload()));
	}

}
