package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

//generates one log file fir each car in indy500
//runs anomaly detection on one metric for a car, and all cars run in 33 separate threads.
public class SequentialHTM33cars {
	static List<String> carlist = new ArrayList<String>();
	
	public static void main(String[] args) {
		carlist.add("20");carlist.add("21");carlist.add("13");carlist.add("98");carlist.add("19");carlist.add("6");carlist.add("33");carlist.add("24");carlist.add("26");carlist.add("7");carlist.add("60");carlist.add("27");
		carlist.add("22");carlist.add("18");carlist.add("3");carlist.add("4");carlist.add("28");carlist.add("32");carlist.add("59");carlist.add("25");carlist.add("64");carlist.add("10");carlist.add("15");carlist.add("17");
		carlist.add("12");carlist.add("1");carlist.add("9");carlist.add("14");carlist.add("23");carlist.add("30");carlist.add("29");carlist.add("88");carlist.add("66");
		
		String[] metrics = {"speed", "RPM", "throttle"};
		Map<String, Double> minvalues = new HashMap<String, Double>();
		minvalues.put("speed", -20.0);
		minvalues.put("RPM", -3000.0);
		minvalues.put("throttle", 0.0);
		
		Map<String, Double> maxvalues = new HashMap<String, Double>();
		maxvalues.put("speed", 250.0);
		maxvalues.put("RPM", 15000.0);
		maxvalues.put("throttle", 30.0);
		
		Map<String, Integer> indexfrominputfile = new HashMap<String, Integer>();
		indexfrominputfile.put("speed", 1);
		indexfrominputfile.put("RPM", 2);
		indexfrominputfile.put("throttle", 3);
		
		SequentialHTM33cars seqobj = new SequentialHTM33cars();
//		seqobj.generateSingleCarFiles();
		
		for(int j=0; j<metrics.length; j++) {
			for(int i=0; i<carlist.size(); i++) {
				System.out.println("going to launch thread " + i + " for car " + carlist.get(i));
				seqobj.runAnomalyDetectionThreads(carlist.get(i), i, metrics[j], minvalues.get(metrics[j]), maxvalues.get(metrics[j]), indexfrominputfile.get(metrics[j]));
			}
		}
		
//		seqobj.getFirstZerosFilteredData();
//		seqobj.htmstudioRavifiles();
		
		System.out.println("completed sequential htm for 33 threads");
	}
	
