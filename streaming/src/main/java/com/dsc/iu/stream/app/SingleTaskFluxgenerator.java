package com.dsc.iu.stream.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SingleTaskFluxgenerator {
	 private static final String TEMPLATE_SPOUT = "  - id: \"telemetryspout-#NO\"\n" +
	            "    className: \"com.dsc.iu.streaming.AnomalyDetectionTask\"\n" +
	            "    parallelism: 1\n" +
	            "    constructorArgs:\n" +
	            "      - \"#NO\"\n\n";
	 
	 public static void main(String[] args) throws IOException {
		 StringBuilder fluxTemplate = new StringBuilder(
	                "name: \"INTEL_TOPOLOGY_INDYCAR\"\n" +
	                        "config:\n" +
	                        "  topology.workers: 2\n" +
	                        "  topology.debug: false\n" +
	                        "  topology.acker.executors: 0\n\n"
	        );
		 
		 StringBuilder spouts = new StringBuilder("spouts:\n");
		 List<String> carlist = new LinkedList<String>();
		 carlist.add("20");carlist.add("21");carlist.add("13");carlist.add("98");carlist.add("19");carlist.add("33");carlist.add("24");carlist.add("26");carlist.add("7");carlist.add("6");
	     carlist.add("60");carlist.add("27");carlist.add("22");carlist.add("18");carlist.add("3");carlist.add("4");carlist.add("28");carlist.add("32");carlist.add("59");carlist.add("25");
	     carlist.add("64");carlist.add("10");carlist.add("15");carlist.add("17");carlist.add("12");carlist.add("1");carlist.add("9");carlist.add("14");carlist.add("23");carlist.add("30");
	     carlist.add("29");carlist.add("88");carlist.add("66");
	        
	     System.out.println("list size:"+ carlist.size());
	     
	     for (int i = 0; i < carlist.size(); i++) {
	    	 spouts.append(
	                    TEMPLATE_SPOUT.replaceAll("#NO", carlist.get(i))
	            );
	     }
	     
	     fluxTemplate.append(spouts);
	     fluxTemplate.append("\n");
	     FileWriter fileWriter = new FileWriter(new File("/Users/sahiltyagi/Desktop/intel_indycar.yaml"));
	     fileWriter.write(fluxTemplate.toString());
	     fileWriter.flush();
	     fileWriter.close();
	     System.out.println(fluxTemplate);
	}
}
