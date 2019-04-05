package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/*
 * fetches a single parameter from eRP log file, 'vehicle_speed'
 * */
public class IndycarERPMetrics {
	public static void main(String[] args) {
		//two metrics: <rpm,speed>
		//added multiplier to normalize rpm w.r.t speeed
		try {
			System.out.println("going to start");
//			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\\\anomalydetection\\dixon_SPEED_RPM.log")));
//			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\\\anomalydetection\\eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
			
			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Desktop/dixon_SPEED_RPM.log")));
			BufferedReader rdr = new BufferedReader(new InputStreamReader(
								new FileInputStream("/Users/sahiltyagi/Desktop/Indy500/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
			String record;
			while((record = rdr.readLine()) != null) {
//				if(record.startsWith("$P") && record.split("\\u00A6")[2].length() >9 && record.split("\\u00A6")[1].equals("9")) {
//						System.out.println("VEHICLE_SPEED IS: " + record.split("\\u00A6")[4] + " AND ENGINE_RPM:" + record.split("\\u00A6")[5]);
//						wrtr.write(record.split("\\u00A6")[4] + "," + record.split("\\u00A6")[5] + "\n");
//				}
				
				if(record.startsWith("$P") && record.split("�")[2].length() >9 && record.split("�")[1].equals("9") && !record.split("�")[4].equals("0.000")) {
					System.out.println("VEHICLE_SPEED IS: " + record.split("�")[4] + " AND ENGINE_RPM:" + record.split("�")[5]);
					wrtr.write(record.split("�")[4] + "," + record.split("�")[5] + "\n");

//			BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\\\anomalydetection\\normalized.log")));
//			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\\\anomalydetection\\eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
//			String record;
//			while((record = rdr.readLine()) != null) {
//				System.out.println(record.split("\u00A6")[2]);
//				if(record.startsWith("$P") && record.split("¦")[2].length() >9 && record.split("¦")[1].equals("9")) {
//						System.out.println("VEHICLE_SPEED IS: " + record.split("¦")[4] + " AND ENGINE_RPM:" + record.split("¦")[5]);
//						wrtr.write(record.split("¦")[4] + "," + (Integer.parseInt(record.split("¦")[5])*0.02) + "\n");
//				}
				}
			}
			
			System.out.println("generated the vehicle_speed log file for car #9");
			rdr.close();
			wrtr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
