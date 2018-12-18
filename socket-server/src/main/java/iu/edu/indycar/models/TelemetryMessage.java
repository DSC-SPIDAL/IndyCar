package iu.edu.indycar.models;

import java.util.Date;

public class TelemetryMessage {

  private Date timestamp;
  private double distanceFromStart;
  private int carNumber;

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public double getDistanceFromStart() {
    return distanceFromStart;
  }

  public void setDistanceFromStart(double distanceFromStart) {
    this.distanceFromStart = distanceFromStart;
  }

  public int getCarNumber() {
    return carNumber;
  }

  public void setCarNumber(int carNumber) {
    this.carNumber = carNumber;
  }
}
