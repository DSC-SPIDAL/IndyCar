package iu.edu.indycar.streamer.experiments;

import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.TimeUtils;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MetricBounds {

    private static final Logger LOG = LogManager.getLogger(MetricBounds.class);

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 100000000, s -> s.split("_")[2]);


        class Tuple {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            void report(double val) {
                if (val < min) {
                    min = val;
                }

                if (val > max) {
                    max = val;
                }
            }

            @Override
            public String toString() {
                return min + "," + max;
            }
        }

        class MinMax {
            Tuple speed = new Tuple();
            Tuple rpm = new Tuple();
            Tuple throttle = new Tuple();
            Tuple distanceFromStart = new Tuple();
        }

        HashMap<String, MinMax> minMaxHashMap = new HashMap<>();

        recordStreamer.setTelemetryRecordListener(record -> {
            MinMax minMax = minMaxHashMap.computeIfAbsent(record.getCarNumber(), s -> new MinMax());

            minMax.speed.report(record.getVehicleSpeed());
            minMax.rpm.report(record.getEngineSpeed());
            minMax.throttle.report(record.getThrottle());
            minMax.distanceFromStart.report(record.getLapDistance());
        });


        recordStreamer.setStreamEndListener(tag -> {
            LOG.info("End of stream");
            try {
                BufferedWriter br = new BufferedWriter(
                        new FileWriter(new File("min_max.csv")));
                minMaxHashMap.forEach((k, v) -> {
                    try {
                        br.write(k + "," + v.speed + "," + v.rpm + "," + v.throttle + "," + v.distanceFromStart);
                        br.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });



    /*
      Adding a policy to skip all records until a non zero record is met for the first time
     */

        long startTime = TimeUtils.convertTimestampToLong("16:23:00.000");
        recordStreamer.addRecordAcceptPolicy(TelemetryRecord.class,
                new AbstractRecordAcceptPolicy<TelemetryRecord>() {

                    HashMap<String, Boolean> metFirstNonZero = new HashMap<>();

                    @Override
                    public boolean evaluate(TelemetryRecord record) {
                        if (metFirstNonZero.getOrDefault(record.getCarNumber(), false)) {
                            return true;
                        } else if (record.getTimeOfDayLong() > startTime
                                && record.getLapDistance() > 0 && record.getLapDistance() < 3000) {
                            //start streaming for this car
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
