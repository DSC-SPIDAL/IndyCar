package iu.edu.indycar.streamer;

import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class StreamerTest {
    public static void main(String[] args) throws IOException {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, true, 100000, s -> s.split("_")[2]);

        recordStreamer.setTelemetryRecordListener(record -> {

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
            System.out.println("End of stream");
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
                        } else if (Double.valueOf(record.getVehicleSpeed()) > 10) {
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }
}
