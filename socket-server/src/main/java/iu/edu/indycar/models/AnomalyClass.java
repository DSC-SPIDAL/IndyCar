package iu.edu.indycar.models;

public class AnomalyClass {

  public static int ANOMALY_CLASS_SEVERE = 2;
  public static int ANOMALY_CLASS_WARN = 1;
  public static int ANOMALY_CLASS_NORMAL = 0;

  private int carNumber;
  private int anomalyClass;

  private String anomalyTag;

  public AnomalyClass(int carNumber, int anomalyClass, String anomalyTag) {
    this.carNumber = carNumber;
    this.anomalyClass = anomalyClass;
    this.anomalyTag = anomalyTag;
  }

  public String getAnomalyTag() {
    return anomalyTag;
  }

  public void setAnomalyTag(String anomalyTag) {
    this.anomalyTag = anomalyTag;
  }

  public int getCarNumber() {
    return carNumber;
  }

  public void setCarNumber(int carNumber) {
    this.carNumber = carNumber;
  }

  public int getAnomalyClass() {
    return anomalyClass;
  }

  public void setAnomalyClass(int anomalyClass) {
    this.anomalyClass = anomalyClass;
  }
}
