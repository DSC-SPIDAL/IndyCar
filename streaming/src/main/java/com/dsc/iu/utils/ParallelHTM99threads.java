package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.AnomalyLikelihood;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.PublisherSupplier;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.util.Tuple;
import org.numenta.nupic.util.UniversalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class ParallelHTM99threads {
	static List<String> carlist = new ArrayList<String>();
	public static Map<String, Double> prev_lhood_thd = new HashMap<String, Double>();
	
	public static void main(String[] args) {
		carlist.add("20");carlist.add("21");carlist.add("13");carlist.add("98");carlist.add("19");carlist.add("6");carlist.add("33");carlist.add("24");carlist.add("26");carlist.add("7");carlist.add("60");carlist.add("27");
		carlist.add("22");carlist.add("18");carlist.add("3");carlist.add("4");carlist.add("28");carlist.add("32");carlist.add("59");carlist.add("25");carlist.add("64");carlist.add("10");carlist.add("15");carlist.add("17");
		carlist.add("12");carlist.add("1");carlist.add("9");carlist.add("14");carlist.add("23");carlist.add("30");carlist.add("29");carlist.add("88");carlist.add("66");
		
		String[] metrics = {"speed", "RPM", "throttle"};
		int threadnum=0;
		for(int j=0; j<metrics.length; j++) {
			for(int i=0; i<carlist.size(); i++) {
				prev_lhood_thd.put(carlist.get(i)+"_"+metrics[j], 0.0);
				threadnum++;
				runHTM(carlist.get(i), metrics[j], threadnum);
			}
		}
	}
	
	public static Parameters getModelParameters(JsonNode params) {
        JsonNode modelParams = params.path("modelParams");
        Parameters p = Parameters.getAllDefaultParameters()
            .union(getSpatialPoolerParams(modelParams))
            .union(getTemporalMemoryParams(modelParams))
            .union(getSensorParams(modelParams));
        
        // TODO https://github.com/numenta/htm.java/issues/482
        // if (spParams.has("seed")) {
        //     p.set(KEY.SEED, spParams.get("seed").asInt());
        // }
        p.set(KEY.RANDOM, new UniversalRandom(42));
        // Setting the random above is done as a work-around to this.
        //p.set(KEY.SEED, 42);
        return p;
    }
	
	public static Parameters getSpatialPoolerParams(JsonNode modelParams) {
        Parameters p = Parameters.getSpatialDefaultParameters();
        JsonNode spParams = modelParams.path("spParams");
        if (spParams.has("columnCount")) {
            p.set(KEY.COLUMN_DIMENSIONS, new int[]{spParams.get("columnCount").asInt()});
        }
        if (spParams.has("maxBoost")) {
            p.set(KEY.MAX_BOOST, spParams.get("maxBoost").asDouble());
        }
        if (spParams.has("synPermInactiveDec")) {
            p.set(KEY.SYN_PERM_INACTIVE_DEC, spParams.get("synPermInactiveDec").asDouble());
        }
        if (spParams.has("synPermConnected")) {
            p.set(KEY.SYN_PERM_CONNECTED, spParams.get("synPermConnected").asDouble());
        }
        if (spParams.has("synPermActiveInc")) {
            p.set(KEY.SYN_PERM_ACTIVE_INC, spParams.get("synPermActiveInc").asDouble());
        }
        if (spParams.has("numActiveColumnsPerInhArea")) {
            p.set(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, spParams.get("numActiveColumnsPerInhArea").asDouble());
        }
        if (spParams.has("globalInhibition")) {
            p.set(KEY.GLOBAL_INHIBITION, spParams.get("globalInhibition").asBoolean());
        }
        if (spParams.has("potentialPct")) {
            p.set(KEY.POTENTIAL_PCT, spParams.get("potentialPct").asDouble());
        }

        return p;
    }
	
	public static Parameters getTemporalMemoryParams(JsonNode modelParams) {
        Parameters p = Parameters.getTemporalDefaultParameters();
        JsonNode tpParams = modelParams.path("tpParams");
        if (tpParams.has("columnCount")) {
            p.set(KEY.COLUMN_DIMENSIONS, new int[]{tpParams.get("columnCount").asInt()});
        }
        if (tpParams.has("activationThreshold")) {
            p.set(KEY.ACTIVATION_THRESHOLD, tpParams.get("activationThreshold").asInt());
        }
        if (tpParams.has("cellsPerColumn")) {
            p.set(KEY.CELLS_PER_COLUMN, tpParams.get("cellsPerColumn").asInt());
        }
        if (tpParams.has("permanenceInc")) {
            p.set(KEY.PERMANENCE_INCREMENT, tpParams.get("permanenceInc").asDouble());
        }
        if (tpParams.has("minThreshold")) {
            p.set(KEY.MIN_THRESHOLD, tpParams.get("minThreshold").asInt());
        }
        if (tpParams.has("initialPerm")) {
            p.set(KEY.INITIAL_PERMANENCE, tpParams.get("initialPerm").asDouble());
        }
        if(tpParams.has("maxSegmentsPerCell")) {
            p.set(KEY.MAX_SEGMENTS_PER_CELL, tpParams.get("maxSegmentsPerCell").asInt());
        }
        if(tpParams.has("maxSynapsesPerSegment")) {
            p.set(KEY.MAX_SYNAPSES_PER_SEGMENT, tpParams.get("maxSynapsesPerSegment").asInt());
        }
        if (tpParams.has("permanenceDec")) {
            p.set(KEY.PERMANENCE_DECREMENT, tpParams.get("permanenceDec").asDouble());
        }
        if (tpParams.has("predictedSegmentDecrement")) {
            p.set(KEY.PREDICTED_SEGMENT_DECREMENT, tpParams.get("predictedSegmentDecrement").asDouble());
        }
        if (tpParams.has("newSynapseCount")) {
            p.set(KEY.MAX_NEW_SYNAPSE_COUNT, tpParams.get("newSynapseCount").intValue());
        }

        return p;
    }
	
	public static Parameters getSensorParams(JsonNode modelParams) {
	        JsonNode sensorParams = modelParams.path("sensorParams");
	        Map<String, Map<String, Object>> fieldEncodings = getFieldEncodingMap(sensorParams);
	        Parameters p = Parameters.empty();
	        p.set(KEY.CLIP_INPUT, true);
	        p.set(KEY.FIELD_ENCODING_MAP, fieldEncodings);

	        return p;
	}
	
	public static Map<String, Map<String, Object>> getFieldEncodingMap(JsonNode modelParams) {
        Map<String, Map<String, Object>> fieldEncodings = new HashMap<>();
        String fieldName;
        Map<String, Object> fieldMap;
        JsonNode encoders = modelParams.path("encoders");
        for (JsonNode node : encoders) {
            if (node.isNull())
                continue;

            fieldName = node.path("fieldname").textValue();
            fieldMap = fieldEncodings.get(fieldName);
            if (fieldMap == null) {
                fieldMap = new HashMap<>();
                fieldMap.put("fieldName", fieldName);
                fieldEncodings.put(fieldName, fieldMap);
            }
            fieldMap.put("encoderType", node.path("type").textValue());
            if (node.has("timeOfDay")) {
                JsonNode timeOfDay = node.get("timeOfDay");
                fieldMap.put("fieldType", "datetime");
                //fieldMap.put(KEY.DATEFIELD_PATTERN.getFieldName(), "YYYY-MM-dd HH:mm:ss.SSS");
                fieldMap.put(KEY.DATEFIELD_PATTERN.getFieldName(), "YYYY-MM-dd HH:mm:ss");
                fieldMap.put(KEY.DATEFIELD_TOFD.getFieldName(),
                        new Tuple(timeOfDay.get(0).asInt(), timeOfDay.get(1).asDouble()));
            } else {
                fieldMap.put("fieldType", "float");
            }
            if (node.has("resolution")) {
                fieldMap.put("resolution", node.get("resolution").asDouble());
            }
        }
        
        return fieldEncodings;
    }
	
	private static void runHTM(String carnum, String metric, int threadnum) {
		new Thread("thread-"+threadnum+"-for car-"+carnum) {
			public void run() {
				System.out.println("thread name:"+Thread.currentThread().getName());
				String arg1 = "{\"input\":\"/Users/sahiltyagi/Desktop/numenta_indy2018-13-vspeed.csv\", \"output\":\"/Users/sahiltyagi/Desktop/javaNAB.csv\", "
						+ "\"aggregationInfo\": {\"seconds\": 0, \"fields\": [], \"months\": 0, \"days\": 0, \"years\": 0, \"hours\": 0, \"microseconds\": 0, "
						+ "\"weeks\": 0, \"minutes\": 0, \"milliseconds\": 0}, \"model\": \"HTMPrediction\", \"version\": 1, \"predictAheadTime\": null, "
						+ "\"modelParams\": {\"sensorParams\": {\"sensorAutoReset\": null, \"encoders\": {\"value\": {\"name\": \"value\", "
						+ "\"resolution\": 2.5143999999999997, \"seed\": 42, \"fieldname\": \"value\", \"type\": \"RandomDistributedScalarEncoder\"}, "
						+ "\"timestamp_dayOfWeek\": null, \"timestamp_timeOfDay\": {\"fieldname\": \"timestamp\", \"timeOfDay\": [21, 9.49], "
						+ "\"type\": \"DateEncoder\", \"name\": \"timestamp\"}, \"timestamp_weekend\": null}, \"verbosity\": 0}, "
						+ "\"anomalyParams\": {\"anomalyCacheRecords\": null, \"autoDetectThreshold\": null, \"autoDetectWaitRecords\": 5030}, "
						+ "\"spParams\": {\"columnCount\": 2048, \"synPermInactiveDec\": 0.0005, \"spatialImp\": \"cpp\", \"inputWidth\": 0, \"spVerbosity\": 0, "
						+ "\"synPermConnected\": 0.2, \"synPermActiveInc\": 0.003, \"potentialPct\": 0.8, \"numActiveColumnsPerInhArea\": 40, \"boostStrength\": 0.0, "
						+ "\"globalInhibition\": 1, \"seed\": 1956}, \"trainSPNetOnlyIfRequested\": false, \"clParams\": {\"alpha\": 0.035828933612158, "
						+ "\"verbosity\": 0, \"steps\": \"1\", \"regionName\": \"SDRClassifierRegion\"}, \"tmParams\": {\"columnCount\": 2048, "
						+ "\"activationThreshold\": 20, \"cellsPerColumn\": 32, \"permanenceDec\": 0.008, \"minThreshold\": 13, \"inputWidth\": 2048, "
						+ "\"maxSynapsesPerSegment\": 128, \"outputType\": \"normal\", \"initialPerm\": 0.24, \"globalDecay\": 0.0, \"maxAge\": 0, "
						+ "\"newSynapseCount\": 31, \"maxSegmentsPerCell\": 128, \"permanenceInc\": 0.04, \"temporalImp\": \"tm_cpp\", \"seed\": 1960, "
						+ "\"verbosity\": 0, \"predictedSegmentDecrement\": 0.001}, \"tmEnable\": true, \"clEnable\": false, \"spEnable\": true, "
						+ "\"inferenceType\": \"TemporalAnomaly\"}}";
				
				
	            // Parse command line args
	            OptionParser parser = new OptionParser();
	            parser.nonOptions("OPF parameters object (JSON)");
	            parser.acceptsAll(Arrays.asList("p", "params"), "OPF parameters file (JSON).\n(default: first non-option argument)")
	                .withOptionalArg()
	                .ofType(File.class);
	            parser.acceptsAll(Arrays.asList("i", "input"), "Input data file (csv).\n(default: stdin)")
	                .withOptionalArg()
	                .ofType(File.class);
	            parser.acceptsAll(Arrays.asList("o", "output"), "Output results file (csv).\n(default: stdout)")
	                .withOptionalArg()
	                .ofType(File.class);
	            parser.acceptsAll(Arrays.asList("s", "skip"), "Header lines to skip")
	                .withOptionalArg()
	                .ofType(Integer.class)
	                .defaultsTo(0);
	            parser.acceptsAll(Arrays.asList("h", "?", "help"), "Help");
	            OptionSet options = parser.parse(arg1);
	            
	            try {
	            		FileInputStream inp = new FileInputStream(new File("/scratch_ssd/sahil/IPBroadcaster_Input_2018-05-27_0.log"));
	            		JsonNode params;
	                    ObjectMapper mapper = new ObjectMapper();
	                    if (options.has("p")) {
	                        params = mapper.readTree((File)options.valueOf("p"));
	                    } 
	                    else if (options.nonOptionArguments().isEmpty()) {
	                        try { inp.close(); }catch(Exception ignore) {}
	                        if(options.has("o")) {
	                            try {}catch(Exception ignore) {}
	                        }
	                        throw new IllegalArgumentException("Expecting OPF parameters. See 'help' for more information");
	                    } else {
	                        params = mapper.readTree((String)options.nonOptionArguments().get(0));
	                    }
	                    
	                    int skip = (int) options.valueOf("s");

	                    // Force timezone to UTC
	                 DateTimeZone.setDefault(DateTimeZone.UTC);
	                 AnomalyLikelihood likelihood = new AnomalyLikelihood(true, 8640, false, 375, 375);
	                  
	                 System.out.println("going to start htm..");
	                 //HTMSahil model = new HTMSahil(params);
	                 
	                 PublisherSupplier supplier = PublisherSupplier.builder()
	                         .addHeader("timestamp,value")
	                         .addHeader("datetime,float")
	                         .addHeader("T,B")
	                         .build();

	                 // Get updated model parameters
	                 Parameters parameters = getModelParameters(params);

	                 // Create NAB Network
	                 Network network = Network.create("NAB Network", parameters)
	                     .add(Network.createRegion("NAB Region")
	                         .add(Network.createLayer("NAB Layer", parameters)
	                             .add(Anomaly.create())
	                             .add(new TemporalMemory())
	                             .add(new SpatialPooler())
	                             .add(Sensor.create(ObservableSensor::create,
	                                     SensorParams.create(SensorParams.Keys::obs, "Manual Input", supplier)))));
	                 
	                 network.observe().subscribe((Inference inference) -> {
	                     double score = inference.getAnomalyScore();
	                     int record = inference.getRecordNum();
	                     double value = (Double)inference.getClassifierInput().get("value").get("inputValue");
	                     DateTime timestamp = (DateTime)inference.getClassifierInput().get("timestamp").get("inputValue");
	                     
	                     
	                     double anomaly_likelihood = likelihood.anomalyProbability(value, score, timestamp);
	                     double prev_likelihood = prev_lhood_thd.get(carnum+"_"+metric);
	                     if (anomaly_likelihood >=0.99999 && prev_likelihood >= 0.99999){
	                         prev_likelihood = anomaly_likelihood;
	                         anomaly_likelihood = 0.999;
	                     }
	                     else{
	                         prev_likelihood = anomaly_likelihood;
	                     }
	                     
	                     prev_lhood_thd.put(carnum+"_"+metric, prev_likelihood);
	                     double logscore = AnomalyLikelihood.computeLogLikelihood(anomaly_likelihood);
	                     
	                 }, (error) -> {
	                     
	                 }, () -> {
	                     
	                    
	                 });
	                 network.start();
	                 Publisher publisher = supplier.get();
	                 BufferedReader rdr = new BufferedReader(new InputStreamReader(inp));
	                 String line;
	                 while((line=rdr.readLine()) != null) {
	                	 	if(line.startsWith("$P") && line.split("�")[2].matches("\\d+:\\d+:\\d+.\\d+") && line.split("�")[1].equalsIgnoreCase(carnum)) {
	                	 		publisher.onNext("2018-05-27 " + line.split("�")[2] + "," + line.split("�")[4]);
	                	 		try {
	                	 			Thread.sleep(10);
	                	 		} catch(InterruptedException ex) {}
	                	 	}
	                 }
	                 
	                 publisher.onComplete();
	                 System.out.println("completed publishing data for car-" + carnum + " and metric: " + metric);
	                 
	            } catch(IOException e) {
	            		e.printStackTrace();
	            }
			}
		}.start();
	}
}
