package iu.edu.indycar.streamer.records;

public class WeatherRecord implements IndycarRecord {

  private long timeOfDay;
  private int temperature;
  private int relativeHumidity;
  private int pressure;

  public long getTimeOfDay() {
    return timeOfDay;
  }

  public void setTimeOfDay(long timeOfDay) {
    this.timeOfDay = timeOfDay;
  }

  public int getTemperature() {
    return temperature;
  }

  public void setTemperature(int temperature) {
    this.temperature = temperature;
  }

  public int getRelativeHumidity() {
    return relativeHumidity;
  }

  public void setRelativeHumidity(int relativeHumidity) {
    this.relativeHumidity = relativeHumidity;
  }

  public int getPressure() {
    return pressure;
  }

  public void setPressure(int pressure) {
    this.pressure = pressure;
  }

  @Override
  public String getGroupTag() {
    return "WEATHER";
  }

  @Override
  public long getTimeField() {
    return this.timeOfDay;
  }
}
