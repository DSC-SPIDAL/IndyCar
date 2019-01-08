package iu.edu.indycar;

import iu.edu.indycar.models.CarPositionMessage;
import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;

public class PositionStreamer {

    private final static Logger LOG = LogManager.getLogger(PositionStreamer.class);

    private final ServerBoot serverBoot;

    private final CarPositionMessage carPositionMessage;

    private long lastPublishedTime = 0;

    private final long rate = 1000 / 60;// fps

    public PositionStreamer(ServerBoot serverBoot) {
        this.serverBoot = serverBoot;
        this.carPositionMessage = new CarPositionMessage();
    }

    public void start() {
        RecordStreamer recordStreamer = new RecordStreamer(
                new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-16_0.log"),
                true,
                1,
                s -> s.split("_")[2]
        );


        recordStreamer.addRecordAcceptPolicy(TelemetryRecord.class, new AbstractRecordAcceptPolicy<TelemetryRecord>() {
            private final HashMap<String, Boolean> foundFirstNonZero = new HashMap<>();

            @Override
            public boolean evaluate(TelemetryRecord indycarRecord) {
                if (foundFirstNonZero.containsKey(indycarRecord.getCarNumber())) {
                    return true;
                } else if (indycarRecord.getLapDistance() != 0) {
                    foundFirstNonZero.put(indycarRecord.getCarNumber(), true);
                    return true;
                }
                return false;
            }
        });

        final HashMap<String, Long> lastTimes = new HashMap<>();

        recordStreamer.setTelemetryRecordListener(telemetryRecord -> {
            //this.serverBoot.publishPositionEvent(this.carPositionMessage);
            if (System.currentTimeMillis() - lastPublishedTime >= rate) {

                this.carPositionMessage.recordPosition(
                        telemetryRecord.getCarNumber(),
                        telemetryRecord.getLapDistance(),
                        telemetryRecord.getTimeField(),
                        telemetryRecord.getTimeField() - lastTimes.getOrDefault(telemetryRecord.getCarNumber(), telemetryRecord.getTimeField())
                );

                carPositionMessage.incrementSequence();
                this.serverBoot.publishPositionEvent(carPositionMessage);
                lastPublishedTime = System.currentTimeMillis();
                lastTimes.put(telemetryRecord.getCarNumber(), telemetryRecord.getTimeField());
            }
        });

        recordStreamer.setWeatherRecordListener(weatherRecord -> {
            this.serverBoot.publishWeatherEvent(weatherRecord);
        });

        recordStreamer.start();
    }
}
