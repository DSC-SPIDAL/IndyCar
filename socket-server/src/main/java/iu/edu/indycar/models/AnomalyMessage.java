package iu.edu.indycar.models;

public class AnomalyMessage {

  private int carNumber;
  private int index;
  private double anomaly;
  private double rawData;
  private String anomalyType;

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

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public double getAnomaly() {
    return anomaly;
  }

  public void setAnomaly(double anomaly) {
    this.anomaly = anomaly;
  }
}
