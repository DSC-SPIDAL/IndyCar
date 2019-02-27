package iu.edu.indycar.models;

import java.util.HashMap;
import java.util.Map;

public class AnomalyMessage {

    private int carNumber;
    private long index;
    private Map<String, Anomaly> anomalies = new HashMap<>();
    private String timeOfDayString;
    private AnomalyLabel anomalyLabel;

    public AnomalyLabel getAnomalyLabel() {
        return anomalyLabel;
    }

    public void setAnomalyLabel(AnomalyLabel anomalyLabel) {
        this.anomalyLabel = anomalyLabel;
    }

    public String getTimeOfDayString() {
        return timeOfDayString;
    }

    public void setTimeOfDayString(String timeOfDayString) {
        this.timeOfDayString = timeOfDayString;
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

    public Map<String, Anomaly> getAnomalies() {
        return anomalies;
    }

    public void addAnomaly(Anomaly anomaly) {
        this.anomalies.put(anomaly.getAnomalyType(), anomaly);
    }
}
