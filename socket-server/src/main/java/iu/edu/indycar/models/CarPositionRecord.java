package iu.edu.indycar.models;

public class CarPositionRecord {

    private double distance;
    private double time;
    private long deltaTime = 0;
    private double deltaDistance = 0;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(long deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double getDeltaDistance() {
        return deltaDistance;
    }

    public void setDeltaDistance(double deltaDistance) {
        this.deltaDistance = deltaDistance;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "CarPositionRecord{" +
                "distance=" + distance +
                ", deltaTime=" + deltaTime +
                '}';
    }
}
