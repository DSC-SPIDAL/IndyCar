package iu.edu.indycar.tmp;

import iu.edu.indycar.ServerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LatencyCalculator {

    private final static Logger LOG = LogManager.getLogger(LatencyCalculator.class);

    private static HashMap<String, Long> records = new HashMap<>();

    private static long maxLatency = Long.MIN_VALUE;
    private static long minLatency = Long.MAX_VALUE;
    private static long totalRecords = 0;
    private static BigInteger totalLatency = BigInteger.ZERO;

    private final static String PARAM_TIME_RECV = "recvTime"; //time storm received
    private final static String PARAM_TIME_SEND = "sendTime"; //time storm send out

    private static final List<Long> times = new ArrayList<>();

    private static final List<String> timeStamps = new ArrayList<>();

    public synchronized static void addSent(String uuid) {
        if (!ServerConstants.CALCULATE_MQTT_LATENCY) {
            return;
        }
        records.put(uuid, System.currentTimeMillis());
    }

    private static volatile BufferedWriter allWriter;

    public static void init() {
        try {
            if (allWriter != null) {
                allWriter.close();
            }
            allWriter = new BufferedWriter(
                    new FileWriter(
                            new File("bench/rt_latency_" + ServerConstants.NO_OF_STREAMING_CARS + "-" + System.currentTimeMillis() + ".csv")
                    )
            );
        } catch (IOException e) {
            LOG.error("Error in creating allWriter");
        }
    }

    static {
        init();
    }


    public synchronized static boolean addRecv(String uuid, JSONObject jsonObject) {
        if (!ServerConstants.CALCULATE_MQTT_LATENCY) {
            return true;
        }
        Long submittedTime = records.get(uuid);
        if (submittedTime != null) {
            long recvTime = System.currentTimeMillis();
            long latency = recvTime - submittedTime;
            if (latency > maxLatency) {
                maxLatency = latency;
                LOG.info("Max Latency : " + maxLatency + "ms");
            }

            if (latency < minLatency) {
                minLatency = latency;
            }

            synchronized (times) {
                times.add(latency);
            }

            totalRecords++;
            totalLatency = totalLatency.add(BigInteger.valueOf(latency));
            if (totalRecords % 10000 == 0) {
                LOG.info("Max Latency : " + maxLatency + "ms");
                LOG.info("Min Latency : " + minLatency + "ms");
                LOG.info("Average Latency : " + (totalLatency.divide(BigInteger.valueOf(totalRecords))) + "ms");
            }

            //collection to write to file
            synchronized (timeStamps) {
                try {
                    timeStamps.add(String.format("%s,%d,%d,%d,%d",
                            uuid, submittedTime,
                            jsonObject.getLong(PARAM_TIME_RECV),
                            jsonObject.getLong(PARAM_TIME_SEND),
                            recvTime)
                    );

                    if (timeStamps.size() > 10000) {
                        for (int i = 0; i < timeStamps.size(); i++) {
                            allWriter.write(timeStamps.get(i));
                            allWriter.newLine();
                        }
                        timeStamps.clear();
                        allWriter.flush();
                    }
                } catch (Exception ex) {
                    LOG.warn("Failed to record timestamps", ex);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public synchronized static void writeToFile() throws IOException {
        BufferedWriter br = new BufferedWriter(
                new FileWriter(
                        new File("bench/rt_latency_" + ServerConstants.NO_OF_STREAMING_CARS + "-" + System.currentTimeMillis() + ".csv")
                )
        );

        synchronized (times) {
            for (Long time : times) {
                br.write(time.toString());
                br.newLine();
            }
        }

        br.close();
    }


    public static void clear() {
        records.clear();
        times.clear();
        maxLatency = Long.MIN_VALUE;
        minLatency = Long.MAX_VALUE;
        totalRecords = 0;
        totalLatency = BigInteger.ZERO;
        init();
    }
}
