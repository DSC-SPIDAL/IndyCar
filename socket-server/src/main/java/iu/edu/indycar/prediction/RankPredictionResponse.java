package iu.edu.indycar.prediction;

import java.util.List;

public class RankPredictionResponse {

  private List<Float> predictions;

  public List<Float> getPredictions() {
    return predictions;
  }

  public void setPredictions(List<Float> predictions) {
    this.predictions = predictions;
  }
}
