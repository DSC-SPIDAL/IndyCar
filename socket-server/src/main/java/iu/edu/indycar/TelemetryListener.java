package iu.edu.indycar;

import iu.edu.indycar.models.Anomaly;
import iu.edu.indycar.models.AnomalyLabel;
import iu.edu.indycar.models.AnomalyMessage;
import iu.edu.indycar.models.CarPositionRecord;
import iu.edu.indycar.streamer.RecordTiming;
import iu.edu.indycar.streamer.TimeUtils;
import iu.edu.indycar.tmp.*;
import iu.edu.indycar.ws.ServerBoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TelemetryListener implements Runnable {

    private final static Logger LOG = LogManager.getLogger(TelemetryListener.class);

    private ServerBoot serverBoot;

    private RecordWriter recordWriter;

    private Random random = new Random();

    private BlockingQueue<byte[]> msgs = new LinkedBlockingQueue<>();

    private HashMap<String, Boolean> firstRecordDetected = new HashMap<>();
    private HashMap<String, RecordTiming> recordTimingHashMap = new HashMap<>();

    private AtomicInteger messagesToQueue = new AtomicInteger(8 * 30 * 8);

    private boolean stopped;

    public TelemetryListener(ServerBoot serverBoot) {
        this.serverBoot = serverBoot;
        //this.initRecordWriter();
    }

    private void initRecordWriter() {
        try {
            this.recordWriter = new RecordWriter("/tmp/records_out_" + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enqueueForBroadcast(WSMessage wsMessage) throws InterruptedException {
        String carNumber = wsMessage.getCarPositionRecord().getCarNumber();

        this.recordTimingHashMap.computeIfAbsent(carNumber, (k) -> {
            RecordTiming rt = new RecordTiming(carNumber, (r) -> {
                WSMessage message = (WSMessage) r;
                if (!ServerConstants.DIRECT_STREAM_DISTANCE) {
                    serverBoot.publishPositionEvent(
                            message.getCarPositionRecord(),
                            message.getCounter()
                    );
                }
                serverBoot.publishAnomalyEvent(message.getAnomalyMessage());
            }, 1, (s) -> {
                LOG.info("Real-timing Stream ended for car", carNumber);
            }, messagesToQueue.get() <= 0);
            rt.setPollTimeout(5);
            return rt;
        }).enqueue(wsMessage);

        if (messagesToQueue.decrementAndGet() == 0) {
            LOG.info("Starting real-timers after buffering...");
            this.recordTimingHashMap.values().forEach(RecordTiming::start);
            serverBoot.sendReloadEvent();
        }
    }


    private void processMessage(String payload) {
        try {
            JSONObject jsonObject = getJSONMessage(payload);

            String uuid = jsonObject.getString("UUID");

            String carNumber = jsonObject.getString("carNumber");
            long counter = Long.valueOf(uuid.split("_")[1]);

            if (counter == 0) {
                this.firstRecordDetected.put(carNumber, true);
                System.out.println("First record for car " + carNumber);
            }

            if (counter != 0 && !firstRecordDetected.getOrDefault(carNumber, false)) {
                System.out.println("Drop records for " + carNumber);
                //return;
            }

            String timeOfDay = jsonObject.getString("timeOfDay");
            long timeOfDayLong = TimeUtils.convertTimestampToLong(timeOfDay);
            int carNumberInt = Integer.parseInt(carNumber);
            double lapDistance = jsonObject.getDouble("lapDistance");
            double vehicleSpeed = Double.valueOf(jsonObject.getString("vehicleSpeed"));
            double throttle = Double.valueOf(jsonObject.getString("throttle"));
            double rpm = Double.valueOf(jsonObject.getString("engineSpeed"));

            AnomalyLabel anomalyLabel = AnomalyLabelsBank.getAnomalyForCarAt(carNumber, timeOfDayLong);

            AnomalyMessage anomalyMessage = new AnomalyMessage();
            anomalyMessage.setIndex(timeOfDayLong);
            anomalyMessage.setTimeOfDayString(timeOfDay);
            anomalyMessage.setCarNumber(carNumberInt);
            anomalyMessage.setAnomalyLabel(anomalyLabel);

            Anomaly speedAnomaly = new Anomaly();
            speedAnomaly.setAnomalyType("SPEED");
            speedAnomaly.setRawData(vehicleSpeed);
            speedAnomaly.setAnomaly(jsonObject.getDouble("vehicleSpeedAnomaly"));
            anomalyMessage.addAnomaly(speedAnomaly);


            Anomaly throttleAnomaly = new Anomaly();
            throttleAnomaly.setAnomalyType("THROTTLE");
            throttleAnomaly.setRawData(throttle);
            throttleAnomaly.setAnomaly(jsonObject.getDouble("throttleAnomaly"));
            anomalyMessage.addAnomaly(throttleAnomaly);

            Anomaly rpmAnomaly = new Anomaly();
            rpmAnomaly.setAnomalyType("RPM");
            rpmAnomaly.setRawData(rpm);
            rpmAnomaly.setAnomaly(jsonObject.getDouble("engineSpeedAnomaly"));
            anomalyMessage.addAnomaly(rpmAnomaly);


            WSMessage wsMessage = new WSMessage(
                    counter,
                    new CarPositionRecord(lapDistance, timeOfDayLong, carNumber, anomalyLabel),
                    anomalyMessage
            );
            this.enqueueForBroadcast(wsMessage);


            LatencyCalculator.addRecv(uuid);

            if (anomalyLabel != null) {
                AnomalyLogger.AnomalyLabelDocument anomalyLabelDocument = AnomalyLogger.get(
                        carNumber,
                        anomalyLabel.getLabel(),
                        anomalyLabel.getUuid()
                );
                anomalyLabelDocument.record(
                        speedAnomaly.getRawData(),
                        speedAnomaly.getAnomaly(),
                        rpmAnomaly.getRawData(),
                        rpmAnomaly.getAnomaly(),
                        throttleAnomaly.getRawData(),
                        throttleAnomaly.getAnomaly(),
                        timeOfDayLong,
                        counter
                );
            }


//                    recordWriter.write(
//                            carNumber,
//                            counter,
//                            lapDistance,
//                            timeOfDay,
//                            vehicleSpeed,
//                            rpm,
//                            throttle
//                    );
        } catch (Exception ex) {
            LOG.error("Skipping a record from pub/sub", ex);
        }
    }

    private JSONObject getJSONMessage(String msg) {
        if (!ServerConstants.DEBUG_MODE) {
            return new JSONObject(msg);
        } else {
            String[] splits = msg.split(",");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("UUID", splits[0]);
            jsonObject.put("vehicleSpeed", splits[1]);
            jsonObject.put("engineSpeed", splits[2]);
            jsonObject.put("throttle", splits[3]);
            jsonObject.put("counter", splits[4]);
            jsonObject.put("lapDistance", Double.parseDouble(splits[5]));
            jsonObject.put("timeOfDay", splits[6]);
            jsonObject.put("carNumber", splits[7]);
            jsonObject.put("vehicleSpeedAnomaly", this.random.nextDouble());
            jsonObject.put("engineSpeedAnomaly", this.random.nextDouble());
            jsonObject.put("throttleAnomaly", this.random.nextDouble());
            return jsonObject;
        }
    }

    public void start() {
        LOG.info("Starting telemetry listener...");
        new Thread(this).start();
    }

    public void onTelemetryMessage(MqttMessage mqttMessage) {
        if (this.stopped) {
            LOG.info("Lister has stopped. Not accepting messages.");
            return;
        }
        this.msgs.add(mqttMessage.getPayload());
    }

    public void close() {
        this.stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                byte[] msg = msgs.poll(1, TimeUnit.MINUTES);
                if (msg != null) {
                    String payload = new String(msg);
                    processMessage(payload);
                }
            } catch (InterruptedException e) {
                LOG.error("Error occurred when polling for messages", e);
            }
        }
    }
}
