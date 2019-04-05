package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.AnomalyLikelihood;
import org.numenta.nupic.algorithms.Classifier;
import org.numenta.nupic.algorithms.SDRClassifier;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;

import rx.Subscriber;

public class HTMJavacode {
	//private static Lock lock = new ReentrantLock();
	private static long startTs=0L;
	private static PrintWriter pw, pw2;
	private static double min, max;
	private static int index, infercounter, datacounter;
	private static Publisher publisher;
	private static boolean flag = false;
	private static String carnum;
	private static AnomalyLikelihood likelihood;
	
	public static void main(String[] args) {
		//String metric = args[0];
		String metric = "speed";
		HTMJavacode ob = new HTMJavacode();
		infercounter=0;
		
		//likelihood = new AnomalyLikelihood(useMovingAvg, windowSize, isWeighted, claLearningPeriod, estimationSamples)
		//likelihood = new AnomalyLikelihood(false, 0, false, 300, 300);
		//two anomalies: size gives 32 windows over car #13 dataset (35K/32~1K window size)
		likelihood = new AnomalyLikelihood(true, 1000, false, 288, 100);
		
		if(metric.equals("RPM")) {
			min = 0;
			max = 12500;
			index = 1;
		} else if(metric.equals("throttle")) {
			min = 0;
			max = 6;
			index = 2;
		} else if(metric.equals("speed")) {
//			min = -100;
//			max = 300;
			min = -47.178;
			max = 283.068;
			index = 0;
		}
		
		File f2 = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/dataHTM.csv");
		try {
			pw2 = new PrintWriter(f2);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		
		File f = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/inferHTM.csv");
		try {
			pw = new PrintWriter(f);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		System.out.println("going to start HTM network...");
		publisher = startNetwork(metric, min, max);
		
		carnum = "13";
		publishdata(carnum);
		
//		while(true) {
//			if(q.size() >0) {
//				lock.lock();
//				try {
//					startTs = System.currentTimeMillis();
//					uid = q.peek().split(",")[3];
//					pub.onNext(q.poll().split(",")[index]);
//					
//				} finally {
//					lock.unlock();
//				}
//			}
//		}
	}
	
	private static void publishdata(String carnum) {
//		try {
//			List<String> carlist = new LinkedList<String>();
//			carlist.add("20");
//			
//			File f = new File("/N/u/styagi/IPBroadcaster_Input_2018-05-27_0.log");
////			File f = new File("/Users/sahiltyagi/Downloads/Indy_500_2018/IPBroadcaster_Input_2018-05-27_0.log");
//			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
//			System.out.println("going to start reading at:"+System.currentTimeMillis());
//			
//			String line;
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//			datacounter=0; 
//			int newctr=0;
//			while((line=rdr.readLine()) != null) {
//				if(line.startsWith("$P") && line.split("�")[2].matches("\\d+:\\d+:\\d+.\\d+") && line.split("�")[1].equalsIgnoreCase(carnum)) {
//					datacounter++;
//					//for car #13
//					if(datacounter > 28746) {
//						newctr++;
//						String payload = line.split("�")[4] + "," + line.split("�")[5] + "," + line.split("�")[6] + "," + newctr 
//								+ "," + line.split("�")[3] + "," + "2018-05-27 " + line.split("�")[2];
//						
//						startTs = System.currentTimeMillis();
//						pw2.println(payload + "," + startTs);
//						publisher.onNext(payload.split(",")[index]);
//						try {
//							Thread.sleep(10);
//						} catch(InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//			
//			System.out.println("successfully read the log file for car #20");
//			System.out.println("end timestamp after file read: " + System.currentTimeMillis());
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
		
		try {
			File f = new File("/Users/sahiltyagi/Desktop/results/numenta_indy2018-13-vspeed.csv");
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line=rdr.readLine();
			int newctr=0;
			while((line=rdr.readLine()) != null) {
				newctr++;
				String payload = line.split(",")[0] + "," + line.split(",")[1] + "," + newctr;
				pw2.println(payload + "," + System.currentTimeMillis());
				pw2.flush();
				publisher.onNext(payload.split(",")[1]);
//				try {
//					Thread.sleep(1);
//				} catch(InterruptedException e) {
//					e.printStackTrace();
//				}
			}
			System.out.println("done reading the file");
			rdr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Publisher startNetwork(String metric, double min, double max) {
		Publisher manualPublisher = Publisher.builder().addHeader(metric).addHeader("float").addHeader("B").build();
		Sensor<ObservableSensor<String[]>> sensor = Sensor.create(ObservableSensor::create, 
													SensorParams.create(Keys::obs, 
													new Object[] { "htm java", manualPublisher }));
		Parameters params = getParams();
		params = params.union(getNetworkLearningEncoderParams(metric, min, max));
		Network network = Network.create("htm java sequential", params)
						.add(Network.createRegion("region1")
						.add(Network.createLayer("layer2/3", params)
						//.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
						.add(Anomaly.create())
						.add(new TemporalMemory())
						.add(new SpatialPooler())
						.add(sensor)));
		
		network.observe().subscribe(getSubscriber(metric));
		network.start();
		
		return manualPublisher;
	}
	
	private static Subscriber<Inference> getSubscriber(String metric) {
		return new Subscriber<Inference>() {
			@Override public void onCompleted() {}
			@Override public void onError(Throwable e) { e.printStackTrace(); }
			@Override public void onNext(Inference infer) {
				infercounter++;
				double actual_val = (Double) infer.getClassifierInput().get(metric).get("inputValue");
				double anomalyscore = infer.getAnomalyScore();
				double anomaly_likelihood = likelihood.anomalyProbability(actual_val, anomalyscore, null);
				long endTs = System.currentTimeMillis();
//				pw.println(carnum + "," + infercounter + "," + metric + "," + actual_val + "," + anomaly_likelihood + "," + anomalyscore + "," + endTs);
				pw.println(carnum + "," + infercounter + "," + metric + "," + actual_val + "," + anomaly_likelihood + "," + endTs);
				pw.flush();
			}
		};
	}
	
	private static Parameters getNetworkLearningEncoderParams(String metric, double min, double max) {
        Map<String, Map<String, Object>> fieldEncodings = getFieldEncodingMap(metric, min, max);

        Parameters p = Parameters.getEncoderDefaultParameters();
        p.set(KEY.GLOBAL_INHIBITION, true);
        p.set(KEY.COLUMN_DIMENSIONS, new int[] { 2048 });
        p.set(KEY.CELLS_PER_COLUMN, 32);
        p.set(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 40.0);
        p.set(KEY.POTENTIAL_PCT, 0.8);
        p.set(KEY.SYN_PERM_CONNECTED,0.1);
        p.set(KEY.SYN_PERM_ACTIVE_INC, 0.0001);
		p.set(KEY.SYN_PERM_INACTIVE_DEC, 0.0005);
        p.set(KEY.MAX_BOOST, 1.0);
        p.set(KEY.INFERRED_FIELDS, getInferredFieldsMap(metric, SDRClassifier.class));
        
        p.set(KEY.MAX_NEW_SYNAPSE_COUNT, 20);
        p.set(KEY.INITIAL_PERMANENCE, 0.21);
        p.set(KEY.PERMANENCE_INCREMENT, 0.1);
        p.set(KEY.PERMANENCE_DECREMENT, 0.1);
        p.set(KEY.MIN_THRESHOLD, 9);
        p.set(KEY.ACTIVATION_THRESHOLD, 12);
        
        p.set(KEY.CLIP_INPUT, true);
        p.set(KEY.FIELD_ENCODING_MAP, fieldEncodings);

        return p;
    }
	
	private static Map<String, Map<String, Object>> getFieldEncodingMap(String metric, double min, double max) {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 50, 21, min, max, 0, 0.1, null, Boolean.TRUE, null, metric, "float", "ScalarEncoder");
        return fieldEncodings;
    }
	
	private static Map<String, Class<? extends Classifier>> getInferredFieldsMap(String field, Class<? extends Classifier> classifier) {
        Map<String, Class<? extends Classifier>> inferredFieldsMap = new HashMap<>();
        inferredFieldsMap.put(field, classifier);
        return inferredFieldsMap;
    }
	
	private static Map<String, Map<String, Object>> setupMap(
            Map<String, Map<String, Object>> map,
            int n, int w, double min, double max, double radius, double resolution, Boolean periodic,
            Boolean clip, Boolean forced, String fieldName, String fieldType, String encoderType) {

        if(map == null) {
            map = new HashMap<String, Map<String, Object>>();
        }
        Map<String, Object> inner = null;
        if((inner = map.get(fieldName)) == null) {
            map.put(fieldName, inner = new HashMap<String, Object>());
        }

        inner.put("n", n);
        inner.put("w", w);
        inner.put("minVal", min);
        inner.put("maxVal", max);
        inner.put("radius", radius);
        inner.put("resolution", resolution);

        if(periodic != null) inner.put("periodic", periodic);
        if(clip != null) inner.put("clipInput", clip);
        if(forced != null) inner.put("forced", forced);
        if(fieldName != null) inner.put("fieldName", fieldName);
        if(fieldType != null) inner.put("fieldType", fieldType);
        if(encoderType != null) inner.put("encoderType", encoderType);

        return map;
    }
	
	private static Parameters getParams() {
		Parameters parameters = Parameters.getAllDefaultParameters();
        parameters.set(Parameters.KEY.INPUT_DIMENSIONS, new int[] { 8 });
        parameters.set(KEY.COLUMN_DIMENSIONS, new int[] { 20 });
        parameters.set(KEY.CELLS_PER_COLUMN, 6);
        
        //SpatialPooler specific
        parameters.set(KEY.POTENTIAL_RADIUS, 12);//3
        parameters.set(KEY.POTENTIAL_PCT, 0.5);//0.5
        parameters.set(KEY.GLOBAL_INHIBITION, false);
        parameters.set(KEY.LOCAL_AREA_DENSITY, -1.0);
        parameters.set(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 5.0);
        parameters.set(KEY.STIMULUS_THRESHOLD, 1.0);
        parameters.set(KEY.SYN_PERM_INACTIVE_DEC, 0.01);
        parameters.set(KEY.SYN_PERM_ACTIVE_INC, 0.1);
        parameters.set(KEY.SYN_PERM_TRIM_THRESHOLD, 0.05);
        parameters.set(KEY.SYN_PERM_CONNECTED, 0.1);
        parameters.set(KEY.MIN_PCT_OVERLAP_DUTY_CYCLES, 0.1);
        parameters.set(KEY.MIN_PCT_ACTIVE_DUTY_CYCLES, 0.1);
        parameters.set(KEY.DUTY_CYCLE_PERIOD, 10);
        parameters.set(KEY.MAX_BOOST, 10.0);
        parameters.set(KEY.SEED, 42);
        
        //Temporal Memory specific
        parameters.set(KEY.INITIAL_PERMANENCE, 0.2);
        parameters.set(KEY.CONNECTED_PERMANENCE, 0.8);
        parameters.set(KEY.MIN_THRESHOLD, 5);
        parameters.set(KEY.MAX_NEW_SYNAPSE_COUNT, 6);
        parameters.set(KEY.PERMANENCE_INCREMENT, 0.05);
        parameters.set(KEY.PERMANENCE_DECREMENT, 0.05);
        parameters.set(KEY.ACTIVATION_THRESHOLD, 4);
        //parameters.set(KEY.MAX_SEGMENTS_PER_CELL, 128);
        
        return parameters;
	}
}
