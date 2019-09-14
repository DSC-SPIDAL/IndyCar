package iu.edu.indycar.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import iu.edu.indycar.ServerConstants;
import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.ws.ServerBoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.*;

public class RankPrediction {

  private final Logger LOG = LogManager.getLogger(RankPrediction.class);

  private final HashMap<String, LinkedList<CompleteLapRecord>> records = new HashMap<>();
  private final HashMap<String, Integer> lastLap = new HashMap<>();

  private final RankResult rankResult = new RankResult();

  private Client client = ClientBuilder.newClient();
  private ServerBoot serverBoot;

  public RankPrediction(ServerBoot serverBoot) {
    this.serverBoot = serverBoot;
    this.client.register(JacksonFeature.class);
  }

  public synchronized boolean predictRank(CompleteLapRecord completeLapRecord) {
    // process only if this is not a duplicate record
    if (completeLapRecord.getCarNumber().equals("") ||
            lastLap.getOrDefault(completeLapRecord.getCarNumber(), -1)
                    > completeLapRecord.getCompletedLaps()) {
      return false;
    }

    LinkedList<CompleteLapRecord> completeLapRecords = records.computeIfAbsent(
            completeLapRecord.getCarNumber(),
            carNumber -> new LinkedList<>());

    completeLapRecords.add(completeLapRecord);
    while (completeLapRecords.size() > ServerConstants.RANK_PRED_RECORDS_PER_REQ) {
      completeLapRecords.pop();
    }

    final List<LinkedList<CompleteLapRecord>> carsWithFivePlus = new ArrayList<>();
    final List<String> carsWithFivePlusRef = new ArrayList<>();

    records.forEach((k, v) -> {
      if (v.size() == ServerConstants.RANK_PRED_RECORDS_PER_REQ) {
        carsWithFivePlusRef.add(k);
        carsWithFivePlus.add(v);
      }
    });


    try {
      if (!carsWithFivePlus.isEmpty()) {
        RankPredictionRequest rankPredictionRequest = new RankPredictionRequest(carsWithFivePlus);
        RankPredictionResponse post = this.client.target(ServerConstants.RANK_PRED_REST)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(
                        rankPredictionRequest, MediaType.APPLICATION_JSON_TYPE),
                        RankPredictionResponse.class);

        for (int i = 0; i < carsWithFivePlusRef.size(); i++) {
          String carNumber = carsWithFivePlusRef.get(i);
          float predictedRank = post.getPredictions().get(i);
          this.rankResult.publishPrediction(carNumber, Math.round(predictedRank));
        }

//        final List<PredictionOfCar> toSort = new ArrayList<>();
//        for (int i = 0; i < carsWithFivePlusRef.size(); i++) {
//          String carNumber = carsWithFivePlusRef.get(i);
//          float predictedTime = post.getPredictions().get(i);
//          int completedLaps = this.lastLap.get(carNumber);
//          toSort.add(new PredictionOfCar(carNumber, completedLaps, predictedTime));
//        }
//        Collections.sort(toSort);
//        int rank = 1;
//        for (PredictionOfCar predictionOfCar : toSort) {
//          this.rankResult.publishPrediction(predictionOfCar.getCarNumber(), rank++);
//        }
      }
    } catch (Exception ex) {
      LOG.error("Error in rank prediction", ex);
    }

    this.rankResult.publishRank(completeLapRecord.getCarNumber(), completeLapRecord.getOverallRank());
    this.lastLap.put(completeLapRecord.getCarNumber(), completeLapRecord.getCompletedLaps());
    if (this.rankResult.isHasChanges()) {
      this.serverBoot.publishRankData(this.rankResult);
    }
    return true;
  }

  public void clear() {
    this.records.clear();
    this.lastLap.clear();
    this.rankResult.clear();
  }

  public static void main(String[] args) {
    File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

    RecordStreamer recordStreamer = new RecordStreamer(
            file, true, 10000000, s -> s.split("_")[2]);

    RankPrediction rp = new RankPrediction(null);

    recordStreamer.setCompleteLapRecordRecordListener(completeLapRecord -> {
      rp.predictRank(completeLapRecord);
    });

    recordStreamer.start();
  }
}
