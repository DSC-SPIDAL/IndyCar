package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class HTMSequentialLatency {
	public static void main(String[] args) throws IOException {
		PrintWriter pw=null;
		File f2 = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/speed-13/score_executiontime-13.csv");
		try {
			pw = new PrintWriter(f2);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		//HashMap<Long, Long> datamap = new HashMap<Long, Long>();
		HashMap<Long, Long> infermap = new HashMap<Long, Long>();
		HashMap<Long, Double> anomalyscore = new HashMap<Long, Double>();
		HashMap<Long, String> timemap = new HashMap<Long, String>();
		HashMap<Long, Double> speedmap = new HashMap<Long, Double>();
		HashMap<Long, Long> datatime = new HashMap<Long, Long>();
		HashMap<Long, Long> inferencetime = new HashMap<Long, Long>();
		File f = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/speed-13/data-13-speed.csv");
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String line;
		while((line=rdr.readLine()) != null) {
			//contains counter and timestamp
			//datamap.put(Long.parseLong(line.split(",")[2]), Long.parseLong(line.split(",")[1]));
			//counter and timeofday values
			
			timemap.put(Long.parseLong(line.split(",")[2]), line.split(",")[0].split(" ")[1]);
			datatime.put(Long.parseLong(line.split(",")[2]), Long.parseLong(line.split(",")[3]));
		}
		rdr.close();
		
		f = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/speed-13/inference-13-speed.csv");
		rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		line=null;
		while((line=rdr.readLine()) != null) {
			if(line.split(",").length == 6) {
				anomalyscore.put(Long.parseLong(line.split(",")[1]), Double.parseDouble(line.split(",")[4]));
				infermap.put(Long.parseLong(line.split(",")[1]), Long.parseLong(line.split(",")[5]));
				speedmap.put(Long.parseLong(line.split(",")[1]), Double.parseDouble(line.split(",")[3]));
				inferencetime.put(Long.parseLong(line.split(",")[1]), Long.parseLong(line.split(",")[5]));
			}
		}
		rdr.close();
		
		for(Map.Entry<Long, Long> set: infermap.entrySet()) {
			long key = set.getKey();
			System.out.println("key:" + key);
//			pw.println(key + "," + speedmap.get(key) + "," + (set.getValue() - datamap.get(key)) + "," + timemap.get(key) + "," + anomalyscore.get(key));
//			pw.flush();
			pw.println(key + "," + speedmap.get(key) + "," + timemap.get(key) + "," + anomalyscore.get(key) + "," + (inferencetime.get(key) - datatime.get(key)));
			pw.flush();
		}
		pw.close();
		System.out.println("complete.");
		
//		averagelatency(3);
		
//		jiayufile();
	}
	
	private static void jiayufile() {
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(
					new FileInputStream("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/-100to300range/car13/HTMseq_final.csv")));
			File f = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/-100to300range/car13/car13_jiayu.csv");
			PrintWriter pw = new PrintWriter(f);
			String line;
			while((line=rdr.readLine()) != null) {
				pw.println(line.split(",")[2] + "," + line.split(",")[4]);
			}
			rdr.close();
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void averagelatency(int numofexp) throws IOException {
		Map<Integer, Double> totaltime = new HashMap<Integer, Double>();
		Map<Integer, Double> anomalyscore = new HashMap<Integer, Double>();
		Map<Integer, Double> speedmap = new HashMap<Integer, Double>();
		Map<Integer, String> timeofday = new HashMap<Integer, String>();
		File dir1 = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/clean_env/car13/");
		if(dir1.isDirectory()) {
			File[] dirs = dir1.listFiles();
			for(File f: dirs) {
				if(f.isDirectory()) {
					File[] f2 = f.listFiles();
					for(File f3 : f2) {
						if(f3.getName().equalsIgnoreCase("HTMseqTime.csv")) {
							BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f3)));
							String line;
							while((line=rdr.readLine()) != null) {
								if(!totaltime.containsKey(Integer.parseInt(line.split(",")[0]))) {
									totaltime.put(Integer.parseInt(line.split(",")[0]), Double.parseDouble(line.split(",")[2]));
									timeofday.put(Integer.parseInt(line.split(",")[0]), line.split(",")[3]);
									anomalyscore.put(Integer.parseInt(line.split(",")[0]), Double.parseDouble(line.split(",")[4]));
									speedmap.put(Integer.parseInt(line.split(",")[0]), Double.parseDouble(line.split(",")[1]));
								} else {
									totaltime.put(Integer.parseInt(line.split(",")[0]), totaltime.get(Integer.parseInt(line.split(",")[0])) 
													+ Double.parseDouble(line.split(",")[2]));
									
									//check to make sure counter values are consistent to speed and TOD all across
									if(!timeofday.get(Integer.parseInt(line.split(",")[0])).equals(line.split(",")[3])) {
										System.out.println("values not same for counter: " + line.split(",")[0]);
									}
									
									//check on the sanctity of anomaly scores across different runs
									if(anomalyscore.get(Integer.parseInt(line.split(",")[0])) != Double.parseDouble(line.split(",")[4])) {
										System.out.println("anomaly scores do not match");
									}
									
									if(speedmap.get(Integer.parseInt(line.split(",")[0])) != Double.parseDouble(line.split(",")[1])) {
										System.out.println("speed values do not match");
									}
								}
							}
							rdr.close();
						}
					}
				}
			}
		}
		
		File avgout = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/-100to300range/car13/HTMseqTOD.csv");
		PrintWriter pw = new PrintWriter(avgout);
		for(Map.Entry<Integer, Double> set : totaltime.entrySet()) {
			int key = set.getKey();
			pw.println(key + "," + (set.getValue()/numofexp) + "," + timeofday.get(key) + "," + anomalyscore.get(key) + "," + speedmap.get(key));
		}
		pw.close();
		System.out.println("computed average of all experiments...");
	}
	
}
