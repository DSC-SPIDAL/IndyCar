package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PercentileSort {
	public static void main(String[] args) throws Exception {
		int min=Integer.MAX_VALUE, max=Integer.MIN_VALUE;
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/benchmarks/33htm/sorted_latency.csv")));
		String line; 
		int totalval=0, ctr=0;
		while((line =rdr.readLine()) != null) {
			if(!(min < Integer.parseInt(line.trim()))) {
				min = Integer.parseInt(line.trim());
			}
			if(max < Integer.parseInt(line.trim())) {
				max = Integer.parseInt(line.trim());
			}
			totalval += Integer.parseInt(line.trim());
			ctr++;
		}
		
		System.out.println(totalval);
		System.out.println(ctr);
		System.out.println("average latency:" + (totalval/ctr));
		System.out.println("minimum value:" + min);
		System.out.println("maximum value:" + max);
		
//		percentilesort();
	}
	
	public static void percentilesort() throws Exception {
		File f = new File("/Users/sahiltyagi/Desktop/benchmarks/htmjava_sortedlatency.csv");
		PrintWriter pw  = new PrintWriter(f);
		List<Integer> list = new ArrayList<Integer>();
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/benchmarks/HTMsequential.csv")));
		//FOR STORM HTM POST-PROCESSING
//		String line;
//		while((line =rdr.readLine()) != null) {
//			list.add(Integer.parseInt(line.split(",")[1].trim()));
//		}
//		
//		rdr.close();
//		System.out.println("set size:"+ list.size());
//		Collections.sort(list);
//		Iterator<Integer> itr = list.iterator();
//		while(itr.hasNext()) {
//			pw.println(itr.next());
//		}
//		
//		pw.flush();
//		pw.close();
//		System.out.println("sorted latency file");
		
		String line;
		while((line =rdr.readLine()) != null) {
			list.add(Integer.parseInt(line.split(",")[2].trim()));
		}
		
		rdr.close();
		System.out.println("set size:"+ list.size());
		Collections.sort(list);
		Iterator<Integer> itr = list.iterator();
		while(itr.hasNext()) {
			pw.println(itr.next());
		}
		
		pw.flush();
		pw.close();
		System.out.println("sorted latency file");
	}
}
