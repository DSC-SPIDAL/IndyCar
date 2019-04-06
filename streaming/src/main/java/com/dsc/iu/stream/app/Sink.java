package com.dsc.iu.stream.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;

import com.dsc.iu.utils.OnlineLearningUtils;

public class Sink extends BaseRichBolt implements MqttCallback {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PrintWriter pw;
	private MqttClient client;
	private MqttMessage msgobj;
	private ConcurrentHashMap<String, JSONObject> recordaccumulate;
	private JSONObject record;

	@Override
	public void execute(Tuple arg0) {
		String carnum = arg0.getStringByField("carnum");
		String metric = arg0.getStringByField("metric");
		String data_val = arg0.getStringByField("dataval");
		double score = arg0.getDoubleByField("score");
		String counter = arg0.getStringByField("counter");
		String timeOfDay = arg0.getStringByField("timeOfDay");
		String lapDistance = arg0.getStringByField("lapDistance");
		
		//System.out.println("******************** sink data: "+metric+","+carnum+","+data_val+","+score+","+ts);
		//System.out.println("******************** sink data,"+metric+"_"+counter+"_"+carnum+","+System.currentTimeMillis()+","+data_val+","+score);
		
		pw.println("******************** sink data,"+metric+"_"+counter+"_"+carnum+","+System.currentTimeMillis()+","+data_val+","+score);
		pw.flush();
		
		if(!recordaccumulate.containsKey(carnum+"_"+counter)) {
			record = new JSONObject();
			record.put("carNumber", carnum);
			record.put("timeOfDay", timeOfDay);
			record.put("lapDistance", lapDistance);
			record.put("UUID", carnum+"_"+counter);
			
		} else {
			record = recordaccumulate.get(carnum+"_"+counter);
		}
		
		if(metric.equalsIgnoreCase("RPM")) {
			metric = "engineSpeed";
		}
		
		if(metric.equalsIgnoreCase("speed")) {
			metric = "vehicleSpeed";
		}
		
		record.put(metric, data_val);
		record.put(metric+"Anomaly", score);
		recordaccumulate.put(carnum+"_"+counter, record);
		
		if(record.containsKey("engineSpeed") && record.containsKey("vehicleSpeed") && record.containsKey("throttle")) {
			//System.out.println("################ message payload:" + record.toJSONString());
			msgobj.setPayload(record.toJSONString().getBytes());
			msgobj.setQos(2);
			try {
				client.publish(OnlineLearningUtils.sinkoutTopic, msgobj);
				
			} catch(MqttException e) {
				e.printStackTrace();
			}
			
			//calculate the latency and memory overhead caused by accumulator
			recordaccumulate.remove(carnum+"_"+counter);
		}
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		
		msgobj = new MqttMessage();
		recordaccumulate = new ConcurrentHashMap<String, JSONObject>();
		MqttConnectOptions conn = new MqttConnectOptions();
		
		//changing # of inflight messages from default 10 to 500 
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
		} catch(MqttException m) {m.printStackTrace();}
		
		File sinkfile = new File("/scratch_ssd/sahil/sinkfile.txt");
		try {
			pw = new PrintWriter(sinkfile);
		} catch(FileNotFoundException f) {
			f.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("@@@@@@@@@@@@##################$$$$$$$$ connection lost");
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