package iu.edu.indycar.streamer.experiments;

import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordCount {

    private static final Logger LOG = LogManager.getLogger(RecordCount.class);

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 10000000, s -> s.split("_")[2]);

        ConcurrentHashMap<String, AtomicInteger> recordsCount = new ConcurrentHashMap<>();

        Map<String, Long> firstRecordTime = new ConcurrentHashMap<>();
        Map<String, Long> lastRecordTime = new ConcurrentHashMap<>();

        recordStreamer.setTelemetryRecordListener(record -> {
            if (!firstRecordTime.containsKey(record.getCarNumber())) {
                firstRecordTime.put(record.getCarNumber(), record.getTimeOfDayLong());
            }
            lastRecordTime.put(record.getCarNumber(), record.getTimeOfDayLong());

            recordsCount.computeIfAbsent(record.getCarNumber(),
                    s -> new AtomicInteger()).incrementAndGet();
        });

        recordStreamer.setWeatherRecordListener(wr -> {
            //System.out.println(wr.getTimeOfDay());
            //System.out.println(wr.getPressure());
            //System.out.println(wr.getTemperature());
        });

        recordStreamer.setEntryRecordRecordListener(er -> {
            //System.out.println(er.getCarNumber());
        });

        recordStreamer.setCompleteLapRecordRecordListener(cr -> {
            //System.out.println(cr.getCarNumber() + "," + cr.getRank() + "," + cr.getElapsedTime());
        });

        recordStreamer.addRecordAcceptPolicy(CompleteLapRecord.class, new AbstractRecordAcceptPolicy<CompleteLapRecord>() {
            @Override
            public boolean evaluate(CompleteLapRecord record) {
                return record.getElapsedTime() != 0;
            }
        });

        recordStreamer.setStreamEndListener(tag -> {
            LOG.info("End of stream");

            recordsCount.forEach((k, v) -> {
                long timePassed = Math.max(1, lastRecordTime.get(k) - firstRecordTime.get(k));
                System.out.println(k + "," + v + ","
                        + (v.get() * 1000 / ((timePassed))));
            });


//            tt.cancel();
//            averageHashMap.forEach((k, v) -> {
//                LOG.info(k + " : " + (v.records / v.count));
//            });
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
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
