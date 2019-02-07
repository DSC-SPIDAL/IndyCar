package iu.edu.indycar.tmp;

import iu.edu.indycar.ServerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PingLatency {

    private final static Logger LOG = LogManager.getLogger(PingLatency.class);

    private String client;
    private long count;
    private long total;

    private long sentTime;

    private long highestLatency = Long.MIN_VALUE;
    private long lowestLatency = Long.MAX_VALUE;

    private static List<Long> times = new ArrayList<>();


    public PingLatency(String client) {
        this.client = client;
    }

    public void pingSent() {
        this.sentTime = System.currentTimeMillis();
    }

    public synchronized void pongReceived() {
        this.count++;
        long latency = ((System.currentTimeMillis() - this.sentTime) / 2);

        times.add(latency);

        this.total += latency; //send - recv

        if (highestLatency < latency) {
            this.highestLatency = latency;
        }

        if (lowestLatency > latency) {
            this.lowestLatency = latency;
        }

        if (count % 100 == 0) {
            LOG.info("Average latency for client {} : {}ms", client, (total / count));
            LOG.info("Lowest latency for client {} : {}ms", client, lowestLatency);
            LOG.info("Highest latency for client {} : {}ms", client, highestLatency);
        }
    }

    public synchronized static void writeToFile() throws IOException {
        BufferedWriter br = new BufferedWriter(
                new FileWriter(
                        new File("bench/ws_browser_latency_" + ServerConstants.DEBUG_CARS + ".csv")
                )
        );

        for (Long time : times) {
            br.write(time.toString());
            br.newLine();
        }

        br.close();
    }
}
