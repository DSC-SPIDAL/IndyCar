package iu.edu.indycar.streamer.experiments;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class AnomalyLabelsBank {

    public static HashMap<String, Queue<AnomalyLabel>> anomalyLabels = new HashMap<>();

    public static void loadLabelsFromCSV(String csvPath) throws IOException {
        anomalyLabels.clear();
        BufferedReader br = new BufferedReader(new FileReader(new File(csvPath)));
        String line = br.readLine(); // pass the header
        while ((line = br.readLine()) != null) {
            String[] split = line.split(",");
            AnomalyLabel anomalyLabel = new AnomalyLabel();
            anomalyLabel.setCarNumber(split[0]);
            anomalyLabel.setLabel(split[1]);
            anomalyLabel.setFromStr(split[2]);
            anomalyLabel.setToStr(split[3]);

            anomalyLabels.computeIfAbsent(
                    anomalyLabel.getCarNumber(), s -> new PriorityQueue<>()
            ).add(anomalyLabel);
        }
    }

    public static void reset() {
        anomalyLabels.clear();
    }

//    private static long tempCount = 0;
//    private static String uuid = UUID.randomUUID().toString();

    public static AnomalyLabel getAnomalyForCarAt(String carNumber, long eventTime) {
        if (anomalyLabels.containsKey(carNumber)) {
            Queue<AnomalyLabel> anomalyLabels = AnomalyLabelsBank.anomalyLabels.get(carNumber);
            AnomalyLabel anomalyLabel = anomalyLabels.peek();
            if (anomalyLabel != null) {
                if (anomalyLabel.getTo() < eventTime) {
                    anomalyLabels.poll();
                } else if (anomalyLabel.getFrom() < eventTime) {
                    return anomalyLabel;
                }
            }
        }
//        if (anomalyLabels != null && tempCount++ % 1000 < 100) {
//            AnomalyLabel anomalyLabel = new AnomalyLabel();
//            anomalyLabel.setUuid(uuid);
//            anomalyLabel.setFrom(eventTime);
//            anomalyLabel.setTo(eventTime + 100);
//            anomalyLabel.setLabel("Anomaly Label");
//            return anomalyLabel;
//        } else {
//            uuid = UUID.randomUUID().toString();
//        }
        return null;
    }
}
