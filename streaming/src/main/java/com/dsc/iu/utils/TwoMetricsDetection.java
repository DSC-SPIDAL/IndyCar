package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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
public class TwoMetricsDetection {
	public static void main(String[] args) {
		
		Publisher manualPublisher = Publisher.builder().addHeader("rpm").addHeader("float").addHeader("B").build();
		Sensor<ObservableSensor<String[]>> sensor = Sensor.create(ObservableSensor::create, SensorParams.create(Keys::obs, new Object[] { "rpm_counter", manualPublisher }));
		Parameters params = getParams();
		params = params.union(getNetworkLearningEncoderParams());
		Network network = Network.create("rpmcounter", params).add(Network.createRegion("region1").add(Network.createLayer("layer2/3", params)
						.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE).add(Anomaly.create()).add(new TemporalMemory()).add(new SpatialPooler()).add(sensor)));
		
		File output = new File("/Users/sahiltyagi/Desktop/htmoutput.txt");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(output));
			network.observe().subscribe(getSubscriber(output, pw));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		network.start();
		System.out.println("started the HTM network");
		try {
			BufferedReader logreader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/20RPM.csv")));
			String record;
			while((record = logreader.readLine()) != null) {
				manualPublisher.onNext(record.split(",")[0]);
				//500 msg/sec rate
//				try {
//					Thread.sleep(2);
//				} catch(InterruptedException e) {}
			}
			System.out.println("completely read the 20_RPM log file");
			logreader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createlogfile() {
		try {
			BufferedReader logreader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Downloads/Indy_500_2018/IPBroadcaster_Input_2018-05-27_0.log")));
			File f = new File("/Users/sahiltyagi/Desktop/20RPM.csv");
			PrintWriter pw = new PrintWriter(f);
			String rec;
			int ctr=0;
			while((rec=logreader.readLine()) != null) {
				if(rec.startsWith("$P") && rec.split("�")[2].length() >9 && rec.split("�")[1].equals("20")) {
					ctr++;
					pw.println(rec.split("�")[5] + "," + ctr);
					pw.flush();
				}
			}
			System.out.println("created rpm 20");
			logreader.close();
			pw.close();
		} catch(IOException e) {e.printStackTrace();}
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

        
//		Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 50, 21, -3000, 15000, 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "rpm", "float", "ScalarEncoder");
//	    fieldEncodings = setupMap(fieldEncodings, 50, 21, Long.MIN_VALUE, Long.MAX_VALUE, 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "counter", "float", "ScalarEncoder");
//		return fieldEncodings;
		
		Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 50, 21, -3000, 15000, 0, 0.1, null, Boolean.TRUE, null, "rpm", "float", "ScalarEncoder");
		//fieldEncodings = setupMap(fieldEncodings, 50, 21, Long.MIN_VALUE, Long.MAX_VALUE, 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "counter", "float", "ScalarEncoder");
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
        inferredFieldsMap.put("rpm", SDRClassifier.class);
        //inferredFieldsMap.put("counter", SDRClassifier.class);
        
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
			Double rpm = (Double)infer.getClassifierInput().get("rpm").get("inputValue");
			 StringBuilder sb = new StringBuilder().append(infer.getRecordNum()).append(",").append(",")
					 			.append(String.format("%3.2f", rpm)).append(",").append(infer.getAnomalyScore()).append(",")
                     			.append(System.currentTimeMillis());
             pw.println(sb.toString());
             
             pw.flush();
		}
	}
}
