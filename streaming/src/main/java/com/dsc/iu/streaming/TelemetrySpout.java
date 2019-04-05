package com.dsc.iu.streaming;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;

import com.dsc.iu.utils.OnlineLearningUtils;

import rx.Subscriber;

public class TelemetrySpout extends BaseRichSpout implements MqttCallback {

	/**
	 * 
	 */
	//sample record in eRPlog
	//$P¦22¦16:05:54.253¦0.00¦0.000¦0¦0¦0¦0¦0.04¦0¦0¦0¦0¦0¦0¦0¦0¦0.29¦0.02¦-1.11¦0.00¦¦0¦0¦0¦0¦¦0¦¦0¦0¦0¦7¦221.05¦39.7925912¦-86.2388936
	private static final long serialVersionUID = 1L;
	private String connectstring = "tcp://127.0.0.1:61613";
	private String dataTopic = "telemetry_data";
	private Network network;
	//non-blocking queue
	private ConcurrentLinkedQueue<String> nbqueue;
	private Publisher manualpublish;
	SpoutOutputCollector spoutcollector;

	public void nextTuple() {
		// TODO Auto-generated method stub
		if(nbqueue != null && nbqueue.size() >0) {
			manualpublish.onNext(nbqueue.poll());
			
		}
	}

	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		// TODO Auto-generated method stub
		try {
			
			MqttConnectOptions conn = connectToMqtt();
			MqttClient mqttClient = new MqttClient(connectstring, MqttClient.generateClientId());
			mqttClient.setCallback(this);
			mqttClient.connect(conn);
			mqttClient.subscribe(dataTopic, 2);
			
		} catch(MqttException e) {
			e.printStackTrace();
		}
		
		//in pub/sub broker mechanism, the queue is read off record wise in messageArrived(T..) method
		nbqueue = new ConcurrentLinkedQueue<String>();
		this.spoutcollector = arg2;
//		try {
			manualpublish = OnlineLearningUtils.getPublisher();
			Sensor<ObservableSensor<String[]>> sensor = Sensor.create(
													ObservableSensor::create, 
													SensorParams.create(Keys::obs, new Object[] { "kakkerot", manualpublish }));
			
//			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream
//					("/Users/sahiltyagi/Downloads/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
//
//			String line;
//			line = rdr.readLine(); line = rdr.readLine();			//skip line 1 and 2
//			while((line=rdr.readLine()) != null) {
//				//second condition to remove malformed time values in eRP log (or maybe these records hold different context! --to be verified)
//				//replace split str by \u00A6 when running on cluster
//				if(line.startsWith("$P") && line.split("�")[2].length() >9) {
//					
//					nbqueue.add("5/28/17 " + line.split("�")[2] + "," + line.split("�")[line.split("�").length -3]);
//				}
//			}
			
			
			
			Parameters p = OnlineLearningUtils.getLearningParameters();
			p = p.union(OnlineLearningUtils.getNetworkLearningEncoderParams());
			network =  Network.create("Network API Demo", p)
					.add(Network.createRegion("Region 1")
					.add(Network.createLayer("Layer 2/3", p)
					.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
					.add(Anomaly.create())
					.add(new TemporalMemory())
					.add(new SpatialPooler())
					.add(sensor)));
			
//			rdr.close();
			network.start();
//		} catch(IOException e) {
//			e.printStackTrace();
//		} 
		
	}

	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		//no connecting bolt as of yet
		
	}

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("MQTT_LOST_CONNECTION telemetry spout ts:"+ System.currentTimeMillis());
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		
		nbqueue.add(new String(message.getPayload()));
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}
	
	public static MqttConnectOptions connectToMqtt() {
		
		MqttConnectOptions conn = new MqttConnectOptions();
		conn.setAutomaticReconnect(true);
		conn.setCleanSession(true);
		conn.setConnectionTimeout(30);
		conn.setKeepAliveInterval(30);
		conn.setUserName("admin");
		conn.setPassword("password".toCharArray());
		return conn;
	}
	
	//hard coded value here!
	Subscriber<Inference> getSubscriber(File outputFile, PrintWriter pw) {
        return new Subscriber<Inference>() {
            @Override public void onCompleted() {
                System.out.println("\nstream completed. see output: " + outputFile.getAbsolutePath());
                try {
                    pw.flush();
                    pw.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            @Override public void onError(Throwable e) { e.printStackTrace(); }
            @Override public void onNext(Inference infer) {
            		
            		System.out.println("$$$$$$,"+ infer.getRecordNum() + "," + (Double)infer.getClassifierInput().get("consumption").get("inputValue") 
            				+ "," + infer.getAnomalyScore());
            	
            	}
          };
     }

}
