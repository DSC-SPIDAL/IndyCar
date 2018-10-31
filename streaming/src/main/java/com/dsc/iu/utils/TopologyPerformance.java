package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class TopologyPerformance {
	public static void main(String[] args) {
		
		try {
			
//			String q = "apache-storm-1.0.4/logs/workers-artifacts/indy500-1-1536696594/6800/worker.log:2018-09-11 16:10:42.974 "
//					+ "STDIO Thread-6-htmbolt-executor[3 3] [INFO] $$$$$$$$$$$$$$$$$$$$$$$$$$,16,221.07,1536696642974";
//			System.out.println(q.substring(q.lastIndexOf("]")+1, q.length()));
			
			
			File f = new File("/Users/sahiltyagi/Desktop/out.csv");
			PrintWriter pw = new PrintWriter(f);
			
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/executionTime.txt")));
			Map<String, Long> exectimemap = new HashMap<String, Long>();
			String st,s;
			while((st=rdr.readLine()) != null) {
				if(!st.isEmpty()) {
					s = st.substring(st.lastIndexOf("]")+1, st.length()).trim();
					//System.out.println(s);
					System.out.println(s.split(",")[1] + "_" + s.split(",")[2]);
					exectimemap.put(s.split(",")[1] + "_" + s.split(",")[2], Long.parseLong(s.split(",")[3]));
				}
			}
			rdr.close();
			
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/htmsample.txt")));
			while((st=rdr.readLine()) != null) {
				s = st.substring(st.lastIndexOf("]")+1, st.length()).trim();
				String input = s.split(",")[2];
				if(input.length() ==6) {
					input = input + "0";
				}
				
				if(input.length() ==5) {
					input = input + "00";
				}
				
				//String index = s.split(",")[1] + "_" + String.valueOf(Double.parseDouble(s.split(",")[2]));
				String index = s.split(",")[1] + "_" + input;
				//System.out.println(index);
				Long ts = Long.parseLong(s.split(",")[4]);
				//handle redundancy of partially running experiments causing mismatch of # records flushed to each file
				if(exectimemap.containsKey(index)) {
					pw.println(index.split("_")[0] + "," + (ts - exectimemap.get(index)));
				}
			}
			
			System.out.println("completed.");
			pw.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
