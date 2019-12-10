package iu.edu.indycar.prediction;

import iu.edu.indycar.ServerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NewPredictor {

    private static final Logger LOG = LogManager.getLogger(NewPredictor.class);

    private Client client = ClientBuilder.newClient();

    private HashMap<String, AtomicInteger> stageMap = new HashMap<>();

    public int predict(String carNumber) {
        int stage = stageMap.computeIfAbsent(carNumber, cn -> new AtomicInteger()).getAndIncrement();
        String prediction = this.client.target(ServerConstants.NEW_RANK_PRED_REST)
                .queryParam("car", carNumber)
                .queryParam("stage", stage)
                .request()
                .get(String.class);
        LOG.info("Predicting {} with stage {} -> {}", carNumber, stage, prediction);
        return Integer.valueOf(prediction);
    }

    public void clear() {
        this.stageMap.clear();
    }

    public static void main(String[] args) {
        final NewPredictor newPredictor = new NewPredictor();
        Arrays.stream(ServerConstants.CARS_BY_ORDER_OF_RECORDS).forEach(carNumber -> {
            for (int j = 0; j < 10; j++) {
                newPredictor.predict(carNumber);
            }
        });

    }

}
