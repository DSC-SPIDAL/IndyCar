package com.dsc.iu.utils;

import org.numenta.nupic.network.sensor.Publisher;

/*
 * anomaly detection on just the scalar encoder 'vehicle_speed' and removing 'time_of_day' parameter from detection
 * */
public class SingleMetricAnomaly {
	
	public static void main(String[] args) {
		Publisher manualPublisher = Publisher.builder().addHeader("consumption").addHeader("float").addHeader("B").build();
		
		
	}
}
