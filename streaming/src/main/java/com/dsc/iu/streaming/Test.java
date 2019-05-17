package com.dsc.iu.streaming;

public class Test {
    public static void main(String[] args) {
        MQTTClientInstance.getInstance();
        new Thread(new Runnable() {
            @Override
            public void run() {
                AnomalyDetectionTask anomalyDetectionTask = new AnomalyDetectionTask("21");
                anomalyDetectionTask.open(null, null, null);
                while (true) {
                    anomalyDetectionTask.nextTuple();
                }
            }
        }).start();
    }
}
