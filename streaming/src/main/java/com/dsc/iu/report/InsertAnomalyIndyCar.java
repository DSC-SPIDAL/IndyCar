package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
 * inserts anomalous data points at random indexes for a fixed seed value in indycar data corresponding to a particular car. 
 * The fraction of anomaly set to 5% => 850 points in 17000 indycar #9 dataset
 * */
public class InsertAnomalyIndyCar {
	
	public static void main(String[] args) {
		try {
			Random seed = new Random(7);
			BufferedReader rdr;
			String line;
			Map<Integer, String> anomalydatamap = new HashMap<Integer, String>();
			StringBuilder strbldr = new StringBuilder();
			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/Users/sahiltyagi/Desktop/injected_anomaly.log"))));
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/sahiltyagi/Desktop/dixon_speed_nonzeroinput.log"))));
			while((line=rdr.readLine()) != null) {
				strbldr.append(line).append("\n");
			}
			
			strbldr = new StringBuilder(strbldr.toString().substring(0, strbldr.toString().length() -1));
			wrtr.write(strbldr.toString());
			wrtr.close();
			rdr.close();
			
			for(int i=0; i<345; i++) {
				int record_num = seed.nextInt(20754);
				rdr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/sahiltyagi/Desktop/injected_anomaly.log"))));
				if(anomalydatamap.containsKey(record_num)) {
					record_num = seed.nextInt(20754);
				}
				
				//System.out.println("record index chosen is:"+record_num);
				for(int j=0; j<(record_num-1); j++) {
					line = rdr.readLine();
				}
				
				line = rdr.readLine();
				String time = line.split(",")[0];
				//System.out.println("anomalous record at index " + i + ":" + time + "," + "50.000" +"\n");
//				if(anomalydatamap.containsKey(record_num)) {
//					System.out.println("duplicate key present:"+record_num);
//				} else {
//					anomalydatamap.put(record_num, time + "," + "0.000" +"\n");
//				}
				
				if(anomalydatamap.containsKey(record_num)) {
					System.out.println("duplicate key present:"+record_num);
				} else {
					double scalar_input = ThreadLocalRandom.current().nextDouble(-50.000, 20.000);
					//System.out.println("input is:"+scalar_input);
					System.out.println(time.split(" ")[1]);
					anomalydatamap.put(record_num, time + "," + scalar_input +"\n");
				}
				rdr.close();
			}
			
			System.out.println("random  points generated:"+anomalydatamap.size());
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/sahiltyagi/Desktop/injected_anomaly.log"))));
			wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/Users/sahiltyagi/Desktop/anomalyInject1.log"))));
			int index=0;
			while((line = rdr.readLine()) != null) {
				index++;
				wrtr.write(line+"\n");
				if(anomalydatamap.containsKey(index)) {
					wrtr.write(anomalydatamap.get(index));
				}
			}
			rdr.close();
			wrtr.close();
			
			System.out.println("end InsertAnomalyIndyCar");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
