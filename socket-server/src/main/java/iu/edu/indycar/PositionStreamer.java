package iu.edu.indycar;

import iu.edu.indycar.models.CarPositionMessage;
import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import iu.edu.indycar.tmp.RecordPublisher;
import iu.edu.indycar.tmp.RecordWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PositionStreamer {

    private final static Logger LOG = LogManager.getLogger(PositionStreamer.class);

    private final ServerBoot serverBoot;

    private final CarPositionMessage carPositionMessage;
    private final RecordPublisher recordPublisher;

    private long lastPublishedTime = 0;

    private final long rate = 1000 / 60;// fps

    private RecordWriter recordWriter;

    public PositionStreamer(ServerBoot serverBoot, RecordPublisher recordPublisher) {
        this.serverBoot = serverBoot;
        this.carPositionMessage = new CarPositionMessage();
        this.recordPublisher = recordPublisher;
        try {
            this.recordWriter = new RecordWriter("/tmp/records_in");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(String filePath) {
        RecordStreamer recordStreamer = new RecordStreamer(
                new File(filePath),
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

        final HashMap<String, AtomicInteger> carCounter = new HashMap<>();

        recordStreamer.setTelemetryRecordListener(telemetryRecord -> {

            AtomicInteger atomicInteger = carCounter.computeIfAbsent(telemetryRecord.getCarNumber(), (s) -> new AtomicInteger());
            try {
                int counter = atomicInteger.getAndIncrement();
                this.recordPublisher.publishRecord(telemetryRecord.getCarNumber(),
                        String.format("%f,%f,%f,%d,%f,%s",
                                telemetryRecord.getVehicleSpeed(),
                                telemetryRecord.getEngineSpeed(),
                                telemetryRecord.getThrottle(),
                                counter,
                                telemetryRecord.getLapDistance(),
                                "5/27/18 " + telemetryRecord.getTimeOfDay()
                        ));
                this.recordWriter.write(telemetryRecord.getCarNumber() + "_" + counter);
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //this.serverBoot.publishPositionEvent(this.carPositionMessage);
//            if (System.currentTimeMillis() - lastPublishedTime >= rate) {
//                this.carPositionMessage.recordPosition(
//                        telemetryRecord.getCarNumber(),
//                        telemetryRecord.getLapDistance(),
//                        telemetryRecord.getTimeField(),
//                        telemetryRecord.getTimeField() - lastTimes.getOrDefault(telemetryRecord.getCarNumber(), telemetryRecord.getTimeField())
//                );
//
//                carPositionMessage.incrementSequence();
//                this.serverBoot.publishPositionEvent(carPositionMessage);
//                lastPublishedTime = System.currentTimeMillis();
//                lastTimes.put(telemetryRecord.getCarNumber(), telemetryRecord.getTimeField());
//            }
        });

        recordStreamer.setWeatherRecordListener(this.serverBoot::publishWeatherEvent);

        //Entry records
        recordStreamer.setEntryRecordRecordListener(this.serverBoot::publishEntryRecord);

        recordStreamer.setCompleteLapRecordRecordListener(this.serverBoot::publishCompletedLapRecord);

        recordStreamer.start();
    }
}
