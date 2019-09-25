package iu.edu.indycar.prediction;

import java.util.HashMap;
import java.util.Map;

public class RankResult {

  private Map<String, Integer> carToRank = new HashMap<>();
  private Map<Integer, String> rankToCar = new HashMap<>();
  private Map<String, Integer> predictions = new HashMap<>();

  private boolean hasChanges = false;

  public synchronized void clear() {
    this.hasChanges = false;
    this.carToRank.clear();
    this.rankToCar.clear();
    this.predictions.clear();
  }

  public synchronized void publishRank(String carNumber, Integer position) {
    if (position != null) {
      Integer previousPosition = this.carToRank.put(carNumber, position);

      if (previousPosition != null) {
        this.rankToCar.remove(previousPosition);
      }

      String previousCarAtThisPosition = this.rankToCar.get(position);
      if (previousCarAtThisPosition != null) {
        this.carToRank.remove(previousCarAtThisPosition);
        //this.publishRank(previousCarAtThisPosition, position + 1);
      }
      this.rankToCar.put(position, carNumber);
      if (previousPosition == null || !previousPosition.equals(position)) {
        this.hasChanges = true;
      }
    }
  }

  public synchronized void publishPrediction(String carNumber, Integer prediction) {
    if (prediction != null) {
      Integer previousPrediction = this.predictions.put(carNumber, prediction);
      if (previousPrediction == null || !previousPrediction.equals(prediction)) {
        this.hasChanges = true;
      }
    }
  }

  public Map<Integer, String> getRankToCar() {
    return rankToCar;
  }

  public Map<String, Integer> getCarToRank() {
    return carToRank;
  }

  public Map<String, Integer> getPredictions() {
    return predictions;
  }

  public synchronized boolean isHasChanges() {
    return hasChanges;
  }

  public synchronized RankResult copy() {
    RankResult copy = new RankResult();
    copy.predictions.putAll(this.predictions);
    this.carToRank.forEach((car, rank) -> {
      copy.rankToCar.put(rank, car);
    });
    copy.carToRank.putAll(this.carToRank);
    return copy;
  }

  public synchronized void markBroadcasted() {
    this.hasChanges = false;
  }
}
