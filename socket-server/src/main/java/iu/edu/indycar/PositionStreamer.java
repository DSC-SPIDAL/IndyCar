package iu.edu.indycar;

import iu.edu.indycar.models.AnomalyLabel;
import iu.edu.indycar.models.CarPositionRecord;
import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.StreamEndListener;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import iu.edu.indycar.tmp.AnomalyLabelsBank;
import iu.edu.indycar.tmp.LatencyCalculator;
import iu.edu.indycar.mqtt.MQTTClient;
import iu.edu.indycar.tmp.RecordWriter;
import iu.edu.indycar.ws.ServerBoot;
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

    private final MQTTClient mqttClient;
    private final StreamEndListener streamEndlistener;

    private RecordWriter recordWriter;

    private HashMap<String, Boolean> foundFirstNonZero = new HashMap<>();

    private HashMap<String, AtomicLong> carCounter = new HashMap<>();

    private static int resets = 0;

    private RecordStreamer recordStreamer;

    public PositionStreamer(ServerBoot serverBoot,
                            MQTTClient mqttClient,
                            StreamEndListener streamEndListener) {
        this.serverBoot = serverBoot;
        this.mqttClient = mqttClient;

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
        if (this.recordStreamer != null) {
            this.recordStreamer.stop();
        }
    }

    public void start(String filePath) {
        this.recordStreamer = new RecordStreamer(
                new File(filePath),
                true,
                1,
                s -> s.split("_")[2]
        );

        LOG.info("Streaming data for {} cars", ServerConstants.DEBUG_CARS);

        recordStreamer.addRecordAcceptPolicy(
                TelemetryRecord.class,
                new AbstractRecordAcceptPolicy<TelemetryRecord>() {
                    @Override
                    public boolean evaluate(TelemetryRecord indycarRecord) {
                        if (foundFirstNonZero.containsKey(indycarRecord.getCarNumber())) {
                            return true;
                        } else if (indycarRecord.getLapDistance() != 0 && (!ServerConstants.DEBUG_MODE || foundFirstNonZero.size() < ServerConstants.DEBUG_CARS)) {
                            foundFirstNonZero.put(indycarRecord.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                }
        );


        recordStreamer.setTelemetryRecordListener(telemetryRecord -> {

            //todo change if not necessary, normalizing lap distance
            if (telemetryRecord.getLapDistance() > 4400) {
                telemetryRecord.setLapDistance(Math.min(4023 * 2,
                        telemetryRecord.getLapDistance()) % 4023);
            } else {
                telemetryRecord.setLapDistance(Math.min(4023, telemetryRecord.getLapDistance()));
            }

            AtomicLong atomicInteger = carCounter.computeIfAbsent(
                    telemetryRecord.getCarNumber(), (s) -> new AtomicLong());
            try {
                long counter = atomicInteger.getAndIncrement();
                String uuid = telemetryRecord.getCarNumber() + "_" + counter;


                if (ServerConstants.DIRECT_STREAM_DISTANCE) {
                    if (!ServerConstants.DIRECT_STREAM_CAR_FILTER
                            || ServerConstants.DIRECT_STREAM_CARS.contains(telemetryRecord.getCarNumber())) {
                        AnomalyLabel anomalyForCarAt = AnomalyLabelsBank.getAnomalyForCarAt(
                                telemetryRecord.getCarNumber(), telemetryRecord.getTimeOfDayLong());
                        CarPositionRecord cpr = new CarPositionRecord(
                                telemetryRecord.getLapDistance(),
                                telemetryRecord.getTimeField(),
                                telemetryRecord.getCarNumber(),
                                anomalyForCarAt
                        );
                        this.serverBoot.publishPositionEvent(cpr, counter);
                    }
                }

                if (!ServerConstants.DEBUG_MODE) {
                    this.mqttClient.publishRecord(
                            telemetryRecord.getCarNumber(),
                            String.format("%f,%f,%f,%d,%f,%s",
                                    telemetryRecord.getVehicleSpeed(),
                                    telemetryRecord.getEngineSpeed(),
                                    telemetryRecord.getThrottle(),
                                    counter,
                                    telemetryRecord.getLapDistance(),
                                    "5/27/18 " + telemetryRecord.getTimeOfDay()
                            )
                    );
                } else {
                    this.mqttClient.publishRecord(
                            telemetryRecord.getCarNumber(),
                            String.format("%s,%f,%f,%f,%d,%f,%s,%s",
                                    uuid,
                                    telemetryRecord.getVehicleSpeed(),
                                    telemetryRecord.getEngineSpeed(),
                                    telemetryRecord.getThrottle(),
                                    counter,
                                    telemetryRecord.getLapDistance(),
                                    "5/27/18 " + telemetryRecord.getTimeOfDay(),
                                    telemetryRecord.getCarNumber()
                            )
                    );
                }
                LatencyCalculator.addSent(uuid);
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

        recordStreamer.setCompleteLapRecordRecordListener(completeLapRecord -> {
            try {
                this.serverBoot.publishCompletedLapRecord(completeLapRecord);
            } catch (InterruptedException e) {
                LOG.warn("Error in submitting lap record", e);
            }
        });

        recordStreamer.setStreamEndListener(this.streamEndlistener);

        recordStreamer.start();
    }
}
