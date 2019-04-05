package iu.edu.indycar.models;

public class Anomaly {

    private double anomaly;
    private double rawData;
    private String anomalyType;

    public double getAnomaly() {
        return anomaly;
    }

    public void setAnomaly(double anomaly) {
        this.anomaly = anomaly;
    }

    public double getRawData() {
        return rawData;
    }

    public void setRawData(double rawData) {
        this.rawData = rawData;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }
}
