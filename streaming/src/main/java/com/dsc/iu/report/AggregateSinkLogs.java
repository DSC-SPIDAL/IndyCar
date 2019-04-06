package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class AggregateSinkLogs {
	public static void main(String[] args) throws Exception {
		File f = new File("/Users/sahiltyagi/Desktop/sinks33htm/");
		File f2= new File("/Users/sahiltyagi/Desktop/totalSink33.csv");
		PrintWriter pw = new PrintWriter(f2);
		if(f.isDirectory()) {
			File[] files = f.listFiles();
			for(File file : files) {
				if(file.getName().startsWith("sink-")) {
					BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
					System.out.println("reading for car:"+file.getName());
					String line;
					while((line=rdr.readLine()) != null) {
						//System.out.println(line);
						pw.println(line.split(",")[0] + "," + line.split(",")[1] + "," + line.split(",")[2] +  "," + line.split(",")[3] + "," + line.split(",")[4]);
					}
					
					rdr.close();
				}
			}
			pw.close();
		}
	}
}
