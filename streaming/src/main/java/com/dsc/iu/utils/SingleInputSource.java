package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/*
 * generates the telemetry data for a single car from the eRP log file. In the current use case, car #9-> scott dixon
 * */
public class SingleInputSource {
	
	public static void main(String[] args) {
		try {
//			FileWriter fw = new FileWriter("D:\\\\anomalydetection\\dixon_17000.log");
//			PrintWriter pw =new PrintWriter(fw);
//			BufferedReader rdr = new BufferedReader(new InputStreamReader(
//							new FileInputStream("D:\\\\anomalydetection\\eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
			
			FileWriter fw = new FileWriter("/Users/sahiltyagi/Desktop/rpm_car9.csv");
			PrintWriter pw =new PrintWriter(fw);
			BufferedReader rdr = new BufferedReader(new InputStreamReader(
							new FileInputStream("/Users/sahiltyagi/Desktop/Indy500/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
			
			String line;
			while((line=rdr.readLine()) != null) {
				if(line.startsWith("$P") && line.split("�")[2].length() >9 && line.split("�")[1].equals("9")) {
//					pw.println("5/28/17 " + line.split("�")[2] + "," + line.split("�")[line.split("�").length -3]);
//					pw.println("5/28/17 " + line.split("�")[2] + "," + (Double.parseDouble(line.split("�")[4])*0.02));
//					pw.println("5/28/17 " + line.split("�")[2] + "," + Double.parseDouble(line.split("�")[4]));
					
					//speed metric
					//pw.println(Double.parseDouble(line.split("�")[4]));
					//RPM metric
					//pw.println(Double.parseDouble(line.split("�")[5]));
					//throttle metric
					//pw.println(Double.parseDouble(line.split("�")[6]));

					pw.println(line.split("�")[4] + "," + line.split("�")[5] + line.split("�")[6] + "\n");
				}
			}
			
			pw.close();
			fw.close();
			rdr.close();
			System.out.println("created file for single car");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
}