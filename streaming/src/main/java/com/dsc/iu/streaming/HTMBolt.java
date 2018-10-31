package com.dsc.iu.streaming;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
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

public class HTMBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Publisher manualpublish;
	Network network;
	private int subscriberIndex=0;
	private int executeTupleIndex=0;
	private long subscriberTStamp;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		//timestamp parameter denotes the date and time for the record in the race
		manualpublish = Publisher.builder()
								.addHeader("timestamp,consumption")
								.addHeader("datetime,float")
								.addHeader("B")
								.build();
		
		Sensor<ObservableSensor<String[]>> sensor = Sensor.create(
        	     ObservableSensor::create, 
        	         SensorParams.create(
        	             Keys::obs, new Object[] { "kakkerot", manualpublish }));
		
		Parameters p = getLearningParameters();
		p = p.union(getNetworkLearningEncoderParams());
		network =  Network.create("Network API Demo", p)
				.add(Network.createRegion("Region 1")
				.add(Network.createLayer("Layer 2/3", p)
				//.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
				.add(Anomaly.create())
				.add(new TemporalMemory())
				.add(new SpatialPooler())
				.add(sensor)));
		
		network.observe().subscribe(getSubscriber());
		network.start();
		
	}

	@Override
	public void execute(Tuple input) {
		// TODO Auto-generated method stub
		if(input.size() >0) {
			
			String record = input.getStringByField("record");
			//long ts = input.getLongByField("timestamp");
			
			//logging the input value
			executeTupleIndex++;
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$," + executeTupleIndex + "," + record.split(",")[1] + "," + System.currentTimeMillis());
			manualpublish.onNext(record);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}
	
	Subscriber<Inference> getSubscriber() {
        return new Subscriber<Inference>() {
            @Override public void onCompleted() {
                System.out.println("***********************HTM anomaly detection stream completed***********************");
            }
            @Override public void onError(Throwable e) { e.printStackTrace(); }
            @Override public void onNext(Inference i) {
            		subscriberIndex++;
            		subscriberTStamp = System.currentTimeMillis();
            		double actual = (Double)i.getClassifierInput()
            					.get("consumption").get("inputValue");
            		System.out.println("###################," + subscriberIndex + "," 
            					+ actual + "," + i.getAnomalyScore() + "," + subscriberTStamp);
            	}
        };
    }
	
	public static Parameters getLearningParameters() {
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
	
	public static Parameters getNetworkLearningEncoderParams() {
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
        p.set(KEY.INFERRED_FIELDS, getInferredFieldsMap("consumption", SDRClassifier.class));
        
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
	
	public static Map<String, Map<String, Object>> getNetworkDemoFieldEncodingMap() {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                0, // n
                0, // w
                0, 0, 0, 0, null, null, null,
                "timestamp", "datetime", "DateEncoder");
        fieldEncodings = setupMap(
                fieldEncodings, 
                50, 
                21, 
                0, 400, 0, 0.1, null, Boolean.TRUE, null, 
                "consumption", "float", "ScalarEncoder");
        
        fieldEncodings.get("timestamp").put(KEY.DATEFIELD_TOFD.getFieldName(), new org.numenta.nupic.util.Tuple(21,9.5)); // Time of day
        fieldEncodings.get("timestamp").put(KEY.DATEFIELD_PATTERN.getFieldName(), "MM/dd/YY HH:mm:ss.SSS");
        
        return fieldEncodings;
    }
	
	public static Map<String, Class<? extends Classifier>> getInferredFieldsMap(
            String field, Class<? extends Classifier> classifier) {
        Map<String, Class<? extends Classifier>> inferredFieldsMap = new HashMap<>();
        inferredFieldsMap.put(field, classifier);
        return inferredFieldsMap;
    }
	
	 public static Map<String, Map<String, Object>> setupMap(
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

}
