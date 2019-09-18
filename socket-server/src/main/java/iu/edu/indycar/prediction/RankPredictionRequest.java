package iu.edu.indycar.prediction;

import iu.edu.indycar.streamer.records.CompleteLapRecord;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RankPredictionRequest {
  public List<List<List<Number>>> instances;

  public RankPredictionRequest() {

  }

  public RankPredictionRequest(List<LinkedList<CompleteLapRecord>> data) {
    this.instances = data.stream().map(v -> {
      return v.stream().map(completeLapRecord -> {
        List<Number> list = new ArrayList<>();

        list.add(completeLapRecord.getRank());
        list.add(completeLapRecord.getCompletedLaps());
        list.add((float) completeLapRecord.getElapsedTime() / 60000.0f);
        list.add((float) completeLapRecord.getTime() / 60.0f);
        list.add((float) completeLapRecord.getFastestLapTime() / 60000.0f);
        list.add(completeLapRecord.getFastestLap());
        list.add((float) completeLapRecord.getTimeBehindLeader() / 60000.0f);
        list.add(completeLapRecord.getLapsBehindLeader());
        list.add((float) completeLapRecord.getTimeBehindPrec() / 60000.0f);
        list.add(completeLapRecord.getLapsBehindPrec());
        list.add(completeLapRecord.getOverallRank());
        list.add((float) completeLapRecord.getOverallBestLapTime() / 60000.0f);
        list.add(completeLapRecord.getPitStopsCount());
        list.add(completeLapRecord.getLastPittedLap());
        list.add(completeLapRecord.getStartPosition());
        list.add(completeLapRecord.getLapsLed());

        return list;
      }).collect(Collectors.toList());
    }).collect(Collectors.toList());
  }

  public List<List<List<Number>>> getInstances() {
    return instances;
  }
}
