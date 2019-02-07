package iu.edu.indycar.streamer;

import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class StreamerTest {

    private static final Logger LOG = LogManager.getLogger(StreamerTest.class);

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 100000, s -> s.split("_")[2]);

//        ConcurrentHashMap<String, Long> lastRecordTime = new ConcurrentHashMap<>();
//
//        class Average {
//            long records;
//            long count;
//        }
//
//        HashMap<String, Average> averageHashMap = new HashMap<>();
//
//        TimerTask tt = new TimerTask() {
//            @Override
//            public void run() {
//                ConcurrentHashMap.KeySetView<String, Long> strings = lastRecordTime.keySet();
//                for (String carNumber : strings) {
//                    long recsPerSec = lastRecordTime.get(carNumber);
//                    Average average = averageHashMap.computeIfAbsent(carNumber, (k) -> new Average());
//                    average.records += recsPerSec;
//                    average.count++;
//                    lastRecordTime.remove(carNumber);
//                }
//            }
//        };
//
//        new Timer().schedule(tt, 0, 1000);

        HashMap<String, Long> carTime = new HashMap<>();

        recordStreamer.setTelemetryRecordListener(record -> {

            if (record.getCarNumber().equals("10")) {
                System.out.println(record.getTimeOfDay());
            }

            Long old = carTime.put(record.getCarNumber(), record.getTimeField());
            if (old != null && old > record.getTimeField()) {
                System.out.println("Out of order time for car " + record.getCarNumber()
                        + " : " + old + "," + record.getTimeField());
            }

//            long atomicLong = lastRecordTime.getOrDefault(record.getCarNumber(), 0L);
//            lastRecordTime.put(record.getCarNumber(), ++atomicLong);

            //System.out.println(record.getCarNumber() + ":" + record.getVehicleSpeed());
//      System.out.println(record.getCarNumber());
//      System.out.println(record.getTimeOfDay());
//      System.out.println(record.getEngineSpeed());
//      System.out.println(record.getLapDistance());
//      System.out.println(record.getThrottle());
//      System.out.println(record.getThrottle());
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
                        } else if (record.getVehicleSpeed() > 10) {
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
