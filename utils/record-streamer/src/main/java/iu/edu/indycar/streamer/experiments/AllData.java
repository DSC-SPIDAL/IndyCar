package iu.edu.indycar.streamer.experiments;

import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllData {

    private static final Logger LOG = LogManager.getLogger(AllData.class);

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        AnomalyLabelsBank.loadLabelsFromCSV("/home/chathura/Downloads/indycar_anomaly_labels.csv");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 1000000000, s -> s.split("_")[2]);


        List<String> times = new ArrayList<>(80000);
        List<Double> displacement = new ArrayList<>(80000);
        List<Double> speed = new ArrayList<>(80000);
        List<Double> rpm = new ArrayList<>(80000);
        List<Double> throttle = new ArrayList<>(80000);
        List<Double> anomaly = new ArrayList<>(80000);


        recordStreamer.setTelemetryRecordListener(record -> {
            if (record.getCarNumber().equals("13")) {
                times.add(record.getTimeOfDay());
                displacement.add(record.getLapDistance());
                speed.add(record.getVehicleSpeed());
                rpm.add(record.getEngineSpeed());
                throttle.add(record.getThrottle());
                AnomalyLabel anomalyForCarAt = AnomalyLabelsBank.getAnomalyForCarAt(record.getCarNumber(), record.getTimeOfDayLong());
                if (anomalyForCarAt != null) {
                    anomaly.add(50D);
                } else {
                    anomaly.add(0d);
                }
            }
        });


        recordStreamer.setStreamEndListener(tag -> {
            LOG.info("End of stream");
            try {
                BufferedWriter br = new BufferedWriter(
                        new FileWriter(new File("car13_all_time.csv")));
                for (int i = 0; i < displacement.size(); i++) {
                    br.write(times.get(i)
                            + "," + speed.get(i)
                            + "," + rpm.get(i)
                            + "," + throttle.get(i)
                            + "," + displacement.get(i)
                            + "," + anomaly.get(i)
                    );
                    br.newLine();
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    /*
      Adding a policy to skip all records until a non zero record is met for the first time
     */
        recordStreamer.addRecordAcceptPolicy(TelemetryRecord.class,
                new AbstractRecordAcceptPolicy<TelemetryRecord>() {

                    HashMap<String, Boolean> metFirstNonZero = new HashMap<>();

                    @Override
                    public boolean evaluate(TelemetryRecord record) {
                        if (metFirstNonZero.getOrDefault(record.getCarNumber(), false)) {
                            return true;
                        } else if (record.getLapDistance() > 0) {
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
