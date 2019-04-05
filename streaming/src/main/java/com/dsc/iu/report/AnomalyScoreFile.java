package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/*
 * takes htmsample.txt file and returns a file with (record index, anomaly score) to plot the anomaly score graph
 * */
public class AnomalyScoreFile {
	public static void main(String[] args) {
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(
							new File("/Users/sahiltyagi/Desktop/htmrpm11.txt"))));
			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
							new File("/Users/sahiltyagi/Desktop/rpmAnomaly11.csv"))));
			String record;
			while((record=rdr.readLine()) != null) {
				wrtr.write(record.split(",")[0] + "," + record.split(",")[2] + "\n");
			}
			
			wrtr.close();
			rdr.close();
			System.out.println("completed anomaly score report.");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
