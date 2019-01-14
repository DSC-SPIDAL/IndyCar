package iu.edu.indycar;

import iu.edu.indycar.models.AnomalyMessage;
import iu.edu.indycar.models.CarPositionMessage;
import iu.edu.indycar.streamer.TimeUtils;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.parsers.TelemetryRecordParser;
import iu.edu.indycar.tmp.RecordWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class MQTTTelemetryListener {

    private final static Logger LOG = LogManager.getLogger(MQTTTelemetryListener.class);

    private static final String CONNECTION_URL = "tcp://j-093.juliet.futuresystems.org:61613";
    private static final String SUBSCRIPTION = "streaming_output";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "xyi5b2YUcw8CHhAE";

    private final long rate = 1000 / 60;// fps

    private long lastPublishedTime = 0;

    private final CarPositionMessage carPositionMessage = new CarPositionMessage();

    private ServerBoot serverBoot;

    private RecordWriter recordWriter;

    public MQTTTelemetryListener(ServerBoot serverBoot) {
        this.serverBoot = serverBoot;
        try {
            this.recordWriter = new RecordWriter("/tmp/records_out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws MqttException {
        MqttClient client = new MqttClient(CONNECTION_URL, MqttClient.generateClientId());
        MqttConnectOptions connOpts = setUpConnectionOptions(USERNAME, PASSWORD);

        client.setCallback(new MqttCallback() {

            final HashMap<String, Long> lastTimes = new HashMap<>();


            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                try {
                    String csv = new String(mqttMessage.getPayload());

                    JSONObject jsonObject = new JSONObject(csv);

                    String timeOfDay = jsonObject.getString("timeOfDay");
                    long timeOfDayLong = TimeUtils.convertTimestampToLong(timeOfDay);
                    String carNumber = jsonObject.getString("carNumber");
                    int carNumberInt = Integer.parseInt(carNumber);


                    if (System.currentTimeMillis() - lastPublishedTime >= rate) {
                        carPositionMessage.recordPosition(
                                carNumber,
                                jsonObject.getDouble("lapDistance"),
                                timeOfDayLong,
                                timeOfDayLong - lastTimes.getOrDefault(
                                        carNumber, timeOfDayLong
                                )
                        );

                        carPositionMessage.incrementSequence();
                        serverBoot.publishPositionEvent(carPositionMessage);
                        lastPublishedTime = System.currentTimeMillis();
                        lastTimes.put(carNumber, timeOfDayLong);
                    }

                    AnomalyMessage speedAnomaly = new AnomalyMessage();
                    speedAnomaly.setIndex(0);
                    speedAnomaly.setAnomalyType("SPEED");
                    speedAnomaly.setCarNumber(carNumberInt);
                    speedAnomaly.setRawData(Double.valueOf(jsonObject.getString("vehicleSpeed")));
                    speedAnomaly.setAnomaly(jsonObject.getDouble("vehicleSpeedAnomaly"));


                    AnomalyMessage throttleAnomaly = new AnomalyMessage();
                    throttleAnomaly.setIndex(0);
                    throttleAnomaly.setAnomalyType("THROTTLE");
                    throttleAnomaly.setCarNumber(carNumberInt);
                    throttleAnomaly.setRawData(Double.valueOf(jsonObject.getString("throttle")));
                    throttleAnomaly.setAnomaly(jsonObject.getDouble("throttleAnomaly"));

                    AnomalyMessage rpmAnomaly = new AnomalyMessage();
                    rpmAnomaly.setIndex(0);
                    rpmAnomaly.setAnomalyType("RPM");
                    rpmAnomaly.setCarNumber(carNumberInt);
                    rpmAnomaly.setRawData(Double.valueOf(jsonObject.getString("engineSpeed")));
                    rpmAnomaly.setAnomaly(jsonObject.getDouble("engineSpeedAnomaly"));

                    serverBoot.publishAnomalyEvent(speedAnomaly);
                    serverBoot.publishAnomalyEvent(throttleAnomaly);
                    serverBoot.publishAnomalyEvent(rpmAnomaly);

                    recordWriter.write(jsonObject.getString("UUID"));
                } catch (Exception ex) {
                    System.out.println("Skipping record due to " + ex.getMessage());
                }
                //LOG.info("Message arrived {} : {}", s, new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        client.connect(connOpts);

        client.subscribe(SUBSCRIPTION);
    }

    private static MqttConnectOptions setUpConnectionOptions(String username, String password) {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        return connOpts;
    }
}
