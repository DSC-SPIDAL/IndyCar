package iu.edu.indycar.prediction;

public class PredictionOfCar implements Comparable<PredictionOfCar> {

  private String carNumber;
  private int completedLaps = 0;
  private float predictedTime = 0f;

  public PredictionOfCar(String carNumber, int completedLaps, float predictedTime) {
    this.carNumber = carNumber;
    this.completedLaps = completedLaps;
    this.predictedTime = predictedTime;
  }

  public String getCarNumber() {
    return carNumber;
  }

  @Override
  public int compareTo(PredictionOfCar o) {
    int compareLaps = Integer.compare(this.completedLaps, o.completedLaps);
    if (compareLaps != 0) {
      return compareLaps * -1;
    } else {
      return Float.compare(this.predictedTime, o.predictedTime) * -1;
    }
  }
}
