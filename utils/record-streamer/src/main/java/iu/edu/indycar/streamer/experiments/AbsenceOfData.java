package iu.edu.indycar.streamer.experiments;

import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AbsenceOfData {

    private static final Logger LOG = LogManager.getLogger(AbsenceOfData.class);

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 1, s -> s.split("_")[2]);


        class DistanceRecord {
            double distance;
            long timeStamp;

            public DistanceRecord(double distance, long timeStamp) {
                this.distance = distance;
                this.timeStamp = timeStamp;
            }

            DistanceRecord copy() {
                DistanceRecord distanceRecord = new DistanceRecord(distance, timeStamp);
                return distanceRecord;
            }
        }

        final List<DistanceRecord> distanceRecords = new ArrayList<>();


        recordStreamer.setTelemetryRecordListener(record -> {
            if (record.getCarNumber().equals("26")) {
                synchronized (distanceRecords) {
                    distanceRecords.add(
                            new DistanceRecord(record.getLapDistance(), record.getTimeOfDayLong())
                    );
                }
            }
        });


        List<Integer> counts = new ArrayList<>(25000);

        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                synchronized (distanceRecords) {
                    counts.add(distanceRecords.size());
                    distanceRecords.clear();
                }
            }
        };

        recordStreamer.setStreamEndListener(tag -> {
            tt.cancel();
            LOG.info("End of stream");
            try {
                BufferedWriter br = new BufferedWriter(
                        new FileWriter(new File("car26_counts.csv")));
                for (int i = 0; i < counts.size(); i++) {
                    br.write(i + "," + counts.get(i));
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
                        } else if (record.getLapDistance() > 5) {
                            if (record.getCarNumber().equals("26")) {
                                timer.schedule(tt, 0, 1000 * 60);
                            }
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
