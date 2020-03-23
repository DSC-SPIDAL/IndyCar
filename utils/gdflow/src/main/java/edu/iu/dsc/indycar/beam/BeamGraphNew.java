package edu.iu.dsc.indycar.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Partition;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.joda.time.Duration;

import java.io.IOException;

public class BeamGraphNew {

  public static void main(String[] args) throws IOException {
    Options pipelineOptions = PipelineOptionsFactory.fromArgs(args)
        .as(Options.class);

    pipelineOptions.setStreaming(true);

    //PubsubOptions pubsubOptions = pipelineOptions.as(PubsubOptions.class);
    //pubsubOptions.setPubsubRootUrl("http://localhost:8085");


//    GoogleCredentials credential = GoogleCredentials
//        .fromStream(new FileInputStream("/home/chathura/gcp/Test-708a62221b22.json")).createScoped();
//
//    pubsubOptions.setGcpCredential(credential);

    Pipeline pipeline = Pipeline.create(pipelineOptions);

//    final Map<String, TupleTag<List<Float>>> tags = new HashMap<>();
//    List<TupleTag<?>> tupleTags = new ArrayList<>();
//    for (int i = 0; i < 33; i++) {
//      TupleTag<List<Float>> carTag = new TupleTag<List<Float>>() {
//      };
//      tags.put(Integer.toString(i), carTag);
//      if (i != 0) {
//        tupleTags.add(carTag);
//      }
//    }


    pipeline.apply(PubsubIO.readStrings().fromSubscription("projects/dataflow-test-270217/subscriptions/sub"))
        //.apply("write", PubsubIO.writeStrings().to("projects/dataflow-test-270217/topics/out"));
        .apply(Partition.of(33,
            new Partition.PartitionFn<String>() {
              @Override
              public int partitionFor(String elem, int numPartitions) {
                return Integer.parseInt(elem.split(",")[0]);
              }
            }));


//    pipeline.apply(TextIO.read().from("/tmp/input.csv"))
//        .apply("write", TextIO.write().to("/tmp/final.csv"));
//        .apply("partition-by-car", ParDo.of(new DoFn<String, KV<String, String>>() {
//          @ProcessElement
//          public void process(ProcessContext c) {
//            String[] splits = c.element().split(",");
//            c.outputWithTimestamp(KV.of(splits[1], c.element()),
//                new DateTime(Long.parseLong(splits[0])).toInstant());
//          }
//        })).setCoder(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()))
//        .apply("split-by-metric", ParDo.of(new DoFn<KV<String, String>, KV<String, Float>>() {
//          @ProcessElement
//          public void process(ProcessContext c) {
//            KV<String, String> tuple = c.element();
//            System.out.println(tuple + ", " + this.hashCode() + " : " + Thread.currentThread().getName());
//            String[] metrics = tuple.getValue().split(",");
//            for (int i = 2; i < metrics.length; i++) {
//              c.outputWithTimestamp(KV.of(tuple.getKey() + "_" + i,
//                  Float.parseFloat(metrics[i])), new DateTime(Long.parseLong(metrics[0])).toInstant());
//            }
//          }
//        }));


//    PCollectionTuple mixedCollection = pipeline.apply(TextIO.read().from("/tmp/input.csv"))
//        .apply("split-by-car", ParDo.of(new DoFn<String, List<Float>>() {
//              @ProcessElement
//              public void splitByCar(ProcessContext c) {
//                String tuple = c.element();
//                System.out.println(tuple);
//                String[] elements = tuple.split(",");
//                String carNumber = elements[0];
//                List<Float> metrics = new ArrayList<>();
//                for (int i = 1; i < elements.length; i++) {
//                  metrics.add(Float.parseFloat(elements[i]));
//                }
//                c.output(tags.get(carNumber), metrics);
//              }
//            }).withOutputTags(tags.get("0"), TupleTagList.of(tupleTags))
//        );
//
//    for (int i = 0; i < 33; i++) {
//      final String carNumber = Integer.toString(i);
//      final List<TupleTag<Float>> metricTags = new ArrayList<>();
//      final List<TupleTag<?>> tupleTagList = new ArrayList<>();
//      for (int j = 0; j < 3; j++) {
//        TupleTag<Float> tupleTag = new TupleTag<Float>() {
//        };
//        metricTags.add(tupleTag);
//        if (j != 0) {
//          tupleTagList.add(tupleTag);
//        }
//      }
//      PCollectionTuple metricsCollection = mixedCollection.get(tags.get(carNumber)).apply("car-" + i,
//          ParDo.of(new DoFn<List<Float>, Float>() {
//
//            @ProcessElement
//            public void splitMatrices(ProcessContext c) {
//              List<Float> metrics = c.element();
//              System.out.println(carNumber + ":" + metrics);
//              for (int j = 0; j < 3; j++) {
//                c.output(metricTags.get(j), metrics.get(j));
//              }
//            }
//          }).withOutputTags(metricTags.get(0), TupleTagList.of(tupleTagList))
//      );
//
//      // doing anomaly detection
//      List<PCollection<List<Float>>> anomalyScoresList = new ArrayList<>();
//      for (int j = 0; j < 3; j++) {
//        final int metricIndex = j;
//        PCollection<List<Float>> anomalyScore = metricsCollection.get(metricTags.get(j))
//            .apply("anomaly-detect-" + carNumber + "-" + j,
//                ParDo.of(new DoFn<Float, List<Float>>() {
//                  @ProcessElement
//                  public void detectAnomaly(ProcessContext c) {
//                    System.out.println("detecting for : " + carNumber + " : " + metricIndex + " : " + c.element());
//                    List<Float> rawAndScore = new ArrayList<>();
//                    rawAndScore.add((float) metricIndex);
//                    rawAndScore.add(c.element());
//                    rawAndScore.add(c.element() + 0.001f);
//                    c.output(rawAndScore);
//                  }
//                })
//            );
//        anomalyScoresList.add(anomalyScore);
//      }
//
//      PCollection<List<Float>> finalResults = PCollectionList.of(anomalyScoresList)
//          .apply(Flatten.pCollections());
//
//      finalResults.apply("publish-results", ParDo.of(new DoFn<Object, Object>() {
//
//        private Map<Float, Queue<List<Float>>> metricQ = new HashMap<>();
//
//
//        private List<Float> check() {
//          boolean notEmpty = true;
//          for (Float aFloat : metricQ.keySet()) {
//            notEmpty = notEmpty && !metricQ.get(aFloat).isEmpty();
//          }
//
//          if (notEmpty) {
//            List<Float> finalMetric = new ArrayList<>();
//            for (Float aFloat : metricQ.keySet()) {
//              List<Float> poll = metricQ.get(aFloat).poll();
//              for (int j = 1; j < poll.size(); j++) {
//                finalMetric.add(poll.get(j));
//              }
//            }
//            return finalMetric;
//          }
//          return null;
//        }
//
//        @Setup
//        public void setup() {
//          for (int j = 0; j < 3; j++) {
//            metricQ.put((float) j, new LinkedList<>());
//          }
//        }
//
//        @ProcessElement
//        public void publish(ProcessContext c) {
//          System.out.println(c.element());
//          List<Float> element = (List<Float>) c.element();
//          metricQ.get(element.get(0)).add(element);
//          if (carNumber.equals("1")) {
//            System.out.println(this.hashCode() + " Merging : " + carNumber + " : " + c.element());
//          }
//          List<Float> check = check();
//          if (check != null) {
//            System.out.println("publishing : " + carNumber + " : " + check);
//          }
//        }
//      }));
//    }
    pipeline.run();
  }
}
