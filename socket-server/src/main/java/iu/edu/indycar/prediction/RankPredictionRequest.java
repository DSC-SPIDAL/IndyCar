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
        list.add(completeLapRecord.getElapsedTime());
        list.add(completeLapRecord.getTime());
        list.add(completeLapRecord.getFastestLapTime());
        list.add(completeLapRecord.getFastestLap());
        list.add(completeLapRecord.getTimeBehindLeader());
        list.add(completeLapRecord.getLapsBehindLeader());
        list.add(completeLapRecord.getTimeBehindPrec());
        list.add(completeLapRecord.getLapsBehindPrec());
        list.add(completeLapRecord.getOverallRank());
        list.add(completeLapRecord.getOverallBestLapTime());
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
