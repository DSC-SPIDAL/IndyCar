package iu.edu.indycar.tmp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class AnomalyLogger {

    private static Timer writerTimer = new Timer();

    private static HashMap<String, AnomalyLabelDocument> docs = new HashMap<>();

    public static AnomalyLabelDocument get(String carNumber, String anomalyLabel, String anomalyLabelId) {
        return docs.computeIfAbsent(anomalyLabelId,
                s -> new AnomalyLabelDocument(carNumber, anomalyLabel, anomalyLabelId)
        );
    }

    private static String sanitizeFilename(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }


    public static class AnomalyLabelDocument implements Serializable {

        private String carNumber;
        private String anomalyLabel;
        private String anomalyLabelId;

        private long from;
        private long to;

        private List<Long> counter = new ArrayList<>();

        private List<Double> speed = new ArrayList<>();
        private List<Double> speedAnomaly = new ArrayList<>();

        private List<Double> rpm = new ArrayList<>();
        private List<Double> rpmAnomaly = new ArrayList<>();

        private List<Double> throttle = new ArrayList<>();
        private List<Double> throttleAnomaly = new ArrayList<>();

        @JsonIgnore
        private TimerTask writeTask;

        public AnomalyLabelDocument(String carNumber, String anomalyLabel, String anomalyLabelId) {
            this.carNumber = carNumber;
            this.anomalyLabel = anomalyLabel;
            this.anomalyLabelId = anomalyLabelId;
        }

        private synchronized void scheduleWriting() {
            if (writeTask != null) {
                writeTask.cancel();
            }

            this.writeTask = new TimerTask() {
                @Override
                public void run() {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        File folder = new File("anomaly_labels");
                        if (folder.exists()) {
                            folder.mkdir();
                        }
                        objectMapper.writeValue(
                                new File(folder, sanitizeFilename(carNumber + "_" + anomalyLabel + anomalyLabelId) + ".json"),
                                AnomalyLabelDocument.this
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            writerTimer.schedule(this.writeTask, 1000 * 60);
        }

        public void record(double speed, double speedAn,
                           double rpm, double rpmAn,
                           double throttle, double throttleAn, long time, long counter) {

            if (this.from == 0) {
                this.from = time;
            } else {
                this.to = time;
            }

            this.speed.add(speed);
            this.speedAnomaly.add(speedAn);

            this.rpm.add(rpm);
            this.rpmAnomaly.add(rpmAn);

            this.throttle.add(throttle);
            this.throttleAnomaly.add(throttleAn);

            this.counter.add(counter);


            this.scheduleWriting();
        }

        public long getFrom() {
            return from;
        }

        public void setFrom(long from) {
            this.from = from;
        }

        public long getTo() {
            return to;
        }

        public void setTo(long to) {
            this.to = to;
        }

        public String getCarNumber() {
            return carNumber;
        }

        public void setCarNumber(String carNumber) {
            this.carNumber = carNumber;
        }

        public String getAnomalyLabel() {
            return anomalyLabel;
        }

        public void setAnomalyLabel(String anomalyLabel) {
            this.anomalyLabel = anomalyLabel;
        }

        public String getAnomalyLabelId() {
            return anomalyLabelId;
        }

        public void setAnomalyLabelId(String anomalyLabelId) {
            this.anomalyLabelId = anomalyLabelId;
        }

        public List<Double> getSpeed() {
            return speed;
        }

        public void setSpeed(List<Double> speed) {
            this.speed = speed;
        }

        public List<Double> getSpeedAnomaly() {
            return speedAnomaly;
        }

        public void setSpeedAnomaly(List<Double> speedAnomaly) {
            this.speedAnomaly = speedAnomaly;
        }

        public List<Double> getRpm() {
            return rpm;
        }

        public void setRpm(List<Double> rpm) {
            this.rpm = rpm;
        }

        public List<Double> getRpmAnomaly() {
            return rpmAnomaly;
        }

        public void setRpmAnomaly(List<Double> rpmAnomaly) {
            this.rpmAnomaly = rpmAnomaly;
        }

        public List<Double> getThrottle() {
            return throttle;
        }

        public void setThrottle(List<Double> throttle) {
            this.throttle = throttle;
        }

        public List<Double> getThrottleAnomaly() {
            return throttleAnomaly;
        }

        public void setThrottleAnomaly(List<Double> throttleAnomaly) {
            this.throttleAnomaly = throttleAnomaly;
        }

        public List<Long> getCounter() {
            return counter;
        }

        public void setCounter(List<Long> counter) {
            this.counter = counter;
        }
    }
}
