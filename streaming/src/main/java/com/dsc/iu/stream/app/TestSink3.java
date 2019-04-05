package com.dsc.iu.stream.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

public class TestSink3 extends BaseRichBolt implements MqttCallback {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MqttClient client;
	private MqttMessage msgobj;
	private ConcurrentHashMap<String, JSONObject> recordaccumulate;
	private JSONObject record;
//	private File f;
//	private PrintWriter pw;
	private String carnum;
	
	public TestSink3(String carnum) {
		this.carnum = carnum;
	}

	@Override
	public void execute(Tuple arg0) {
		//String carnum = arg0.getStringByField("carnum");
		String metric = arg0.getStringByField("metric");
		String data_val = arg0.getStringByField("dataval");
		double score = arg0.getDoubleByField("score");
		String counter = arg0.getStringByField("counter");
		long ts = arg0.getLongByField("spout_timestamp");
		String lapDistance = arg0.getStringByField("lapDistance");
		long bolt_ts= arg0.getLongByField("bolt_timestamp");
		
		if(!recordaccumulate.containsKey(carnum+"_"+counter)) {
			record = new JSONObject();
			record.put("carNumber", carnum);
			record.put("timeOfDay", ts);
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
		
		//CALCULATE INDIVIDUAL LATENCY NOT JUST AGGREGATED TOTAL LATENCY (FETCH TS OF EACH METRIC ARRIVAL AT SINK)
		if(record.containsKey("engineSpeed") && record.containsKey("vehicleSpeed") && record.containsKey("throttle")) {
			//System.out.println(record.toJSONString());
			msgobj.setPayload(record.toJSONString().getBytes());
			msgobj.setQos(OnlineLearningUtils.QoS);
			try {
				client.publish(OnlineLearningUtils.sinkoutTopic, msgobj);
				
			} catch(MqttException e) {
				e.printStackTrace();
			}
			
			//calculate the CPU and memory utilization caused by aggregator
			recordaccumulate.remove(carnum+"_"+counter);
		}
		
		long currtime = System.currentTimeMillis();
		long nanosec = System.nanoTime();
//		pw.write(carnum + "," + counter + "," + metric + "," + (currtime - ts) + "," + (currtime - bolt_ts) + "," + currtime + "," + bolt_ts + "," + ts + ","
//				+ (bolt_ts - ts) + "," + (System.nanoTime() - nanosec) + "\n");
//		if(Integer.parseInt(counter) % 500 == 0) {
//			pw.flush();
//		}
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		//f = new File("/scratch/sahil/sinks/sink-" + carnum + ".csv");
//		f = new File("/scratch_ssd/rbapat/sinks/sink-" + carnum + ".csv");
//		try {
//			pw = new PrintWriter(f);
//		} catch(FileNotFoundException e) {
//			e.printStackTrace();
//		}
		
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
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		//xyi5b2YUcw8CHhAE
		System.out.println("@@@@@@@@@@@@##################$$$$$$$$ connection to MQTT broker lost:" + cause.getMessage());
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