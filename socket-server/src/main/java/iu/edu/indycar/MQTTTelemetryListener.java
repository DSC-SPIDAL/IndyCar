package iu.edu.indycar;

import iu.edu.indycar.models.AnomalyMessage;
import iu.edu.indycar.models.CarPositionRecord;
import iu.edu.indycar.streamer.TimeUtils;
import iu.edu.indycar.tmp.LatencyCalculator;
import iu.edu.indycar.tmp.RecordWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MQTTTelemetryListener {

    private final static Logger LOG = LogManager.getLogger(MQTTTelemetryListener.class);

    private final String topic;

    private ServerBoot serverBoot;

    private RecordWriter recordWriter;

    private int rests = 0;//keeps number of rests

    private Random random = new Random();

    private ArrayBlockingQueue<byte[]> msgs = new ArrayBlockingQueue<>(1000);
    private final ExecutorService executorService = Executors.newFixedThreadPool(ServerConstants.EVENT_EMITTER_THREADS);

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
        this.initRecordWriter();
    }

    private void processMessage(String payload) {
        try {
            JSONObject jsonObject = getJSONMessage(payload);

            String counter = jsonObject.getString("UUID");

            LatencyCalculator.addRecv(counter);

            String timeOfDay = jsonObject.getString("timeOfDay");
            long timeOfDayLong = TimeUtils.convertTimestampToLong(timeOfDay);
            String carNumber = jsonObject.getString("carNumber");
            int carNumberInt = Integer.parseInt(carNumber);

            double lapDistance = jsonObject.getDouble("lapDistance");

            serverBoot.publishPositionEvent(
                    new CarPositionRecord(lapDistance, timeOfDayLong, carNumber)
            );

            double vehicleSpeed = Double.valueOf(jsonObject.getString("vehicleSpeed"));
            double throttle = Double.valueOf(jsonObject.getString("throttle"));
            double rpm = Double.valueOf(jsonObject.getString("engineSpeed"));

            AnomalyMessage speedAnomaly = new AnomalyMessage();
            speedAnomaly.setIndex(timeOfDayLong);
            speedAnomaly.setTimeOfDayString(timeOfDay);
            speedAnomaly.setAnomalyType("SPEED");
            speedAnomaly.setCarNumber(carNumberInt);
            speedAnomaly.setRawData(vehicleSpeed);
            speedAnomaly.setAnomaly(jsonObject.getDouble("vehicleSpeedAnomaly"));


            AnomalyMessage throttleAnomaly = new AnomalyMessage();
            throttleAnomaly.setIndex(timeOfDayLong);
            throttleAnomaly.setTimeOfDayString(timeOfDay);
            throttleAnomaly.setAnomalyType("THROTTLE");
            throttleAnomaly.setCarNumber(carNumberInt);
            throttleAnomaly.setRawData(throttle);
            throttleAnomaly.setAnomaly(jsonObject.getDouble("throttleAnomaly"));

            AnomalyMessage rpmAnomaly = new AnomalyMessage();
            rpmAnomaly.setIndex(timeOfDayLong);
            rpmAnomaly.setTimeOfDayString(timeOfDay);
            rpmAnomaly.setAnomalyType("RPM");
            rpmAnomaly.setCarNumber(carNumberInt);
            rpmAnomaly.setRawData(rpm);
            rpmAnomaly.setAnomaly(jsonObject.getDouble("engineSpeedAnomaly"));

            serverBoot.publishAnomalyEvent(speedAnomaly);
            serverBoot.publishAnomalyEvent(throttleAnomaly);
            serverBoot.publishAnomalyEvent(rpmAnomaly);


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
        return connOpts;
    }
}
