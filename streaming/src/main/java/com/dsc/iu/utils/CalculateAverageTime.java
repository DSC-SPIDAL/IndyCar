package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CalculateAverageTime {
	public static void main(String[] args) throws IOException {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/brokersink.csv")));
		String line;
		int ctr=0;
		long total=0;
		while((line=rdr.readLine()) != null) {
			ctr++;
			total += Long.parseLong(line.split(",")[1]);
		}
		rdr.close();
		
		System.out.println("average time value:"+ ((double)total/ctr));
	}
}
