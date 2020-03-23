package edu.iu.dsc.indycar.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.state.ValueState;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.*;

import java.util.*;

public class BeamGraph {

  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create(PipelineOptionsFactory.fromArgs(args).create());

    final Map<String, TupleTag<List<Float>>> tags = new HashMap<>();
    List<TupleTag<?>> tupleTags = new ArrayList<>();
    for (int i = 0; i < 33; i++) {
      TupleTag<List<Float>> carTag = new TupleTag<List<Float>>() {
      };
      tags.put(Integer.toString(i), carTag);
      if (i != 0) {
        tupleTags.add(carTag);
      }
    }

    PCollectionTuple mixedCollection = pipeline.apply(TextIO.read().from("/tmp/input.csv"))
        .apply("split-by-car", ParDo.of(new DoFn<String, List<Float>>() {
              @ProcessElement
              public void splitByCar(ProcessContext c) {
                String tuple = c.element();
                System.out.println(tuple);
                String[] elements = tuple.split(",");
                String carNumber = elements[0];
                List<Float> metrics = new ArrayList<>();
                for (int i = 1; i < elements.length; i++) {
                  metrics.add(Float.parseFloat(elements[i]));
                }
                c.output(tags.get(carNumber), metrics);
              }
            }).withOutputTags(tags.get("0"), TupleTagList.of(tupleTags))
        );

    for (int i = 0; i < 33; i++) {
      final String carNumber = Integer.toString(i);
      final List<TupleTag<Float>> metricTags = new ArrayList<>();
      final List<TupleTag<?>> tupleTagList = new ArrayList<>();
      for (int j = 0; j < 3; j++) {
        TupleTag<Float> tupleTag = new TupleTag<Float>() {
        };
        metricTags.add(tupleTag);
        if (j != 0) {
          tupleTagList.add(tupleTag);
        }
      }
      PCollectionTuple metricsCollection = mixedCollection.get(tags.get(carNumber)).apply("car-" + i,
          ParDo.of(new DoFn<List<Float>, Float>() {

            @ProcessElement
            public void splitMatrices(ProcessContext c) {
              List<Float> metrics = c.element();
              System.out.println(carNumber + ":" + metrics);
              for (int j = 0; j < 3; j++) {
                c.output(metricTags.get(j), metrics.get(j));
              }
            }
          }).withOutputTags(metricTags.get(0), TupleTagList.of(tupleTagList))
      );

      // doing anomaly detection
      List<PCollection<List<Float>>> anomalyScoresList = new ArrayList<>();
      for (int j = 0; j < 3; j++) {
        final int metricIndex = j;
        PCollection<List<Float>> anomalyScore = metricsCollection.get(metricTags.get(j))
            .apply("anomaly-detect-" + carNumber + "-" + j,
                ParDo.of(new DoFn<Float, List<Float>>() {
                  @ProcessElement
                  public void detectAnomaly(ProcessContext c) {
                    System.out.println("detecting for : " + carNumber + " : " + metricIndex + " : " + c.element());
                    List<Float> rawAndScore = new ArrayList<>();
                    rawAndScore.add((float) metricIndex);
                    rawAndScore.add(c.element());
                    rawAndScore.add(c.element() + 0.001f);
                    c.output(rawAndScore);
                  }
                })
            );
        anomalyScoresList.add(anomalyScore);
      }

      PCollection<List<Float>> finalResults = PCollectionList.of(anomalyScoresList)
          .apply(Flatten.pCollections());

      finalResults.apply("publish-results", ParDo.of(new DoFn<Object, Object>() {

        private Map<Float, Queue<List<Float>>> metricQ = new HashMap<>();


        private List<Float> check() {
          boolean notEmpty = true;
          for (Float aFloat : metricQ.keySet()) {
            notEmpty = notEmpty && !metricQ.get(aFloat).isEmpty();
          }

          if (notEmpty) {
            List<Float> finalMetric = new ArrayList<>();
            for (Float aFloat : metricQ.keySet()) {
              List<Float> poll = metricQ.get(aFloat).poll();
              for (int j = 1; j < poll.size(); j++) {
                finalMetric.add(poll.get(j));
              }
            }
            return finalMetric;
          }
          return null;
        }

        @Setup
        public void setup() {
          for (int j = 0; j < 3; j++) {
            metricQ.put((float) j, new LinkedList<>());
          }
        }

        @ProcessElement
        public void publish(ProcessContext c) {
          System.out.println(c.element());
          List<Float> element = (List<Float>) c.element();
          metricQ.get(element.get(0)).add(element);
          if (carNumber.equals("1")) {
            System.out.println(this.hashCode() + " Merging : " + carNumber + " : " + c.element());
          }
          List<Float> check = check();
          if (check != null) {
            System.out.println("publishing : " + carNumber + " : " + check);
          }
        }
      }));
    }
    pipeline.run();
  }
}
