package com.dsc.iu.streaming;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

public class AnomalyDetectionTest {
	public static void main(String[] args) {
		
		TopologyBuilder builder = new TopologyBuilder();
		
		builder.setSpout("eRPlog", new TelemetryTestSpout());
		//builder.setBolt("htmbolt", new HTMBolt(), 2).shuffleGrouping("eRPlog");
		builder.setBolt("htmbolt", new HTMBolt()).shuffleGrouping("eRPlog");
		
		Config config = new Config();
		//run across 3 workers
		config.setNumWorkers(1);
		
//		LocalCluster cluster = new LocalCluster();
//		cluster.submitTopology("indy500", config, builder.createTopology());
//		try {
//			Thread.sleep(1000000);			//running topology for 1000 seconds in local mode
//		} catch(InterruptedException e) {
//			e.printStackTrace();
//		}
//		cluster.shutdown();
		
		StormTopology stormTopology = builder.createTopology();
	    try {
			StormSubmitter.submitTopology("indy500", config, stormTopology);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
