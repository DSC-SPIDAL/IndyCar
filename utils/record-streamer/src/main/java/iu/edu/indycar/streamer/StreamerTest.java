package iu.edu.indycar.streamer;

import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class StreamerTest {

    private static final Logger LOG = LogManager.getLogger(StreamerTest.class);

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, false, 10000000, s -> s.split("_")[2]);


        final HashMap<String, TelemetryRecord> prevRecords = new HashMap<>();
        final HashMap<String, List<String>> recordsMissing = new HashMap<>();

        final HashMap<String, Long> lastTimeStamps = new HashMap<>();
        final AtomicLong finalTimeStamp = new AtomicLong();
        recordStreamer.setTelemetryRecordListener(record -> {
            TelemetryRecord previous = prevRecords.put(record.getCarNumber(), record);
            lastTimeStamps.put(record.getCarNumber(), record.getTimeOfDayLong());
            if (finalTimeStamp.get() < record.getTimeOfDayLong()) {
                finalTimeStamp.set(record.getTimeOfDayLong());
            }
            if (previous != null) {
                double distanceDiff = 0;
                if (previous.getLapDistance() > record.getLapDistance()) {
                    distanceDiff = record.getLapDistance() + (4200 - previous.getLapDistance());
                } else {
                    distanceDiff = record.getLapDistance() - previous.getLapDistance();
                }

                if (record.getTimeOfDayLong() - previous.getTimeOfDayLong() > 2000) {
                    recordsMissing.computeIfAbsent(
                            record.getCarNumber(), s -> new ArrayList<>()
                    ).add(
                            String.format("[%d]sec -> (d: %f->%f, spd: %f->%f, rpm: %f->%f, thr: %f->%f) [%s]",
                                    (record.getTimeOfDayLong() - previous.getTimeOfDayLong()) / 1000,
                                    previous.getLapDistance(),
                                    record.getLapDistance(),
                                    previous.getVehicleSpeed(),
                                    record.getVehicleSpeed(),
                                    previous.getEngineSpeed(),
                                    record.getEngineSpeed(),
                                    previous.getThrottle(),
                                    record.getLapDistance(),
                                    distanceDiff > 250 ? "Missing" : "Delayed"
                            )
                    );
                }
            }
        });

        recordStreamer.addRecordAcceptPolicy(CompleteLapRecord.class, new AbstractRecordAcceptPolicy<CompleteLapRecord>() {
            @Override
            public boolean evaluate(CompleteLapRecord record) {
                return record.getElapsedTime() != 0;
            }
        });

        recordStreamer.setStreamEndListener(tag -> {
            LOG.info("End of stream");

            recordsMissing.keySet().stream().filter(s -> !s.contains("S")).sorted().forEach((k) -> {
                System.out.println("Car : " + k);
                recordsMissing.get(k).forEach(System.out::println);
                System.out.println();
            });

            System.out.println();
            System.out.println("Possible Crashes");

            lastTimeStamps.forEach((k, v) -> {
                long diff = (finalTimeStamp.get() - v) / 1000;
                System.out.println(String.format("%s : %d [%s]", k, diff, diff > 60 * 15 ? "Crashed" : "Finished"));
            });

//            tt.cancel();
//            averageHashMap.forEach((k, v) -> {
//                LOG.info(k + " : " + (v.records / v.count));
//            });
        });


    /*
      Adding a policy to skip all records until a non zero record is met for the first time
     */

        final long startTime = TimeUtils.convertTimestampToLong("16:23:00.000");

        recordStreamer.addRecordAcceptPolicy(TelemetryRecord.class,
                new AbstractRecordAcceptPolicy<TelemetryRecord>() {

                    HashMap<String, Boolean> metFirstNonZero = new HashMap<>();

                    @Override
                    public boolean evaluate(TelemetryRecord record) {
                        if (metFirstNonZero.containsKey(record.getCarNumber())) {
                            return true;
                        } else if (record.getTimeOfDayLong() > startTime) {
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
