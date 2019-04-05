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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GapsOfRecords {

    private static final Logger LOG = LogManager.getLogger(GapsOfRecords.class);

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 100000000, s -> s.split("_")[2]);


        AtomicLong previous = new AtomicLong(-1);

        HashMap<Long, AtomicInteger> gaps = new HashMap<>();

        recordStreamer.setTelemetryRecordListener(record -> {
            if (record.getCarNumber().equals("26")) {
                if (previous.get() == -1) {
                    previous.set(record.getTimeOfDayLong());
                } else {
                    long previousTime = previous.get();
                    long timeGap = ((record.getTimeOfDayLong() - previousTime) / 100) * 100;//100ms gaps

                    gaps.computeIfAbsent(timeGap, s -> new AtomicInteger()).incrementAndGet();
                    previous.set(record.getTimeOfDayLong());
                }
            }
        });


        recordStreamer.setStreamEndListener(tag -> {
            LOG.info("End of stream");
            try {
                BufferedWriter br = new BufferedWriter(
                        new FileWriter(new File("car26_gaps.csv")));

                long minGap = gaps.keySet().stream().min(Long::compareTo).get();
                long maxGap = gaps.keySet().stream().max(Long::compareTo).get();

                gaps.keySet().stream().sorted().forEach(key->{
                    try {
                        br.write(key + "," + gaps.get(key).get());
                        br.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

//                for (long i = minGap; i <= maxGap; i += 100) {
//
//                    br.write(i + "," + gaps.getOrDefault(i, new AtomicInteger(0)).get());
//                    gaps.remove(i);
//                    br.newLine();
//                }
//                System.out.println(gaps.size() + " left");
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
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
