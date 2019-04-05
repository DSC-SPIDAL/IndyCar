package com.dsc.iu.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.Classifier;
import org.numenta.nupic.algorithms.SDRClassifier;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.datagen.ResourceLocator;
import org.numenta.nupic.encoders.Encoder;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.sensor.FileSensor;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;
import org.numenta.nupic.util.Tuple;

import rx.Subscriber;

public class OnlineLearningUtils {
	public static int inflightMsgRate = 5000;
	public static int QoS = 2;
//	public static String brokerurl = "tcp://10.16.4.204:61613";
//	public static String mqttadmin = "admin";
//	public static String mqttpwd = "password";
	public static String sinkoutTopic = "streaming_output";
	public static String restart_topic = "status";
	public static String brokerurl = "tcp://j-093.juliet.futuresystems.org:61613";
	public static String mqttadmin = "admin";
	public static String mqttpwd = "xyi5b2YUcw8CHhAE";
	
//	public static String brokerurl = "tcp://10.16.0.23:61613";
//	public static String mqttadmin = "admin";
//	public static String mqttpwd = "password";
	
	public static Network createBasicLearningNetwork(Sensor<ObservableSensor<String[]>> sensor, Publisher manualPublisher) {	
		Parameters params = getLearningParameters();
		params = params.union(getNetworkLearningEncoderParams());
		
		Network network =  Network.create("Network API Demo", params)
							.add(Network.createRegion("Region 1")
							.add(Network.createLayer("Layer 2/3", params)
							.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
							.add(Anomaly.create())
							.add(new TemporalMemory())
							.add(new SpatialPooler())
							.add(sensor)));
		
//		network = Network.create("Network API Demo", params)
//		        .add(Network.createRegion("Region 1")
//		                .add(Network.createLayer("Layer 2/3", params)
//		                    .alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
//		                    .add(Anomaly.create())
//		                    .add(new TemporalMemory())
//		                    .add(new SpatialPooler())
//		                    .add(Sensor.create(FileSensor::create, SensorParams.create(
//		                        Keys::path, "", ResourceLocator.path("/Users/sahiltyagi/Desktop/sample.csv"))))));
		
		return network;
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
	
	public static Map<String, Class<? extends Classifier>> getInferredFieldsMap(
            String field, Class<? extends Classifier> classifier) {
        Map<String, Class<? extends Classifier>> inferredFieldsMap = new HashMap<>();
        inferredFieldsMap.put(field, classifier);
        return inferredFieldsMap;
    }
	
	public static Map<String, Map<String, Object>> getNetworkDemoFieldEncodingMap() {
		System.out.println("##################### in getNetworkDemoFieldEncodingMap() method ###################");
//        Map<String, Map<String, Object>> fieldEncodings = setupMap(
//                null,
//                0, // n
//                0, // w
//                0, 0, 0, 0, null, null, null,
//                "timestamp", "datetime", "DateEncoder");
//        fieldEncodings = setupMap(
//                fieldEncodings, 
//                50, 
//                21, 
//                -50, 300, 0, 0.1, null, Boolean.TRUE, null, 
//                "consumption", "float", "ScalarEncoder");
		
		 Map<String, Map<String, Object>> fieldEncodings = setupMap(
	                null, 
	                50, 
	                21, 
	                -50, 300, 0, 0.1, null, Boolean.TRUE, null, 
	                "consumption", "float", "ScalarEncoder");
        
//        fieldEncodings.get("timestamp").put(KEY.DATEFIELD_TOFD.getFieldName(), new Tuple(21,9.5)); // Time of day
//        fieldEncodings.get("timestamp").put(KEY.DATEFIELD_PATTERN.getFieldName(), "MM/dd/YY HH:mm:ss.SSS");
        
        return fieldEncodings;
    }
	
	 /**
     * Sets up an Encoder Mapping of configurable values.
     *  
     * @param map               if called more than once to set up encoders for more
     *                          than one field, this should be the map itself returned
     *                          from the first call to {@code #setupMap(Map, int, int, double, 
     *                          double, double, double, Boolean, Boolean, Boolean, String, String, String)}
     * @param n                 the total number of bits in the output encoding
     * @param w                 the number of bits to use in the representation
     * @param min               the minimum value (if known i.e. for the ScalarEncoder)
     * @param max               the maximum value (if known i.e. for the ScalarEncoder)
     * @param radius            see {@link Encoder}
     * @param resolution        see {@link Encoder}
     * @param periodic          such as hours of the day or days of the week, which repeat in cycles
     * @param clip              whether the outliers should be clipped to the min and max values
     * @param forced            use the implied or explicitly stated ratio of w to n bits rather than the "suggested" number
     * @param fieldName         the name of the encoded field
     * @param fieldType         the data type of the field
     * @param encoderType       the Camel case class name minus the .class suffix
     * @return
     */
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
    
    public static Publisher getPublisher() {
//		Publisher manualPublisher = Publisher.builder()
//								.addHeader("timestamp,consumption")
//								.addHeader("datetime,float")
//								.addHeader("B")
//								.build();
    	
    	Publisher manualPublisher = Publisher.builder()
				.addHeader("consumption")
				.addHeader("float")
				.addHeader("B")
				.build();
		
		return manualPublisher;
    }
    
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
            @Override public void onNext(Inference i) { writeToFileAnomalyOnly(i, "consumption", pw); }
        };
    }
    
    private static void writeToFileAnomalyOnly(Inference infer, String classifierField, PrintWriter pw) {
        try {
            if(infer.getRecordNum() > 0) {
                double actual = (Double)infer.getClassifierInput()
                        .get(classifierField).get("inputValue");
                //double error = Math.abs(predictedValue - actual);
                StringBuilder sb = new StringBuilder()
                        .append(infer.getRecordNum()).append(", ")
                                //.append("classifier input=")
                        .append(String.format("%3.2f", actual)).append(", ")
                                //.append("prediction= ")
                    //    .append(String.format("%3.2f", predictedValue)).append(", ")
                      //  .append(String.format("%3.2f", error)).append(", ")
                                //.append("anomaly score=")
                        .append(infer.getAnomalyScore());
                pw.println(sb.toString());
                pw.flush();
                System.out.println(sb.toString());
            } else {

            }
           // predictedValue = newPrediction;
        }catch(Exception e) {
            e.printStackTrace();
            pw.flush();
        }

    }
}
