package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

//CPU memory consumption log processor for sequential execution on 33 cars for 3 metrics (speed,rpm,throttle) in parallel threads
public class CPUMemSequentialParser {
	public static void main(String[] args) throws IOException{
		String javaprocessID = "35090";
		String username = "styagi";
		
		File outputcsv = new File("/Users/sahiltyagi/Desktop/seqCPUMEM.csv");
		PrintWriter pw = new PrintWriter(outputcsv);
		
		BufferedReader txtrdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/cpusequential33_3metrics.txt")));
		String record;
		int index=0;
		while((record=txtrdr.readLine()) != null) {
			if(record.contains(username) && record.contains(javaprocessID)) {
				//index is the tuple recorded- once every 30 seconds
				//index,csv,memory
				index++;
				pw.println(index + "," + record.split("\\s+")[8] + "," + record.split("\\s+")[9]);
			}
		}
		
		System.out.println("completed cpu and memory stats generation");
		txtrdr.close();
		pw.close();
	}
}
