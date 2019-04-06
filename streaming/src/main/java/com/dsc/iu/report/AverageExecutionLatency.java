package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/*
 * calculates the average latency per record basis over multiple runs. O/P is the average
 * execution latency file across every record index averaged over multiple runs
 * 
 * */
public class AverageExecutionLatency {
	public static Map<Integer, Double> latencymap = new HashMap<Integer, Double>();
	
	public static void main(String[] args) {
		try {
			File dir = new File("/Users/sahiltyagi/Desktop/batch_HDD/");
			if(dir.isDirectory()) {
				File[] run_dirs = dir.listFiles();
				for(File run : run_dirs) {
					if(run.isDirectory()) {
						File[] inner_files = run.listFiles();
						for(File out_csv : inner_files) {
							if(out_csv.getName().equals("out.csv")) {
								System.out.println("going to read file:" + out_csv.getAbsolutePath());
								BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(out_csv.getAbsolutePath())));
								String line;
								while((line = rdr.readLine()) != null) {
									int record_index = Integer.parseInt(line.split(",")[0]);
									double execution_ts = Double.parseDouble(line.split(",")[1]);
									
									if(!latencymap.containsKey(record_index)) {
										latencymap.put(record_index, execution_ts);
									} else {
										latencymap.put(record_index, latencymap.get(record_index) + execution_ts);
									}
									
								}
								
								rdr.close();
							}
						}
					}
				}
			}
			
			System.out.println("for index 10:" + latencymap.get(10)/6);
			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Desktop/out.csv")));
			for(Map.Entry<Integer, Double> entry : latencymap.entrySet()) {
				//System.out.println(entry.getKey() + "," + (entry.getValue()/6));
				wrtr.write(entry.getKey() + "," + Double.valueOf(entry.getValue()/6) + "\n");
			}
			
			wrtr.close();
			System.out.println("completed AverageExecutionLatency!");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
