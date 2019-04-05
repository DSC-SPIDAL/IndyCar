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
			BufferedReader rdr = new BufferedReader(new InputStreamReader(
							new FileInputStream("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/htm99threads.txt")));
			String str;
			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Desktop/allcars99threads-cpumem.csv")));
			int index=1;
			//speed
			//String proc_id = "6509";
			//rpm
			//String proc_id = "7448";
			//throttle
			//String proc_id = "6854";
			//all cars 99 threads
			String proc_id = "9040";
			
			while((str=rdr.readLine()) !=null) {
				if(str.contains("java") && str.contains(proc_id)) {
					//index, memory, cpu
					System.out.println(str.split("\\s+")[str.split("\\s+").length -4]);
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
}
