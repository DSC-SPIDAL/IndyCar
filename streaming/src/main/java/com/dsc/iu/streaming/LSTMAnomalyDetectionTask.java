package com.dsc.iu.streaming;

import com.dsc.iu.utils.OnlineLearningUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.shade.com.google.common.collect.EvictingQueue;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;

import java.util.HashMap;
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
    private Map<String, EvictingQueue<Float>> lstmInputQueues = null;
    private final int lstmHistorySize = 150;
    private final String lstmModelsDir = "/conf/models/";
    private Map<String, Map<String, Float>> inputScaleValues;
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

    //The models are trained with min-max scaler. Therefore, the inputs should be scaled here as well.
    private void setInputScaleValues() {
        inputScaleValues = new HashMap<>();
        inputScaleValues.put(METRIC_VEHICLE_SPEED, new HashMap<>());
        inputScaleValues.get(METRIC_VEHICLE_SPEED).put("min", 0.0f);
        inputScaleValues.get(METRIC_VEHICLE_SPEED).put("max", 239.0f);

        inputScaleValues.put(METRIC_ENGINE_SPEED, new HashMap<>());
        inputScaleValues.get(METRIC_ENGINE_SPEED).put("min", 0.0f);
        inputScaleValues.get(METRIC_ENGINE_SPEED).put("max", 13217.0f);

        inputScaleValues.put(METRIC_THROTTLE, new HashMap<>());
        inputScaleValues.get(METRIC_THROTTLE).put("min", 0.0f);
        inputScaleValues.get(METRIC_THROTTLE).put("max", 117.0f);
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
            this.lstmInputQueues.put(metric, EvictingQueue.create(this.lstmHistorySize));
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
    }

    @Override
    //Sends results to MQTT
    public void nextTuple() {
        if (!this.outMessages.isEmpty()) {
            JSONObject msgToSend = this.outMessages.poll();

            msgToSend.put(PARAM_TIME_SEND, System.currentTimeMillis());
            mqttmsg.setPayload(msgToSend.toJSONString().getBytes());
            mqttmsg.setQos(OnlineLearningUtils.QoS);
            //System.out.println("output message" + msgToSend.toString());
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
            arr2[0][i][0] = (float)arr[i];
        }
        return arr2;
    }


    //LSTM models require inputs between 0 and 1. We use min-max scaler
    private float scaleInput(String metric, float val) {
        float min = inputScaleValues.get(metric).get("min");
        float max = inputScaleValues.get(metric).get("max");
        return (val - min) / (max - min);
    }

    @Override
    //Gets inputs from MQTT and runs the inference
    public void onMessage(String topic, MqttMessage mqttMessage) {
        try {
            String record = new String(mqttMessage.getPayload());

            //System.out.println("input message: " + record);

            String[] splits = record.split(STR_COMMA);
            if (splits.length == 6) {
                float speed = Float.parseFloat(splits[0]);
                float rpm = Float.parseFloat(splits[1]);
                float throttle = Float.parseFloat(splits[2]);
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
                    //We want records that has the same length with lstmHistorySize.
                    //Evicting queue automatically removes the first element if the queue is full.
                    lstmInputQueues.get(metric).add(scaleInput(metric, (float)recordobj.get(metric)));


                }

                // start LSTM inference  if there are enough input for the models
                if (lstmInputQueues.get(METRIC_VEHICLE_SPEED).size() == lstmHistorySize) {
                    Map<String, Future> futureMap = new HashMap<>();
                    Future[] future = new Future[3];

                    //submit the inference
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
