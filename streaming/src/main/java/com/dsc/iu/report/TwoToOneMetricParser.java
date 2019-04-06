package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TwoToOneMetricParser {
	public static void main(String[] args) throws Exception {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/Indy500/default_HTM/scott_dixon/dixon_indycar.log")));
		BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Desktop/testlogic.log")));
		String line;
		while((line=rdr.readLine()) != null) {
			wrtr.write(line.split(",")[1] + "\n");
		}
		
		System.out.println("completed.");
		rdr.close();
		wrtr.close();
	}
}
