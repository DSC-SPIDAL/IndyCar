package iu.edu.indycar.streamer.experiments;

import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.MatlabTheme;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class GapsOfRecords {

    private static final Logger LOG = LogManager.getLogger(GapsOfRecords.class);

    private static DecimalFormat df2 = new DecimalFormat(".##");
    private static DecimalFormat df4 = new DecimalFormat(".####");

    public static void main(String[] args) {
        File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log");

        RecordStreamer recordStreamer = new RecordStreamer(
                file, false, 100000000, s -> s.split("_")[2]);

        HashMap<String, AtomicLong> previousRecordsForCars = new HashMap<>();
        HashMap<String, HashMap<Long, AtomicInteger>> gapsForCars = new HashMap<>();

        recordStreamer.setTelemetryRecordListener(record -> {
            HashMap<Long, AtomicInteger> gaps = gapsForCars.computeIfAbsent(
                    record.getCarNumber(), c -> new HashMap<>());
            AtomicLong previous = previousRecordsForCars.computeIfAbsent(
                    record.getCarNumber(), c -> new AtomicLong(-1));
            if (previous.get() == -1) {
                previous.set(record.getTimeOfDayLong());
            } else {
                long previousTime = previous.get();
                long binnedTimeGap = ((record.getTimeOfDayLong() - previousTime) / 100) * 100;//100ms gaps

                gaps.computeIfAbsent(binnedTimeGap, s -> new AtomicInteger()).incrementAndGet();
                previous.set(record.getTimeOfDayLong());
            }
        });


        recordStreamer.setStreamEndListener(tag -> {
            LOG.info("End of stream");
            //all cars
            Map<Long, AtomicInteger> gaps = new ConcurrentHashMap<>();
            gapsForCars.keySet().parallelStream().forEach(carNumber -> {
                HashMap<Long, AtomicInteger> gapsForCar = gapsForCars.get(carNumber);

                gapsForCar.forEach((k, v) -> {
                    long key = Math.min(k, 1001);
                    if (gaps.containsKey(key)) {
                        gaps.get(key).set(gaps.get(key).get() + v.get());
                    } else {
                        gaps.put(key, new AtomicInteger(v.get()));
                    }
                });

                drawForCar(carNumber, gapsForCar, 2000);
            });

            drawForCar("ALL", gaps, 1000);
        });


    /*
      Adding a policy to skip all records until a non zero record is met for the first time
     */
        recordStreamer.addRecordAcceptPolicy(TelemetryRecord.class,
                new AbstractRecordAcceptPolicy<TelemetryRecord>() {

                    HashMap<String, Boolean> metFirstNonZero = new HashMap<>();

                    @Override
                    public boolean evaluate(TelemetryRecord record) {
                        if (metFirstNonZero.getOrDefault(record.getCarNumber(), false)) {
                            return true;
                        } else if (record.getLapDistance() > 5) {
                            metFirstNonZero.put(record.getCarNumber(), true);
                            return true;
                        }
                        return false;
                    }
                });

        recordStreamer.start();
    }

    private static void drawForCar(String carNumber, Map<Long, AtomicInteger> gaps, int width) {
        try {
            //writing file
            BufferedWriter br = new BufferedWriter(
                    new FileWriter(new File("gaps/car" + carNumber + "_gaps.csv")));
            gaps.keySet().stream().sorted().forEach(key -> {
                try {
                    br.write(key + "," + gaps.get(key).get());
                    br.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            br.close();


            //drawing chart
            CategoryChart histogram = new CategoryChartBuilder()
                    .width(width).height(600).title("Car " + carNumber + " Record Gaps")
                    .xAxisTitle("Gap(ms)").yAxisTitle("No. of record pairs").build();
            histogram.getStyler().setHasAnnotations(true);
            histogram.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            histogram.getStyler().setXAxisLabelRotation(45);

            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
            histogram.getStyler().setAxisTickLabelsFont(font);
            histogram.getStyler().setAxisTitleFont(font);
            histogram.getStyler().setLegendFont(font);
            histogram.getStyler().setPlotBackgroundColor(Color.WHITE);
            histogram.getStyler().setChartBackgroundColor(Color.WHITE);
            histogram.setTitle("");


            //long minGap = gaps.keySet().stream().min(Long::compareTo).get();
            //long maxGap = gaps.keySet().stream().max(Long::compareTo).get();

            List<Long> sortedGaps = new ArrayList<>(gaps.keySet());
            Collections.sort(sortedGaps);

            List<Integer> sortedFreq = sortedGaps.stream()
                    .map(g -> gaps.get(g).get()).collect(Collectors.toList());

            List<String> sortedRangeLabels = sortedGaps.stream().map(
                    g -> {
                        DecimalFormat df = df2;
                        double up = g + 99, low = g;
                        String unit = "";
                        if (g > 1000 * 60) {
                            unit = "min";
                            up /= 1000 * 60;
                            low /= 1000 * 60;
                            df = df4;
                        } else if (g > 1000) {
                            unit = "s";
                            up /= 1000;
                            low /= 1000;
                        }
                        return df.format(low) + unit + "-" + df.format(up) + unit;
                    }
            ).collect(Collectors.toList());

            if (carNumber.equals("ALL")) {
                sortedRangeLabels.set(sortedRangeLabels.size() - 1, "1001+");
            }

            histogram.addSeries("gaps",
                    sortedRangeLabels,
                    sortedFreq);
            BitmapEncoder.saveBitmap(histogram,
                    "gaps/car" + carNumber + "_gaps.png", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            System.out.println("Failed to draw for " + carNumber);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to draw for " + carNumber);
        }
    }
}
