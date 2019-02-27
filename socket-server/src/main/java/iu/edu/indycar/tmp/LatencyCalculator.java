package iu.edu.indycar.tmp;

import iu.edu.indycar.ServerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final List<Long> times = new ArrayList<>();

    public synchronized static void addSent(String uuid) {
        if (!ServerConstants.CALCULATE_MQTT_LATENCY) {
            return;
        }
        records.put(uuid, System.nanoTime());
    }

    public synchronized static boolean addRecv(String uuid) {
        if (!ServerConstants.CALCULATE_MQTT_LATENCY) {
            return true;
        }
        Long submittedTime = records.get(uuid);
        if (submittedTime != null) {
            long latency = System.nanoTime() - submittedTime;
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
            return true;
        } else {
            return false;
        }
    }

    public synchronized static void writeToFile() throws IOException {
        BufferedWriter br = new BufferedWriter(
                new FileWriter(
                        new File("bench/rt_latency_" + ServerConstants.DEBUG_CARS + ".csv")
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
    }
}
