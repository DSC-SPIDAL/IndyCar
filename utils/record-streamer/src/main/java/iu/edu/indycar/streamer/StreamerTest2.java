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

public class StreamerTest2 {

    private static final Logger LOG = LogManager.getLogger(StreamerTest2.class);

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, false, 10000000, s -> s.split("_")[2]);

        recordStreamer.setTelemetryRecordListener(record -> {
            System.out.println(record.getCarNumber() + "," + record.getTimeOfDayLong()
                    + "," + record.getVehicleSpeed() + ",Add anything");
        });


        recordStreamer.setCompleteLapRecordRecordListener(lapRecord -> {
            //use if you need this
        });


        recordStreamer.setStreamEndListener(tag -> {
            LOG.info("End of stream");
        });

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
