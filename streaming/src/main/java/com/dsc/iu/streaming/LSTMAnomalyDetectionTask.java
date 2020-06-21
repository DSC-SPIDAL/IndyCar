package com.dsc.iu.streaming;

import com.dsc.iu.utils.OnlineLearningUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class LSTMAnomalyDetectionTask extends BaseRichSpout implements MQTTMessageCallback {

    private final static Logger LOG = LogManager.getLogger(AnomalyDetectionTask.class);


    private final static String METRIC_VEHICLE_SPEED = "vehicleSpeed";
    private final static String METRIC_ENGINE_SPEED = "engineSpeed";
    private final static String METRIC_THROTTLE = "throttle";

    private final static String PARAM_CAR_NUMBER = "carNumber";
    private final static String PARAM_LAP_DISTANCE = "lapDistance";
    private final static String PARAM_TIME_OF_DAY = "timeOfDay";
    private final static String PARAM_UUID = "UUID";
    private final static String PARAM_TIME_RECV = "recvTime";
    private final static String PARAM_TIME_SEND = "sendTime";

    private final static String STR_COMMA = ",";
    private final static String STR_UCO = "_";
    private final static String STR_SPACE = " ";
    private final static String STR_ANOMALY = "Anomaly";

    private static final long serialVersionUID = 1L;
    ExecutorService executor;
    private final String inputTopic;
    private final String outputTopic;
    private final String carNumber;
    private final String[] metrics = {METRIC_VEHICLE_SPEED, METRIC_ENGINE_SPEED, METRIC_THROTTLE};
    private final Queue<JSONObject> outMessages = new ConcurrentLinkedQueue<>();
    private MqttMessage mqttmsg;
    private Map<String, LSTMAnomalyDetection> anomalyDetectionModels = null;
    private Map<String, Queue> lstmInputQueues = null;
    private final int lstmHistorySize = 150;
    private final String lstmModelsDir = "/home/sakkas/github/indycar-lstm-anomalydetection-java/models/";
    private Map<String, Map<String, Double>> inputScaleValues;
    private MQTTClientInstance mqttClientInstance;


    public LSTMAnomalyDetectionTask(String inputTopic, String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;

        //todo a temp hack for demo
        this.carNumber = inputTopic.replace("2017", "").replace("2018", "");
        LOG.info("Taking inputs from : {} and outputing to {}", inputTopic, outputTopic);
    }

    public LSTMAnomalyDetectionTask(String inputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = OnlineLearningUtils.sinkoutTopic;

        //todo a temp hack for demo
        this.carNumber = inputTopic.replace("2017", "").replace("2018", "");
    }

    private void setInputScaleValues() {
        inputScaleValues = new HashMap<String, Map<String, Double>>();
        inputScaleValues.put(METRIC_VEHICLE_SPEED, new HashMap<String, Double>());
        inputScaleValues.get(METRIC_VEHICLE_SPEED).put("min", 0.0);
        inputScaleValues.get(METRIC_VEHICLE_SPEED).put("max", 239.0);

        inputScaleValues.put(METRIC_ENGINE_SPEED, new HashMap<String, Double>());
        inputScaleValues.get(METRIC_ENGINE_SPEED).put("min", 0.0);
        inputScaleValues.get(METRIC_ENGINE_SPEED).put("max", 13217.0);

        inputScaleValues.put(METRIC_THROTTLE, new HashMap<String, Double>());
        inputScaleValues.get(METRIC_THROTTLE).put("min", 0.0);
        inputScaleValues.get(METRIC_THROTTLE).put("max", 117.0);
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        mqttClientInstance = MQTTClientInstance.getInstance();
        mqttmsg = new MqttMessage();

        this.anomalyDetectionModels = new HashMap<>();
        this.lstmInputQueues = new HashMap<>();
        setInputScaleValues();


        //instantiate the LSTM networks for all metrics
        for (String metric : metrics) {
            this.anomalyDetectionModels.put(metric, new LSTMAnomalyDetection(lstmModelsDir + metric + "/", lstmHistorySize));
            this.lstmInputQueues.put(metric, new LinkedList<Double>());
            executor = Executors.newFixedThreadPool(3);

            LOG.info("Starting LSTM model for metric {}...", metric);
        }
        LOG.info("Created all networks. Subscribing to topic...");
        try {
            mqttClientInstance.subscribe(inputTopic, this);
            LOG.info("Subscribed to topic {}", inputTopic);
        } catch (MqttException e) {
            LOG.error("Error in subscribing to topic{}", inputTopic, e);
        }
    }// do not change this part

    @Override
    //Sends results to MQTT
    public void nextTuple() {
        if (!this.outMessages.isEmpty()) {
            JSONObject msgToSend = this.outMessages.poll();

            msgToSend.put(PARAM_TIME_SEND, System.currentTimeMillis());
            mqttmsg.setPayload(msgToSend.toJSONString().getBytes());
            mqttmsg.setQos(OnlineLearningUtils.QoS);
            try {
                mqttClientInstance.sendMessage(this.outputTopic, mqttmsg); //result back here
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //nothing to do here
    }

    //LSTM Model accepts 3d float array.
    private float[][][] to3DArray(Object[] arr) {
        float[][][] arr2 = new float[1][lstmHistorySize][1];
        for (int i = 0; i < lstmHistorySize; i++) {
            arr2[0][i][0] = Float.parseFloat(arr[i].toString());
        }
        return arr2;
    }


    //LSTM models require inputs between 0 and 1.
    private double scaleInput(String metric, double val) {
        double min = inputScaleValues.get(metric).get("min");
        double max = inputScaleValues.get(metric).get("max");
        return (val - min) / (max - min);
    }

    @Override
    //Gets inputs from MQTT and runs the inference
    public void onMessage(String topic, MqttMessage mqttMessage) {
        try {
            String record = new String(mqttMessage.getPayload());
            String[] splits = record.split(STR_COMMA);
            if (splits.length == 6) {
                double speed = Double.parseDouble(splits[0]);
                double rpm = Double.parseDouble(splits[1]);
                double throttle = Double.parseDouble(splits[2]);
                int record_counter = Integer.parseInt(splits[3]);
                String lapDistance = splits[4];
                String timeOfDay = splits[5];

                JSONObject recordobj = new JSONObject();
                recordobj.put(PARAM_CAR_NUMBER, this.carNumber);
                recordobj.put(PARAM_LAP_DISTANCE, lapDistance);
                recordobj.put(PARAM_TIME_OF_DAY, timeOfDay.split(STR_SPACE)[1]);
                recordobj.put(METRIC_VEHICLE_SPEED, speed);
                recordobj.put(METRIC_ENGINE_SPEED, rpm);
                recordobj.put(METRIC_THROTTLE, throttle);


                final String uuid = this.carNumber + STR_UCO + record_counter;

                recordobj.put(PARAM_TIME_RECV, System.currentTimeMillis());
                recordobj.put(PARAM_UUID, uuid);


                //collect data in a queue. LSTM models require last 150 values.
                for (String metric : metrics) {

                    //remove the first element if there are already 150 values.
                    if (lstmInputQueues.get(metric).size() == lstmHistorySize) {
                        lstmInputQueues.get(metric).poll(); //remove first element
                    }
                    lstmInputQueues.get(metric).add(scaleInput(metric,
                            Double.parseDouble(recordobj.get(metric).toString())));
                    Object[] arr = lstmInputQueues.get(metric).toArray();

                }

                // start LSTM inference  if there are enough input for the models
                if (lstmInputQueues.get(METRIC_VEHICLE_SPEED).size() == lstmHistorySize) {
                    Map<String, Future> futureMap = new HashMap<>();
                    Future[] future = new Future[3];

                    //submit inference
                    //runs parallel
                    for (String metric : metrics) {
                        futureMap.put(metric, executor.submit(
                                new Callable() {
                                    public Double call() throws Exception {
                                        return anomalyDetectionModels.get(metric)
                                                .calculateAnomalyScore(to3DArray(lstmInputQueues.get(metric).toArray()));
                                    }
                                }
                        ));
                    }

                    //collect results
                    for (String metric : metrics) {
                        recordobj.put(metric + STR_ANOMALY, futureMap.get(metric).get());
                    }


                } else { // if there are less than 150 records, set anomaly score to 0.
                    for (String metric : metrics) {
                        recordobj.put(metric + STR_ANOMALY, 0);
                    }

                }

                outMessages.add(recordobj);

            } else {
                LOG.warn("CSV of unknown length {} received. Expected 6", splits.length);
            }
        } catch (Exception ex) {
            LOG.warn("Error in parsing record", ex);
        }
    }
}
