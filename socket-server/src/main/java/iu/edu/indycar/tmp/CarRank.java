package iu.edu.indycar.tmp;

public class CarRank implements Comparable<CarRank> {

    private String carNumber;

    private int lap = 1;
    private double distanceFromStart = 0;

    public CarRank(String carNumber) {
        this.carNumber = carNumber;
    }

    public void reset() {
        this.lap = 1;
        this.distanceFromStart = 0;
    }

    public synchronized void recordDistance(double distance) {
        if (distanceFromStart > distance) {
//            System.out.println("Lower record " + carNumber + " : " + distanceFromStart + "," + distance + ":" + Thread.currentThread().getId());
            lap++;
        }
        this.distanceFromStart = distance;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public int getLap() {
        return lap;
    }

    public double getDistanceFromStart() {
        return distanceFromStart;
    }

    @Override
    public int compareTo(CarRank o) {
        if (this.lap < o.lap) {
            return 1;
        } else if (this.lap > o.lap) {
            return -1;
        } else {
            if (this.distanceFromStart < o.distanceFromStart) {
                return 1;
            } else if (this.distanceFromStart > o.distanceFromStart) {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "CarRank{" +
                "carNumber='" + carNumber + '\'' +
                ", lap=" + lap +
                ", distanceFromStart=" + distanceFromStart +
                '}';
    }
}
