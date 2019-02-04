package iu.edu.indycar.tmp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PingLatency {

    private final static Logger LOG = LogManager.getLogger(PingLatency.class);

    private String client;
    private long count;
    private long total;

    private long sentTime;

    public PingLatency(String client) {
        this.client = client;
    }

    public void pingSent() {
        this.sentTime = System.currentTimeMillis();
    }

    public void pongReceived() {
        this.count++;
        this.total += ((System.currentTimeMillis() - this.sentTime) / 2); //send - recv

        if (count % 100 == 0) {
            LOG.info("Average latency for client {} : {}ms", client, (total / count));
        }
    }
}
