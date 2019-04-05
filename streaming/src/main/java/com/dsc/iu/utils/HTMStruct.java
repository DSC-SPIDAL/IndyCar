package com.dsc.iu.utils;

public class HTMStruct {
	String carnum, spoutcounter, lapDistance, metricval, timeofday;
	long spout_ts, bolt_ts;
	boolean htmflag;
	
	public long getSpout_ts() {
		return spout_ts;
	}
	public void setSpout_ts(long spout_ts) {
		this.spout_ts = spout_ts;
	}
	public String getTimeofday() {
		return timeofday;
	}
	public void setTimeofday(String timeofday) {
		this.timeofday = timeofday;
	}
	public long getBolt_ts() {
		return bolt_ts;
	}
	public void setBolt_ts(long bolt_ts) {
		this.bolt_ts = bolt_ts;
	}
	
	public boolean isHtmflag() {
		return htmflag;
	}
	public void setHtmflag(boolean htmflag) {
		this.htmflag = htmflag;
	}
	public String getCarnum() {
		return carnum;
	}
	public void setCarnum(String carnum) {
		this.carnum = carnum;
	}
	public String getSpoutcounter() {
		return spoutcounter;
	}
	public void setSpoutcounter(String spoutcounter) {
		this.spoutcounter = spoutcounter;
	}
	public String getLapDistance() {
		return lapDistance;
	}
	public void setLapDistance(String lapDistance) {
		this.lapDistance = lapDistance;
	}
	public String getMetricval() {
		return metricval;
	}
	public void setMetricval(String metricval) {
		this.metricval = metricval;
	}
}
