package iu.edu.indycar.models;

public class AnomalyMessage {

    private int carNumber;
    private long index;
    private double anomaly;
    private double rawData;
    private String anomalyType;
    private String timeOfDayString;

    public String getTimeOfDayString() {
        return timeOfDayString;
    }

    public void setTimeOfDayString(String timeOfDayString) {
        this.timeOfDayString = timeOfDayString;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }

    public double getRawData() {
        return rawData;
    }

    public void setRawData(double rawData) {
        this.rawData = rawData;
    }

    public int getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(int carNumber) {
        this.carNumber = carNumber;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public double getAnomaly() {
        return anomaly;
    }

    public void setAnomaly(double anomaly) {
        this.anomaly = anomaly;
    }
}
