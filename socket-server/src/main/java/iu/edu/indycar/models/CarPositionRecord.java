package iu.edu.indycar.models;

public class CarPositionRecord {

    private double distance;
    private double time;
    private String carNumber;

    public CarPositionRecord(double distance, double time, String carNumber) {
        this.distance = distance;
        this.time = time;
        this.carNumber = carNumber;
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

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
