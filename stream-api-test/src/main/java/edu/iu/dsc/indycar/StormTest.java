package edu.iu.dsc.indycar;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.streams.Pair;
import org.apache.storm.streams.PairStream;
import org.apache.storm.streams.Stream;
import org.apache.storm.streams.StreamBuilder;
import org.apache.storm.streams.operations.Consumer;
import org.apache.storm.streams.operations.PairFunction;
import org.apache.storm.streams.operations.Predicate;
import org.apache.storm.streams.operations.ValueJoiner;
import org.apache.storm.streams.windowing.BaseWindow;
import org.apache.storm.streams.windowing.SlidingWindows;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.*;

public class StormTest {
    public static void main(String[] args) throws Exception {
        StreamBuilder streamBuilder = new StreamBuilder();

        // start with source
        Stream<Tuple> sourceStream = streamBuilder.newStream(new BaseRichSpout() {

            private SpoutOutputCollector collector;
            private Random random;

            public void open(Map<String, Object> map, TopologyContext topologyContext,
                             SpoutOutputCollector spoutOutputCollector) {
                this.collector = spoutOutputCollector;
                this.random = new Random(System.currentTimeMillis());
            }

            public void nextTuple() {
                Object[] tuple = new Object[]{
                        this.random.nextInt(33),
                        random.nextFloat(),
                        random.nextFloat(),
                        random.nextFloat()
                };
                this.collector.emit(Arrays.asList(tuple));
            }

            public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
                outputFieldsDeclarer.declare(new Fields("car", "speed", "rpm", "throttle"));
            }
        }, 1);

        // split for 33 cars
        Predicate<Tuple>[] branchingPredicates = new Predicate[33];
        for (int i = 0; i < 33; i++) {
            final int index = i;
            branchingPredicates[i] = tuple -> tuple.getInteger(0) == index;
        }
        Stream<Tuple>[] carBranches = sourceStream.branch(branchingPredicates);
        // done splitting for cars

        for (Stream<Tuple> carBranch : carBranches) {
            PairStream<Integer, List<Float>> carBranchPaired = carBranch.mapToPair((PairFunction<Tuple, Integer, List<Float>>) tuple -> {
                List<Float> values = new ArrayList<>();
                values.add(tuple.getFloat(1)); // speed
                values.add(tuple.getFloat(2)); // rpm
                values.add(tuple.getFloat(3)); // throttle
                return Pair.of(tuple.getInteger(0), values);
            });
            // car branch has all the raw data

            PairStream joinedStream = carBranchPaired;
            for (int i = 0; i < 3; i++) {
                int metricIndex = i;
                PairStream<Integer, Float> anomalyScoreStream = carBranch.mapToPair(new PairFunction<Tuple, Integer, Float>() {
                    // here we should initialize htm java and FIFO blocking mechanism should be created
                    private Random htm = new Random(System.currentTimeMillis());

                    @Override
                    public Pair<Integer, Float> apply(Tuple tuple) {
                        Float rawData = tuple.getFloat(metricIndex + 1); // + 1 because 0 is the card number
                        try {
                            // random sleep to simulate processing time
                            Thread.sleep(htm.nextInt(6));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return Pair.of(tuple.getInteger(0), rawData + 10000);
                    }
                });
                joinedStream = joinedStream.join(anomalyScoreStream, new ValueJoiner() {
                    @Override
                    public Object apply(Object o, Object o2) {
                        List<Float> combined = (List<Float>) o;
                        combined.add((Float) o2);
                        return combined;
                    }
                });
            }


            joinedStream.forEach(new Consumer() {
                @Override
                public void accept(Object o) {
                    // publish to websockets or MQTT
                    System.out.println(o);
                }
            });
        }

        try (LocalCluster cluster = new LocalCluster()) {
            cluster.submitTopology("indycar-stream", Collections.singletonMap(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 33), streamBuilder.build());
            Thread.sleep(10000000);
        }
    }
}
