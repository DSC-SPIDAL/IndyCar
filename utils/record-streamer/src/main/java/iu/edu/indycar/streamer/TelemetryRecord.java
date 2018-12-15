package iu.edu.indycar.streamer;

public class TelemetryRecord {

  private String carNumber;
  private String timeOfDay;
  private String lapDistance;
  private String vehicleSpeed;
  private String engineSpeed;
  private String throttle;
  private String date;

  private long timeOfDayLong;

  public long getTimeOfDayLong() {
    return timeOfDayLong;
  }

  public void setTimeOfDayLong(long timeOfDayLong) {
    this.timeOfDayLong = timeOfDayLong;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getCarNumber() {
    return carNumber;
  }

  public void setCarNumber(String carNumber) {
    this.carNumber = carNumber;
  }

  public String getTimeOfDay() {
    return timeOfDay;
  }

  public void setTimeOfDay(String timeOfDay) {
    this.timeOfDay = timeOfDay;
    this.timeOfDayLong = TimeUtils.convertTimestampToLong(this.timeOfDay);
  }

  public String getLapDistance() {
    return lapDistance;
  }

  public void setLapDistance(String lapDistance) {
    this.lapDistance = lapDistance;
  }

  public String getVehicleSpeed() {
    return vehicleSpeed;
  }

  public void setVehicleSpeed(String vehicleSpeed) {
    this.vehicleSpeed = vehicleSpeed;
  }

  public String getEngineSpeed() {
    return engineSpeed;
  }

  public void setEngineSpeed(String engineSpeed) {
    this.engineSpeed = engineSpeed;
  }

  public String getThrottle() {
    return throttle;
  }

  public void setThrottle(String throttle) {
    this.throttle = throttle;
  }
}
