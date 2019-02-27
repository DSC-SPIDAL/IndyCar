package iu.edu.indycar;

import iu.edu.indycar.models.Anomaly;
import iu.edu.indycar.models.AnomalyLabel;
import iu.edu.indycar.models.AnomalyMessage;
import iu.edu.indycar.models.CarPositionRecord;
import iu.edu.indycar.streamer.RecordTiming;
import iu.edu.indycar.streamer.TimeUtils;
import iu.edu.indycar.tmp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MQTTTelemetryListener {

    private final static Logger LOG = LogManager.getLogger(MQTTTelemetryListener.class);

    private final String topic;

    private ServerBoot serverBoot;

    private RecordWriter recordWriter;

    private int rests = 0;//keeps number of rests

    private Random random = new Random();

    private ArrayBlockingQueue<byte[]> msgs = new ArrayBlockingQueue<>(1000);
    private final ExecutorService executorService = Executors.newFixedThreadPool(ServerConstants.EVENT_EMITTER_THREADS);

    private HashMap<String, Boolean> firstRecordDetected = new HashMap<>();
    private HashMap<String, RecordTiming> recordTimingHashMap = new HashMap<>();

    private AtomicInteger messagesToQueue = new AtomicInteger(33 * 30 * 8);

    public MQTTTelemetryListener(ServerBoot serverBoot, String subscription) {
        System.out.println(subscription);
        this.serverBoot = serverBoot;
        this.topic = subscription;
        this.initRecordWriter();
        for (int i = 0; i < ServerConstants.EVENT_EMITTER_THREADS; i++) {
            executorService.submit(() -> {
                while (true) {
                    try {
                        byte[] msg = msgs.poll(1, TimeUnit.SECONDS);
                        if (msg != null) {
                            String payload = new String(msg);
                            processMessage(payload);
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Error occurred when polling for messages", e);
                    }
                }
            });
        }
    }

    private void initRecordWriter() {
        try {
            this.recordWriter = new RecordWriter("/tmp/records_out_" + rests++);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        this.recordWriter.close();
        this.firstRecordDetected.clear();

        this.recordTimingHashMap.values().forEach(RecordTiming::stop);
        this.recordTimingHashMap.clear();

        this.messagesToQueue.set(33 * 30 * 8);

        this.initRecordWriter();
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
                System.out.println("Drop");
                return;
            }

            String timeOfDay = jsonObject.getString("timeOfDay");
            long timeOfDayLong = TimeUtils.convertTimestampToLong(timeOfDay);
            int carNumberInt = Integer.parseInt(carNumber);

            double lapDistance = jsonObject.getDouble("lapDistance");


//            if (carNumber.equals("24")) {
//                System.out.println(System.currentTimeMillis() - previousTime + ":" + (timeOfDayLong - previousEventTime));
//                previousTime = System.currentTimeMillis();
//                offeset += ((System.currentTimeMillis() - previousTime) - (timeOfDayLong - previousEventTime));
//                System.out.println("offset:" + offeset);
//                previousEventTime = timeOfDayLong;
//            }


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


            this.recordTimingHashMap.computeIfAbsent(carNumber, (k) -> {
                RecordTiming rt = new RecordTiming(carNumber, (r) -> {
                    WSMessage wsMessage = (WSMessage) r;
                    if (!ServerConstants.DIRECT_STREAM_DISTANCE) {
                        serverBoot.publishPositionEvent(
                                wsMessage.getCarPositionRecord(),
                                wsMessage.getCounter()
                        );
                    }
                    serverBoot.publishAnomalyEvent(wsMessage.getAnomalyMessage());
                }, 1, (s) -> {
                    LOG.info("Stream ended for car", carNumber);
                }, messagesToQueue.get() <= 0);
                rt.setPollTimeout(5);
                return rt;
            }).enqueue(
                    new WSMessage(
                            counter,
                            new CarPositionRecord(lapDistance, timeOfDayLong, carNumber, anomalyLabel),
                            anomalyMessage
                    )
            );


            LatencyCalculator.addRecv(uuid);

            if (messagesToQueue.decrementAndGet() == 0) {
                LOG.info("Starting real-timers after buffering...");
                this.recordTimingHashMap.values().forEach(RecordTiming::start);
                serverBoot.sendReloadEvent();
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

    public void start() throws MqttException {
        MqttClient client = new MqttClient(
                ServerConstants.CONNECTION_URL,
                MqttClient.generateClientId()
        );
        MqttConnectOptions connOpts = setUpConnectionOptions(
                ServerConstants.USERNAME,
                ServerConstants.PASSWORD
        );

        client.setCallback(new MqttCallback() {


            @Override
            public void connectionLost(Throwable throwable) {
                LOG.error("Telemetry Listener's Connection lost", throwable);
                try {
                    LOG.info("Trying to reconnect...");
                    client.reconnect();
                } catch (MqttException e) {
                    LOG.error("Error in reconnecting", e);
                }
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                msgs.add(mqttMessage.getPayload());
                //LOG.info("Message arrived {} : {}", s, new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        client.connect(connOpts);

        client.subscribe(topic);
    }

    private static MqttConnectOptions setUpConnectionOptions(String username, String password) {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        connOpts.setAutomaticReconnect(true);
        connOpts.setSocketFactory(new MQTTSocketFactory());
        return connOpts;
    }
}
