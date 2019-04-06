package com.dsc.iu.stream.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.Classifier;
import org.numenta.nupic.algorithms.SDRClassifier;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;

import rx.Subscriber;

public class BlankHTMBolt3 extends BaseRichBolt {
	
	private final long serialVersionUID = 1L;
	private OutputCollector collector;
	private String metric;
	//spoutctr brings the counter value from telemetry publisher and used for generating keys for lapDistance and timeOfDay hashmaps
	private String carnum, spoutctr;
	private int min, max;
	//ctr to increment after processing each tuple and used to match records for lapDistance and timeOfDay hashmaps. Basically, htmctr replaces spoutctr as data point emitted to sink and match a tuple for it's original features
	//private int htmctr=0;
	private Publisher manualpublish;
	private Network network;
	private ConcurrentHashMap<String, String> lapdistancemap;
	private ConcurrentHashMap<String, Long> timeOfDaymap;
	private Tuple tuple;
	
	public BlankHTMBolt3(String metric, String min, String max) {
		this.metric = metric;
		this.min = Integer.parseInt(min);
		this.max = Integer.parseInt(max);
	}

	private String getMetricname() {
		return metric;
	}

	private void setMetricname(String metric) {
		this.metric = metric;
	}

	private int getMinVal() {
		return min;
	}

	private void setMinVal(int min) {
		this.min = min;
	}

	private int getMaxVal() {
		return max;
	}

	private void setMaxVal(int max) {
		this.max = max;
	}

	@Override
	public void execute(Tuple arg0) {
		tuple = arg0;
		long bolt_ts = System.currentTimeMillis();
		
		carnum = tuple.getStringByField("carnum");
		spoutctr = tuple.getStringByField("counter");
		lapdistancemap.put(carnum + "_" + spoutctr, tuple.getStringByField("lapDistance"));
		timeOfDaymap.put(carnum + "_" + spoutctr, tuple.getLongByField("spout_timestamp"));
		
		collector.emit(new Values(carnum, getMetricname(), tuple.getStringByField(getMetricname()), 1.0, spoutctr, lapdistancemap.get(carnum+"_"+spoutctr), 
				tuple.getLongByField("spout_timestamp"), bolt_ts));
		
		lapdistancemap.remove(carnum+"_"+spoutctr);
		timeOfDaymap.remove(carnum+"_"+spoutctr);
		
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		
		lapdistancemap = new ConcurrentHashMap<String, String>();
		timeOfDaymap = new ConcurrentHashMap<String, Long>();
		setMetricname(metric);
		setMinVal(min);
		setMaxVal(max);
		
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
	//	arg0.declare(new Fields("carnum","metric","dataval","score","counter", "lapDistance", "current_timestamp"));
		arg0.declare(new Fields("carnum","metric","dataval","score","counter", "lapDistance", "spout_timestamp", "bolt_timestamp"));
	}
}