package iu.edu.indycar.streamer;

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

public class StreamerTest {

    private static final Logger LOG = LogManager.getLogger(StreamerTest.class);

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 10000000, s -> s.split("_")[2]);

        ConcurrentHashMap<String, AtomicInteger> recordsCount = new ConcurrentHashMap<>();

        Map<String, Long> firstRecordTime = new ConcurrentHashMap<>();
        Map<String, Long> lastRecordTime = new ConcurrentHashMap<>();

        AtomicInteger recordNumber = new AtomicInteger();

        recordStreamer.setTelemetryRecordListener(record -> {
            if (record.getCarNumber().equals("22")) {
                recordNumber.incrementAndGet();
                if (recordNumber.get() > 27000 && recordNumber.get() < 29000) {
                    System.out.println(recordNumber.get() + " : "
                            + record.getTimeOfDay() + "|" + record.getVehicleSpeed()
                            + "|" + record.getThrottle()
                            + "|" + record.getEngineSpeed());
                }
            }
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
            System.out.println(cr.getCarNumber()+","+cr.getCompletedLaps());
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
