package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Percentile {
	public static void main(String[] args) throws Exception {
		org.apache.commons.math3.stat.descriptive.rank.Percentile p = new org.apache.commons.math3.stat.descriptive.rank.Percentile();
		
		double[] arr = new double[3694628];
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/totalSink33.csv")));
		String line;
		for(int i=0; i<arr.length; i++) {
			arr[i] = Double.parseDouble(rdr.readLine().split(",")[3]);
		}
		
		System.out.println(p.evaluate(arr, 99.0));
	}
}