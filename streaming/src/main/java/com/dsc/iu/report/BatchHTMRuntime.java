package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * to calculate the cumulative running time for HTM in batch mode to calculate anomaly scores for a single car data. For car #9, it's 20918.9 milliseconds in batch mode. 
 * */
public class BatchHTMRuntime {
	public static void main(String[] args) {
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/avg_execution.csv")));
			String record;
			double execution_time=0.0;
			while((record=rdr.readLine()) != null) {
				System.out.println(record.split(",")[1]);
				execution_time += Double.parseDouble(record.split(",")[1]);
			}
			
			System.out.println("total execution time in milliseconds:" + execution_time);
			System.out.println("end BatchHTMRuntime");
			rdr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
