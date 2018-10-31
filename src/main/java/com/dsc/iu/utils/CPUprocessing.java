package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CPUprocessing {
	public static void main(String[] args) {
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/cpulog.txt")));
			String str;
			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Desktop/resource_consumption.csv")));
			int index=1;
			while((str=rdr.readLine()) !=null) {
				if(str.contains("java")) {
					//System.out.println(str);
					//System.out.println(str.split("\\s+")[str.split("\\s+").length -5]);
					//index, memory, cpu
					wrtr.write(index + "," + str.split("\\s+")[str.split("\\s+").length -4] + "," + str.split("\\s+")[str.split("\\s+").length -5] + "\n");
					index++;
				}
			}
			
			System.out.println("complete.");
			wrtr.close();
			rdr.close();
			
			//nupicDateTimeProcessing();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void nupicDateTimeProcessing() throws IOException {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/dixon_indycar.csv")));
		BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Desktop/dixon_final.csv")));
		String s;
		while((s=rdr.readLine()) != null) {
			//System.out.println(s.substring(0, s.indexOf(":") +3) + "," + s.split(",")[1]);
			wrtr.write(s.substring(0, s.indexOf(":") +3) + "," + s.split(",")[1] + "\n");
		}
		
		System.out.println("completed nupic datetime file");
		wrtr.close();
		rdr.close();
	}
}
