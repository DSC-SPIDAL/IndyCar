package com.dsc.iu.stream.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
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

public class ScalarMetricLatency extends BaseRichBolt {
	
	private final long serialVersionUID = 1L;
	private OutputCollector collector;
	private String metric;
	private String carnum, counter;
	private int min, max;
	private Publisher manualpublish;
	private Network network;
	private ConcurrentHashMap<String, String> lapdistancemap;
	private ConcurrentHashMap<String, String> timeOfDaymap;
	
	private PrintWriter pw;
	
	public ScalarMetricLatency(String metric, String min, String max) {
		this.metric = metric;
		this.min = Integer.parseInt(min);
		this.max = Integer.parseInt(max);
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public void execute(Tuple arg0) {
		
		carnum = arg0.getStringByField("carnum");
		counter = arg0.getStringByField("counter");
		lapdistancemap.put(carnum + "_" + counter, arg0.getStringByField("lapDistance"));
		timeOfDaymap.put(carnum + "_" + counter, arg0.getStringByField("timeOfDay"));
		manualpublish.onNext(arg0.getStringByField(getMetric()) + "," + counter);
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		
		lapdistancemap = new ConcurrentHashMap<String, String>();
		timeOfDaymap = new ConcurrentHashMap<String, String>();
		setMetric(metric);
		setMin(min);
		setMax(max);
		
		File boltfile = new File("/scratch_ssd/sahil/boltfile-"+ UUID.randomUUID().toString() + ".txt");
		try {
			pw = new PrintWriter(boltfile);
		} catch(FileNotFoundException f) {
			f.printStackTrace();
		}
		
		collector = arg2;
		
//		manualpublish = Publisher.builder()
//					.addHeader(getMetric() + ",counter" + ",timeOfDay")
//					.addHeader("float,float,datetime")
//					.addHeader("B")
//					.build();
		
		manualpublish = Publisher.builder()
				.addHeader(getMetric() + ",counter")
				.addHeader("float,float")
				.addHeader("B")
				.build();
		
		Sensor<ObservableSensor<String[]>> sensor = Sensor.create(ObservableSensor::create, SensorParams.create(Keys::obs, 
													new Object[] {"two_metrics", manualpublish }));
		Parameters params = getParams();
		params = params.union(getNetworkLearningEncoderParams());
		Network network = Network.create("two_metrics", params)
						.add(Network.createRegion("region1")
						.add(Network.createLayer("layer2/3", params)
						.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
						.add(Anomaly.create())
						.add(new TemporalMemory())
						.add(new SpatialPooler())
						.add(sensor)));
		
		network.observe().subscribe(getSubscriber());
		network.start();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("carnum","metric","dataval","score","counter", "lapDistance", "timeOfDay"));
	}
	
	private static Parameters getParams() {
		Parameters parameters = Parameters.getAllDefaultParameters();
        parameters.set(Parameters.KEY.INPUT_DIMENSIONS, new int[] { 8 });
        parameters.set(KEY.COLUMN_DIMENSIONS, new int[] { 20 });
        parameters.set(KEY.CELLS_PER_COLUMN, 6);
        
        //SpatialPooler specifics
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
        
        //Temporal Memory specifics
        parameters.set(KEY.INITIAL_PERMANENCE, 0.2);
        parameters.set(KEY.CONNECTED_PERMANENCE, 0.8);
        parameters.set(KEY.MIN_THRESHOLD, 5);
        parameters.set(KEY.MAX_NEW_SYNAPSE_COUNT, 6);
        parameters.set(KEY.PERMANENCE_INCREMENT, 0.05);
        parameters.set(KEY.PERMANENCE_DECREMENT, 0.05);
        parameters.set(KEY.ACTIVATION_THRESHOLD, 4);
        
        return parameters;
	}
	
	private Parameters getNetworkLearningEncoderParams() {
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
        p.set(KEY.INFERRED_FIELDS, getInferredFieldsMap(getMetric(), SDRClassifier.class));
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
	
	private static Map<String, Class<? extends Classifier>> getInferredFieldsMap(String field, Class<? extends Classifier> classifier) {
        Map<String, Class<? extends Classifier>> inferredFieldsMap = new HashMap<>();
        inferredFieldsMap.put(field, classifier);
        
        return inferredFieldsMap;
    }
	
	private Map<String, Map<String, Object>> getNetworkDemoFieldEncodingMap() {
		
		Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 50, 21, getMin(), getMax(), 0, 0.1, null, Boolean.TRUE, null, getMetric(), 
																	"float", "ScalarEncoder");
		fieldEncodings = setupMap(fieldEncodings, 50, 21, Long.MIN_VALUE, Long.MAX_VALUE, 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "counter", 
									"float", "ScalarEncoder");
        return fieldEncodings;
        
//        Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 0, 0, 0, 0, 0, 0, null, null, null, "timeOfDay", "datetime", "DateEncoder");
//	    fieldEncodings = setupMap(fieldEncodings, 50, 21, getMin(), getMax(), 0, 0.1, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, getMetric(), "float", "ScalarEncoder");
//		fieldEncodings = setupMap(fieldEncodings, 50, 21, 0, 400, 0, 0.1, null, Boolean.TRUE, null, "counter", "float", "ScalarEncoder");
//	    
//	    fieldEncodings.get("timeOfDay").put(KEY.DATEFIELD_TOFD.getFieldName(), new org.numenta.nupic.util.Tuple(21,9.5)); // Time of day
//        fieldEncodings.get("timeOfDay").put(KEY.DATEFIELD_PATTERN.getFieldName(), "MM/dd/YY HH:mm:ss.SSS");
//		return fieldEncodings;
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
	
	private Subscriber<Inference> getSubscriber() {
        return new Subscriber<Inference>() {
            @Override public void onCompleted() {}
            @Override public void onError(Throwable e) { e.printStackTrace(); }
            @Override public void onNext(Inference infer) {
            		double actual_val = (Double)infer.getClassifierInput().get(getMetric()).get("inputValue");
            		Double counter_val = (Double)infer.getClassifierInput().get("counter").get("inputValue");
//            	DateTime timeOfDay = (DateTime)infer.getClassifierInput().get("timeOfDay").get("inputValue");
            		long emitTs = System.currentTimeMillis();
            		int ctrval = counter_val.intValue();
            		
            		//fetch lapDistance and remove from map once done
            		if(lapdistancemap.containsKey(carnum + "_" + ctrval)) {
            			String lapdistance = lapdistancemap.get(carnum + "_" + ctrval);
            			String timeOfDay = timeOfDaymap.get(carnum + "_" + ctrval);
//                		collector.emit(new Values(carnum, getMetric(), String.format("%3.2f", actual_val), infer.getAnomalyScore(), String.valueOf(ctrval), lapdistance, 
//                						String.valueOf(timeOfDay.getHourOfDay() + ":" + timeOfDay.getMinuteOfHour() + ":" + timeOfDay.getSecondOfMinute() + "." + timeOfDay.getMillisOfSecond())));
            			
            			collector.emit(new Values(carnum, getMetric(), String.format("%3.2f", actual_val), infer.getAnomalyScore(), String.valueOf(ctrval), lapdistance, timeOfDay));
                		
                		pw.println("***scalar metric out," + getMetric() + "_" + String.valueOf(ctrval) + "_" + carnum + "," + String.format("%3.2f", actual_val) + "," + infer.getAnomalyScore() + "," + emitTs);
                		pw.flush();
                		
                		lapdistancemap.remove(carnum + "_" + ctrval);
            		}
            }
        };
    }
}