package com.dsc.iu.stream.app;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

public class TopologyLauncher {
	
	public static void main(String[] args) {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("telemetryspout", new IndycarSpout("9"));
		builder.setBolt("htmbolt", new ScalarMetricBolt("9", "speed", "0", "250")).shuffleGrouping("telemetryspout");
		builder.setBolt("sinkbolt", new Sink()).shuffleGrouping("htmbolt");
		
		Config config = new Config();
		config.put(Config.WORKER_CHILDOPTS, "-Xmn32768m");
		config.put(Config.WORKER_HEAP_MEMORY_MB, 32768);
		
		StormTopology stormTopology = builder.createTopology();
	    try {
			StormSubmitter.submitTopology("indy500-v1", config, stormTopology);
			System.out.println("job command submitted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
