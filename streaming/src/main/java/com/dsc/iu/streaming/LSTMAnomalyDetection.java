package com.dsc.iu.streaming;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.util.Random;


public class LSTMAnomalyDetection {

    private final int historySize;
    private final String modelInputName;
    private final String modelOutputName;
    String modelDir;
    Session session;

    public LSTMAnomalyDetection(String modelDir, int historySize) {
        this.modelDir = modelDir;
        session = SavedModelBundle.load(this.modelDir, "serve").session();

        this.historySize = historySize;
        this.modelInputName = "serving_default_lstm_input";
        this.modelOutputName = "StatefulPartitionedCall";
    }

    // This is for testing
    public static void main(String[] args) {

        int historySize = 150;
        LSTMAnomalyDetection lstmSpeed = new LSTMAnomalyDetection("/home/sakkas/github/" +
                "indycar-lstm-anomalydetection-java/models/vehicleSpeed/", historySize);
        LSTMAnomalyDetection lstmRpm = new LSTMAnomalyDetection("/home/sakkas/github/" +
                "indycar-lstm-anomalydetection-java/models/engineSpeed/", historySize);
        LSTMAnomalyDetection lstmThrottle = new LSTMAnomalyDetection("/home/sakkas/github/" +
                "indycar-lstm-anomalydetection-java/models/throttle/", historySize);

        Random rand = new Random();

        //Generate random input to test it.
        float[][][] testData = new float[1][historySize][1];
        for (int i = 0; i < historySize; i++) {
            testData[0][i][0] = rand.nextFloat();
            //System.out.println(testData[0][i][0]);
        }

        double speedAnomalyScore = lstmSpeed.calculateAnomalyScore(testData);
        double rpmAnomalyScore = lstmRpm.calculateAnomalyScore(testData);
        double throttleAnomalyScore = lstmThrottle.calculateAnomalyScore(testData);

        System.out.println("Speed anomaly score:" + speedAnomalyScore);
        System.out.println("RPM anomaly score:" + rpmAnomalyScore);
        System.out.println("Throttle anomaly score:" + throttleAnomalyScore);
        //// TODO: 6/19/20 Throttle Model's anomaly values are high compared  to others. Check it.

        benchmark(testData);

    }

    public static void benchmark(float[][][] testData) {
        int historySize = 150;
        LSTMAnomalyDetection lstmSpeed = new LSTMAnomalyDetection("/home/sakkas/github/" +
                "indycar-lstm-anomalydetection-java/models/vehicleSpeed/", historySize);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            lstmSpeed.calculateAnomalyScore(testData);
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("elapsed time: " + timeElapsed);

    }

    public int getHistorySize() {
        return historySize;
    }

    private double meanAbsoluteError(float[][][] input, float[][][] prediction) {
        double sum = 0.0;
        for (int i = 0; i < getHistorySize(); i++) {
            sum += Math.abs(input[0][i][0] - prediction[0][i][0]);
        }

        return sum / getHistorySize(); //mean absolute error


    }

    public double calculateAnomalyScore(float[][][] input) {
        Tensor inputTensor = Tensor.create(input, Float.class);
        Tensor predictionTensor = session.runner()
                .feed(modelInputName, inputTensor).fetch(modelOutputName).run().get(0);
        float[][][] prediction = new float[1][getHistorySize()][1];
        predictionTensor.copyTo(prediction);
        return meanAbsoluteError(input, prediction);
    }
}