package iu.edu.indycar;

import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.StreamEndListener;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import iu.edu.indycar.tmp.LatencyCalculator;
import iu.edu.indycar.tmp.RecordPublisher;
import iu.edu.indycar.tmp.RecordWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PositionStreamer {

    private final static Logger LOG = LogManager.getLogger(PositionStreamer.class);

    private final ServerBoot serverBoot;

    private final RecordPublisher recordPublisher;
    private final StreamEndListener streamEndlistener;

    private RecordWriter recordWriter;

    private HashMap<String, Boolean> foundFirstNonZero = new HashMap<>();

    private HashMap<String, AtomicLong> carCounter = new HashMap<>();

    private static int resets = 0;

    public PositionStreamer(ServerBoot serverBoot,
                            RecordPublisher recordPublisher,
                            StreamEndListener streamEndListener) {
        this.serverBoot = serverBoot;
        this.recordPublisher = recordPublisher;

        this.streamEndlistener = s -> {
            this.stop();
            streamEndListener.onStreamEnd();
        };

        try {
            this.recordWriter = new RecordWriter("/tmp/records_in_" + resets++);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.recordWriter.close();
    }

    public void start(String filePath) {
        RecordStreamer recordStreamer = new RecordStreamer(
                new File(filePath),
                true,
                1,
                s -> s.split("_")[2]
        );

        recordStreamer.addRecordAcceptPolicy(
                TelemetryRecord.class,
                new AbstractRecordAcceptPolicy<TelemetryRecord>() {
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
                }
        );

        recordStreamer.setTelemetryRecordListener(telemetryRecord -> {

            AtomicLong atomicInteger = carCounter.computeIfAbsent(
                    telemetryRecord.getCarNumber(), (s) -> new AtomicLong());
            try {
                long counter = atomicInteger.getAndIncrement();
                this.recordPublisher.publishRecord(telemetryRecord.getCarNumber(),
                        String.format("%f,%f,%f,%d,%f,%s",
                                telemetryRecord.getVehicleSpeed(),
                                telemetryRecord.getEngineSpeed(),
                                telemetryRecord.getThrottle(),
                                counter,
                                telemetryRecord.getLapDistance(),
                                "5/27/18 " + telemetryRecord.getTimeOfDay()
                        ));
                LatencyCalculator.addSent(telemetryRecord.getCarNumber() + "_" + counter);
//                this.recordWriter.write(
//                        telemetryRecord.getCarNumber(),
//                        String.valueOf(counter),
//                        telemetryRecord.getLapDistance(),
//                        telemetryRecord.getTimeOfDay(),
//                        telemetryRecord.getVehicleSpeed(),
//                        telemetryRecord.getEngineSpeed(),
//                        telemetryRecord.getThrottle()
//                );
            } catch (MqttException e) {
                LOG.error("Error occurred when publishing telemetry data", e);
            }
        });

        recordStreamer.setWeatherRecordListener(this.serverBoot::publishWeatherEvent);

        //Entry records
        recordStreamer.setEntryRecordRecordListener(this.serverBoot::publishEntryRecord);

        recordStreamer.setCompleteLapRecordRecordListener(this.serverBoot::publishCompletedLapRecord);

        recordStreamer.setStreamEndListener(this.streamEndlistener);

        recordStreamer.start();
    }
}
