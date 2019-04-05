package iu.edu.indycar.models;

public class CarPositionRecord {

    private double distance;
    private long time;
    private String carNumber;
    private long sentTime;//to calculate latency
    private AnomalyLabel anomalyLabel;

    public CarPositionRecord(double distance, long time, String carNumber, AnomalyLabel anomalyLabel) {
        this.distance = distance;
        this.time = time;
        this.carNumber = carNumber;
        this.anomalyLabel = anomalyLabel;
    }

    public AnomalyLabel getAnomalyLabel() {
        return anomalyLabel;
    }

    public void setAnomalyLabel(AnomalyLabel anomalyLabel) {
        this.anomalyLabel = anomalyLabel;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
