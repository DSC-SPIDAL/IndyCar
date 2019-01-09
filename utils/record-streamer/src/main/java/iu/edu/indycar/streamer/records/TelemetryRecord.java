package iu.edu.indycar.streamer.records;

public class TelemetryRecord implements IndycarRecord {

    private String carNumber;
    private String timeOfDay;
    private double lapDistance;
    private double vehicleSpeed;
    private double engineSpeed;
    private double throttle;
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
    }

    public double getLapDistance() {
        return lapDistance;
    }

    public void setLapDistance(double lapDistance) {
        this.lapDistance = lapDistance;
    }

    public double getVehicleSpeed() {
        return vehicleSpeed;
    }

    public void setVehicleSpeed(double vehicleSpeed) {
        this.vehicleSpeed = vehicleSpeed;
    }

    public double getEngineSpeed() {
        return engineSpeed;
    }

    public void setEngineSpeed(double engineSpeed) {
        this.engineSpeed = engineSpeed;
    }

    public double getThrottle() {
        return throttle;
    }

    public void setThrottle(double throttle) {
        this.throttle = throttle;
    }

    @Override
    public String getGroupTag() {
        return "CAR_" + this.carNumber;
    }

    @Override
    public long getTimeField() {
        return this.getTimeOfDayLong();
    }

    @Override
    public boolean isTimeSensitive() {
        return true;
    }
}
