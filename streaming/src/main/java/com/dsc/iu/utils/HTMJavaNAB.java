package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.AnomalyLikelihood;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;
import org.numenta.nupic.util.Tuple;

import rx.Subscriber;

public class HTMJavaNAB {
	private static PrintWriter pw;
	private static String carnum;
	private static AnomalyLikelihood likelihood;
	
	public static void main(String[] args) {
		HTMJavaNAB obj = new HTMJavaNAB();
		
		File f2 = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/dataHTM.csv");
		PrintWriter pw2 = null;
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
		
		Network network;
		Publisher manualPublisher = Publisher.builder()
				.addHeader("c0,c1")
                .addHeader("datetime,float")
                .addHeader("T,B")
                .build();
		
		Parameters p = Parameters.getAllDefaultParameters()
				.union(obj.getSpatialPoolerParams())
				.union(obj.getTemporalMemoryParams())
				.union(obj.getSensorParams());
		
		Sensor<ObservableSensor<String[]>> sensor = Sensor.create(ObservableSensor::create, SensorParams.create(Keys::obs, new Object[] { "two_metrics", manualPublisher}));
		network = Network.create("NAB Network", p)
	            .add(Network.createRegion("NAB Region")
	                .add(Network.createLayer("NAB Layer", p)
	                    .add(Anomaly.create())
	                    .add(new TemporalMemory())
	                    .add(new SpatialPooler())
	                    .add(sensor)));
		//likelihood = new AnomalyLikelihood(useMovingAvg, windowSize, isWeighted, claLearningPeriod, estimationSamples)
		//likelihood = new AnomalyLikelihood(false, 0, true, 288, 100);
		likelihood = new AnomalyLikelihood(false, 0, false, 300, 300);
		network.observe().subscribe(obj.getSubscriber("speed"));
		network.start();
		System.out.println("started HTM NAB...");
		
		try {
			File input = new File("/Users/sahiltyagi/Desktop/results/numenta_indy2018-13-vspeed.csv");
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			int newctr=0;
			String line=rdr.readLine();
			while((line=rdr.readLine()) != null) {
				newctr++;
				String payload = line.split(",")[0] + "," + line.split(",")[1] + "," + newctr;
				pw2.println(payload + "," + System.currentTimeMillis());
				pw2.flush();
				manualPublisher.onNext(line.split(",")[0] + "," + line.split(",")[1]);
			}
			rdr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private Subscriber<Inference> getSubscriber(String metric) {
		return new Subscriber<Inference>() {
			@Override public void onCompleted() {}
			@Override public void onError(Throwable e) { e.printStackTrace(); }
			@Override public void onNext(Inference infer) {
//				double actual_val = (Double) infer.getClassifierInput().get(metric).get("inputValue");
				double actual_val = (Double) infer.getClassifierInput().get("c1").get("inputValue");
				DateTime datetime = (DateTime) infer.getClassifierInput().get("c0").get("inputValue");
				double anomalyscore = infer.getAnomalyScore();
				double anomaly_likelihood = likelihood.anomalyProbability(actual_val, anomalyscore, datetime);
				int recordnum = infer.getRecordNum();
				long endTs = System.currentTimeMillis();
				pw.println(carnum + "," + recordnum + "," + metric + "," + actual_val + "," + anomaly_likelihood + "," + endTs);
//				pw.println(carnum + "," + (recordnum+1) + "," + metric + "," + actual_val + "," + anomalyscore + "," + endTs);
				pw.flush();
			}
		};
	}
	
	private Parameters getSpatialPoolerParams() {
		Parameters params = Parameters.getSpatialDefaultParameters();
		params.set(KEY.COLUMN_DIMENSIONS, new int[] { 2048 });
		//set to 2.0 or 1.0-->1.0 disables it
		params.set(KEY.MAX_BOOST, 2.0);
		params.set(KEY.SYN_PERM_INACTIVE_DEC, 0.0005);
		params.set(KEY.SYN_PERM_CONNECTED, 0.2);
		params.set(KEY.SYN_PERM_ACTIVE_INC, 0.003);
		//edit:changed to double from int
		params.set(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 40.0);
		//edit: changed to boolean from 1
		params.set(KEY.GLOBAL_INHIBITION, true);
		params.set(KEY.POTENTIAL_PCT, 0.8);
		
		return params;
	}
	
	private Parameters getTemporalMemoryParams() {
		Parameters params = Parameters.getTemporalDefaultParameters();
		params.set(KEY.COLUMN_DIMENSIONS, new int[] { 2048 });
		params.set(KEY.ACTIVATION_THRESHOLD, 20);
		params.set(KEY.CELLS_PER_COLUMN, 32);
		params.set(KEY.PERMANENCE_INCREMENT, 0.04);
		params.set(KEY.MIN_THRESHOLD, 13);
		params.set(KEY.INITIAL_PERMANENCE, 0.24);
		params.set(KEY.MAX_SEGMENTS_PER_CELL, 128);
		params.set(KEY.MAX_SYNAPSES_PER_SEGMENT, 128);
		params.set(KEY.PERMANENCE_DECREMENT, 0.008);
		params.set(KEY.PREDICTED_SEGMENT_DECREMENT, 0.001);
		params.set(KEY.MAX_NEW_SYNAPSE_COUNT, 31);
		
		return params;
	}
	
	private Parameters getSensorParams() {
		Map<String, Map<String, Object>> fieldEncodings = getfieldEncodingsMap();
		Parameters params = Parameters.empty();
		params.set(KEY.CLIP_INPUT, true);
		params.set(KEY.FIELD_ENCODING_MAP, fieldEncodings);
		
		return params;
	}
	
	private static Map<String, Map<String, Object>> getfieldEncodingsMap() {
		Map<String, Map<String, Object>> fieldEncodings = new HashMap<>();
		Map<String, Object> fieldMap;
        String fieldName = "c1";
        fieldMap = fieldEncodings.get(fieldName);
        if (fieldMap == null) {
        		System.out.println("in c1 null");
            fieldMap = new HashMap<>();
            fieldMap.put("fieldName", fieldName);
            //edit: use ScalarEncoder and see results
            fieldMap.put("encoderType", "RandomDistributedScalarEncoder");
            fieldMap.put("fieldType", "float");
            //edit:added resolution. not adding it gave it a negative value
//            fieldMap.put("resolution", 0.001);
            fieldMap.put("resolution", 0.1);
            System.out.println("resolution:"+fieldMap.get("resolution"));
            fieldEncodings.put(fieldName, fieldMap);
        }
        
        fieldName = "c0";
        fieldMap = fieldEncodings.get(fieldName);
        if (fieldMap == null) {
        		System.out.println("in c0 null");
            fieldMap = new HashMap<>();
            fieldMap.put("fieldName", fieldName);
            fieldMap.put("encoderType", "DateEncoder");
            fieldMap.put("fieldType", "datetime");
            fieldMap.put(KEY.DATEFIELD_PATTERN.getFieldName(), "YYYY-MM-dd HH:mm:ss.SSS");
            fieldMap.put(KEY.DATEFIELD_TOFD.getFieldName(), new Tuple(21, 9.49));
            fieldEncodings.put(fieldName, fieldMap);
        }
        
        return fieldEncodings;
	}
}
