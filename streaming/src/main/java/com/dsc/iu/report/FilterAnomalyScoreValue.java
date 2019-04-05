package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/*
 * filters the anomaly score value from the CSV file containing the <record index, anomaly score> for values above a certain threshold.
 * */
public class FilterAnomalyScoreValue {
	public static void main(String[] args) throws IOException{
//		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/anomaly8.csv")));
//		File f = new File("/Users/sahiltyagi/Desktop/filteredAnomaly8.csv");
//		PrintWriter pw = new PrintWriter(f);
//		String record;
//		while((record=rdr.readLine()) != null) {
//			if(Double.parseDouble(record.split(",")[1]) >= 0.5) {
//				pw.println(record);
//			}
//		}
//		
//		rdr.close();
//		pw.flush();
//		pw.close();
//		System.out.println("generated the filtered anomaly score stats.");
		
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/inputcar-9.csv")));
		File f = new File("/Users/sahiltyagi/Desktop/filtered9.csv");
		PrintWriter pw = new PrintWriter(f);
		String record;
		while((record=rdr.readLine()) != null) {
			if(Double.parseDouble(record.split(",")[1]) != 0.0) {
				pw.println(record.trim());
			}
		}
		
		rdr.close();
		pw.flush();
		pw.close();
		System.out.println("generated the filtered input values without pitstops.");
		
	}
}
