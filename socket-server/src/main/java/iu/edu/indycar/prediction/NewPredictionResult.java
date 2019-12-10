package iu.edu.indycar.prediction;

public class NewPredictionResult {

    private String carNumber;
    private int prediction;

    public NewPredictionResult(String carNumber, int prediction) {
        this.carNumber = carNumber;
        this.prediction = prediction;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public int getPrediction() {
        return prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }
}