	private void htmstudioRavifiles() {
		try {
			Iterator<String> itr = carlist.iterator();
			while(itr.hasNext()) {
//				File erpfile = new File("D:\\Indycar2018\\IPBroadcaster_Input_2018-05-27_0.log");
				File erpfile = new File("/scratch_ssd/sahil/IPBroadcaster_Input_2018-05-27_0.log");
				BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(erpfile)));
				String record;
				String carnum = itr.next();
				File f = new File("D:\\ravi_htmstudio\\inputcar-"+carnum.trim()+".csv");
				PrintWriter pw = new PrintWriter(f);
				System.out.println("generating file for car-"+carnum);
				while((record=rdr.readLine()) != null) {
					if(record.startsWith("$P") && record.split("�")[2].length() >9 && record.split("�")[1].equals(carnum)) {
						pw.println("2018-05-27 " + record.split("�")[2] + "," + record.split("�")[4]);
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void generateSingleCarFiles() {
		try {
			Iterator<String> itr = carlist.iterator();
			while(itr.hasNext()) {
				File erpfile = new File("/scratch_ssd/sahil/IPBroadcaster_Input_2018-05-27_0.log");
//				File erpfile = new File("D:\\Indycar2018\\IPBroadcaster_Input_2018-05-27_0.log");
				BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(erpfile)));
				String record;
				String carnum = itr.next();
				File f = new File("/scratch_ssd/sahil/cars2018data/inputcar-"+carnum.trim()+".csv");
				System.out.println("generating file for car-"+carnum);
//				File f = new File("D:\\cars2018data\\inputcar-"+carnum.trim()+".csv");
				PrintWriter pw = new PrintWriter(f);
				while((record=rdr.readLine()) != null) {
//					if(record.startsWith("$P") && record.split("\\u00A6")[2].length() >9 && record.split("\\u00A6")[1].equals(carnum)) {
//						pw.println(carnum + "," + record.split("\\u00A6")[4] + "," + record.split("\\u00A6")[5] + "," + record.split("\\u00A6")[6]);
//						//pw.println(carnum + "," + record.split("\\u00A6")[4]);
//					}
					
					if(record.startsWith("$P") && record.split("�")[2].length() >9 && record.split("�")[1].equals(carnum)) {
						pw.println(carnum + "," + record.split("�")[4] + "," + record.split("�")[5] + "," + record.split("�")[6]);
					}
				}
				
				pw.close();
				rdr.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void runAnomalyDetectionThreads(String carnum, int threadnum, String metric, double min, double max, int indextofetch) {
			new Thread("thread-"+threadnum+"-forcar-"+carnum) {
				public void run() {
					System.out.println("thread name:"+Thread.currentThread().getName());
					Publisher manualPublisher = Publisher.builder().addHeader(metric).addHeader("float").addHeader("B").build();
					Sensor<ObservableSensor<String[]>> sensor = Sensor.create(ObservableSensor::create, SensorParams.create(Keys::obs, new Object[] { "seq33threadsexecution", manualPublisher }));
					Parameters params = getParams();
					//Parameters params = Parameters.getEncoderDefaultParameters();
					params = params.union(getNetworkLearningEncoderParams(metric, min, max));
					Network network = Network.create("single_metric_33threads", params)
									.add(Network.createRegion("region1")
									.add(Network.createLayer("layer2/3", params)
									.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
									.add(Anomaly.create())
									.add(new TemporalMemory())
									.add(new SpatialPooler())
									.add(sensor)));
					
					File output = new File("/scratch_ssd/sahil/output2018/anomalyscore-" + carnum + "_" + metric +".csv");
//					File output = new File("D:\\scores2018\\anomalyscore-"+carnum+".csv");
					try {
						PrintWriter pw = new PrintWriter(new FileWriter(output));
						network.observe().subscribe(getSubscriber(output, pw, metric));
						
						network.start();
//						File f = new File("D:\\cars2018data\\inputcar-"+carnum+".csv");
						File f = new File("/scratch_ssd/sahil/cars2018data/inputcar-"+carnum+".csv");
						BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
						String record;
						boolean firstnonzero=false;
						manualPublisher.onNext("4.000");
						while((record=rdr.readLine()) != null) {
							
							if(Double.parseDouble(record.split(",")[indextofetch]) > 0) {
								firstnonzero=true;
							}
							
							if(firstnonzero) {
								manualPublisher.onNext(record.split(",")[indextofetch]);
								//input processing rate limited to 500 messages per second
								try {
									Thread.sleep(2);
								} catch(InterruptedException i) {
									i.printStackTrace();
								}
							}
						}
						rdr.close();
						
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
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
	
	private static Subscriber<Inference> getSubscriber(File outputFile, PrintWriter pw, String metric) {
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
            		writeToFileAnomaly(i, metric, pw); 
            }
        };
    }
	
	private static void writeToFileAnomaly(Inference infer, String classifierField, PrintWriter pw) {
		if(infer.getRecordNum() > 0) {
			double actual_val = (Double)infer.getClassifierInput().get(classifierField).get("inputValue");
			 StringBuilder sb = new StringBuilder().append(infer.getRecordNum()).append(",").append(String.format("%3.2f", actual_val)).append(",").append(infer.getAnomalyScore()).append(",")
                     			.append(System.currentTimeMillis());
             pw.println(sb.toString());
             pw.flush();
		}
	}
	
	private void getFirstZerosFilteredData() {
		try {
			Iterator<String> itr = carlist.iterator();
			while(itr.hasNext()) {
				String carnum = itr.next();
				File f = new File("D:\\cars2018\\inputcar-"+carnum+".csv");
				System.out.println("running for file:"+carnum);
				BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String record;
				boolean zeroflag=false;
				File f2 = new File("D:\\firstzerosremoved\\car-"+carnum+".csv");
				PrintWriter pw = new PrintWriter(f2);
				while((record=rdr.readLine()) != null) {
					if(Double.parseDouble(record.split(",")[1]) > 0 && !zeroflag) {
						zeroflag = true;
					}
						
					if(zeroflag) {
						pw.println(record.split(",")[0] + "," + record.split(",")[1]);
					}
				}
				
				rdr.close();
				pw.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}