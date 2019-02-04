package iu.edu.indycar.tmp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.HashMap;

public class LatencyCalculator {

    private final static Logger LOG = LogManager.getLogger(LatencyCalculator.class);

    private static HashMap<String, Long> records = new HashMap<>();

    private static long maxLatency = 0;
    private static long totalRecords = 0;
    private static BigInteger totalLatency = BigInteger.ZERO;

    public static void addSent(String uuid) {
        records.put(uuid, System.currentTimeMillis());
    }

    public static boolean addRecv(String uuid) {
        Long submittedTime = records.get(uuid);
        if (submittedTime != null) {
            long latency = System.currentTimeMillis() - submittedTime;
            if (latency > maxLatency) {
                maxLatency = latency;
                LOG.info("Max Latency : " + maxLatency + "ms");
            }
            totalRecords++;
            totalLatency = totalLatency.add(BigInteger.valueOf(latency));
            if (totalRecords % 10000 == 0) {
                LOG.info("Max Latency : " + maxLatency + "ms");
                LOG.info("Average Latency : " + (totalLatency.divide(BigInteger.valueOf(totalRecords))) + "ms");
            }
            return true;
        } else {
            return false;
        }
    }

    public static void clear() {
        records.clear();
        maxLatency = 0;
        totalRecords = 0;
        totalLatency = BigInteger.ZERO;
    }
}
