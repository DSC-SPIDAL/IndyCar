package com.dsc.iu.stream.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

import com.dsc.iu.utils.OnlineLearningUtils;

public class TestSpout extends BaseRichSpout implements MqttCallback {

	/**
	 * 
	 */
	private final long serialVersionUID = 1L;
	private String topic;
	private ConcurrentLinkedQueue<String> nonblockingqueue;
	private SpoutOutputCollector collector;
	private String data;
//	private PrintWriter pw;

	//topic name: #car-number#
	public TestSpout(String topic) {
		this.topic = topic;
	}

	@Override
	public void nextTuple() {
		if(nonblockingqueue.size()>0) {
			data = nonblockingqueue.poll();
			
			if(data.split(",").length == 6 && data.split(",")[5].split(" ").length == 2) {
				collector.emit(new Values(topic,data.split(",")[0],data.split(",")[1],data.split(",")[2], data.split(",")[3], data.split(",")[4], 
							data.split(",")[5].split(" ")[1]));
				
//				pw.println(topic + "," + data.split(",")[3] + "," + System.currentTimeMillis());
//				
//				if(Integer.parseInt(data.split(",")[3]) % 500 == 0) {
//					pw.flush();
//				}
			}
		}
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		
//		File spoutfile = new File("/scratch/sahil/spouts/spout-"+topic+".txt");
//		System.out.println("##########&&&&&& going to subscribe to topic:" + topic);
//		try {
//			pw = new PrintWriter(spoutfile);
//		} catch(FileNotFoundException f) {
//			f.printStackTrace();
//		}
		
		nonblockingqueue = new ConcurrentLinkedQueue<String>();
		collector = arg2;
		
		MqttConnectOptions conn = new MqttConnectOptions();
		//setting maximum # ofinflight messages
		conn.setMaxInflight(OnlineLearningUtils.inflightMsgRate);
		
		conn.setAutomaticReconnect(true);
		conn.setCleanSession(true);
		conn.setConnectionTimeout(30);
		conn.setKeepAliveInterval(30);
		conn.setUserName(OnlineLearningUtils.mqttadmin);
		conn.setPassword(OnlineLearningUtils.mqttpwd.toCharArray());
		
		try {
			MqttClient mqttClient = new MqttClient(OnlineLearningUtils.brokerurl, MqttClient.generateClientId());
			mqttClient.setCallback(this);
			mqttClient.connect(conn);
			mqttClient.subscribe(topic, OnlineLearningUtils.QoS);
		} catch(MqttException m) {m.printStackTrace();}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("carnum","speed","RPM","throttle","counter","lapDistance","timeOfDay"));
	}

	@Override
	public void connectionLost(Throwable arg0) {
		arg0.printStackTrace();
		//System.out.println("lost connection for data:" + data);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		nonblockingqueue.add(new String(arg1.getPayload()));
	}

}