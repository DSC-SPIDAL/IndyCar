package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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

import rx.Subscriber;
import com.dsc.iu.utils.OnlineLearningUtils;

/*
 * sample class using same logic as Sample.java, with few edits to align well with streaming HTM application case.
 * */
public class BatchHTMExecution {
	public Network network;
	public Publisher manualpublish;
	static BatchHTMExecution batchHTM;
	
	public static void main(String[] args) {
		long startTS = System.currentTimeMillis();
		batchHTM = new BatchHTMExecution();
		batchHTM.runHTMNetwork();
		batchHTM.explicitFileRead();
		System.out.println("completed running HTM code");
		System.out.println(System.currentTimeMillis() - startTS + " milliseconds");
		
	}
	
	private void runHTMNetwork() {
		//create a basic network here
		try {
			manualpublish = OnlineLearningUtils.getPublisher();
			Sensor<ObservableSensor<String[]>> sensor = Sensor.create(
	        	     ObservableSensor::create, 
	        	         SensorParams.create(
	        	             Keys::obs, new Object[] { "kakkerot", manualpublish }));
			
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
			
			File f = new File("/N/u/styagi/htmsample.txt");
			PrintWriter htmsample = new PrintWriter(f);
			
			network.observe().subscribe(getSubscriber(htmsample));
			network.start();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	Subscriber<Inference> getSubscriber(PrintWriter pw) {
        return new Subscriber<Inference>() {
            @Override public void onCompleted() {
                System.out.println("\nstream completed. see output");
            }
            @Override public void onError(Throwable e) { e.printStackTrace(); }
            @Override public void onNext(Inference infer) {
            		try {
            			if(infer.getRecordNum() > 0) {
                        double actual = (Double)infer.getClassifierInput().get("consumption").get("inputValue");
                        pw.println(infer.getRecordNum() + "," + actual + "," + infer.getAnomalyScore() + "," + System.currentTimeMillis());
                        pw.flush();
            			}
            		} catch(Exception e) {
                    e.printStackTrace();
            		}
            }
        };
    }
	
	private void explicitFileRead() {
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/N/u/styagi/dixon_indycar.log")));
			
			File f = new File("/N/u/styagi/executionTime.txt");
			PrintWriter executiontime = new PrintWriter(f);
			String line; int index=0;
			manualpublish.onNext("5/28/17 16:05:54.260,0");
			while((line=rdr.readLine()) != null) {
				index++;
				executiontime.println(index + "," + line.split(",")[1] + "," + System.currentTimeMillis());
				manualpublish.onNext(line.trim());
				
				//10 msg/sec
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			executiontime.close();
			rdr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}