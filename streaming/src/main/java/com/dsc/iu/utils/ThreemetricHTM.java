package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
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

/*
 * combining multiple metrics for anomaly detection
 * */
public class ThreemetricHTM {
	public static void main(String[] args) {
		Publisher manualPublisher = Publisher.builder()
								.addHeader("speed,rpm,timestamp")
								.addHeader("float,float,datetime")
								.addHeader("B")
								.build();
		Sensor<ObservableSensor<String[]>> sensor = Sensor.create(ObservableSensor::create, SensorParams.create(Keys::obs, new Object[] { "3metrics", manualPublisher }));
		Parameters params = getParams();
		params = params.union(getNetworkLearningEncoderParams());
		Network network = Network.create("3metrics", params)
						.add(Network.createRegion("region1")
						.add(Network.createLayer("layer2/3", params)
						.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
						.add(Anomaly.create())
						.add(new TemporalMemory())
						.add(new SpatialPooler())
						.add(sensor)));
		
		File output = new File("/Users/sahiltyagi/Desktop/htmoutput.txt");
//		File output = new File("D:\\\\anomalydetection\\htmoutput.txt");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(output));
			network.observe().subscribe(getSubscriber(output, pw));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		network.start();
		System.out.println("started the HTM network");
//		try {
//			BufferedReader logreader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/dixon_SPEED_RPM.log")));
////			BufferedReader logreader = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\\\anomalydetection\\dixon_SPEED_RPM.log")));
//
//			String record;
//			//manualPublisher.onNext("0.000,0");
//			manualPublisher.onNext("0.00, 23");
//			while((record = logreader.readLine()) != null) {
////				String newrec = record.split(",")[0] + "," + String.valueOf((Double.parseDouble(record.split(",")[1])/2));
//				String newrec = record.split(",")[0] + "," + String.valueOf((Double.parseDouble(record.split(",")[1])*0.02));
//				System.out.println(newrec);
//				manualPublisher.onNext(newrec);
//			}
//			
//			logreader.close();
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
		
		while(true) {
			manualPublisher.onNext("220.0,1234,5/28/18 23:59:50.999");
		}
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
        
        return parameters;
	}
	
	private static Parameters getNetworkLearningEncoderParams() {
        Map<String, Map<String, Object>> fieldEncodings = getNetworkDemoFieldEncodingMap();

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
        p.set(KEY.INFERRED_FIELDS, getInferredFieldsMap());
        
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
	
	private static Map<String, Map<String, Object>> getNetworkDemoFieldEncodingMap() {
		/*
		 * changed the periodic and clip boolean metrics from the initial  single metric
		 * changed n and w for RPM input metric
		 * */
		
//        Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 50, 21, 0, 250, 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "speed", "float", "ScalarEncoder");
//        fieldEncodings = setupMap(fieldEncodings, 100, 41, 0, 12500, 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "rpm", "float", "ScalarEncoder");

        
		Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 0, 0, 0, 0, 0, 0, null, null, null, "timestamp", "datetime", "DateEncoder");
	    fieldEncodings = setupMap(fieldEncodings, 50, 21, Long.MIN_VALUE, Long.MAX_VALUE, 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "rpm", "float", "ScalarEncoder");
		fieldEncodings = setupMap(fieldEncodings, 50, 21, 0, 400, 0, 0.1, null, Boolean.TRUE, null, "speed", "float", "ScalarEncoder");
	    
	    fieldEncodings.get("timestamp").put(KEY.DATEFIELD_TOFD.getFieldName(), new org.numenta.nupic.util.Tuple(21,9.5)); // Time of day
        fieldEncodings.get("timestamp").put(KEY.DATEFIELD_PATTERN.getFieldName(), "MM/dd/YY HH:mm:ss.SSS");
		return fieldEncodings;
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
	
	private static Map<String, Class<? extends Classifier>> getInferredFieldsMap() {
        Map<String, Class<? extends Classifier>> inferredFieldsMap = new HashMap<>();
        inferredFieldsMap.put("speed", SDRClassifier.class);
        inferredFieldsMap.put("rpm", SDRClassifier.class);
        inferredFieldsMap.put("timestamp", SDRClassifier.class);
        
        return inferredFieldsMap;
    }
	
	private static Subscriber<Inference> getSubscriber(File outputFile, PrintWriter pw) {
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
            @Override public void onNext(Inference i) {
            		writeToFileAnomaly(i, pw); 
            	}
        };
    }
	
	private static void writeToFileAnomaly(Inference infer, PrintWriter pw) {
		if(infer.getRecordNum() > 0) {
			Double speed = (Double)infer.getClassifierInput().get("speed").get("inputValue");
			Double rpm = (Double)infer.getClassifierInput().get("rpm").get("inputValue");
			DateTime TOD = (DateTime)infer.getClassifierInput().get("timestamp").get("inputValue");
			System.out.println("time of day:" + TOD.getHourOfDay()+":"+TOD.getMinuteOfHour()+":"+TOD.getSecondOfMinute()+"."+TOD.getMillisOfSecond());
			//Integer i = speed.intValue();
			 StringBuilder sb = new StringBuilder().append(infer.getRecordNum()).append(",").append(String.format("%3.2f", speed)).append(",")
					 			.append(String.format("%3.2f", rpm)).append(",").append(infer.getAnomalyScore()).append(",")
                     			.append(System.currentTimeMillis());
             System.out.println(sb.toString());
			 pw.println(sb.toString());
             pw.flush();
		}
	}
}