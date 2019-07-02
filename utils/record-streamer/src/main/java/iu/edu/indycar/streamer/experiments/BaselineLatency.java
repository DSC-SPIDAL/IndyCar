package iu.edu.indycar.streamer.experiments;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class BaselineLatency {

    public static void main(String[] args) throws IOException {
        String basePath = "/home/chathura/Code/IndyCar/benchmark/baseline";

        //String[] csvs = new File(basePath).list();
        String[] csvs = {"latency_throttle.csv"};

        final AtomicLong max = new AtomicLong(Long.MIN_VALUE);
        final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
        final BigInteger[] sum = {BigInteger.ZERO, BigInteger.ZERO};

        final Map<Long, Long> counts = new HashMap<>();

        for (String csv : csvs) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(basePath, csv)));
            bufferedReader.lines().parallel().map(Long::valueOf).forEach(latency -> {
                if (max.get() < latency) {
                    max.set(latency);
                }

                if (min.get() > latency) {
                    min.set(latency);
                }
                synchronized (sum) {
                    sum[0] = sum[0].add(BigInteger.valueOf(latency));
                    sum[1] = sum[1].add(BigInteger.ONE);

                    counts.put(latency, counts.getOrDefault(latency, 0L) + 1);
                }
            });
        }

        System.out.println("Max : " + max);
        System.out.println("Min : " + min);
        System.out.println("Sum : " + sum[0]);
        System.out.println("Average : " + sum[0].divide(sum[1]));

        long minMs = Math.round(min.get() / 10.0) * 10;
        long maxMs = Math.round(max.get() / 10.0) * 10;

        CategoryChart histogram = new CategoryChartBuilder()
                .width(800).height(600).title("Round Trip Latency")
                .xAxisTitle("Latency Bin(ms)").yAxisTitle("No. of record").build();
        histogram.getStyler().setHasAnnotations(false);
        histogram.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        histogram.getStyler().setXAxisLabelRotation(45);

        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
        histogram.getStyler().setAxisTickLabelsFont(font);
        histogram.getStyler().setAxisTitleFont(font);
        histogram.getStyler().setLegendFont(font);
        histogram.getStyler().setPlotBackgroundColor(Color.WHITE);
        histogram.getStyler().setChartBackgroundColor(Color.WHITE);

//        histogram.getStyler().setXAxisTickMarkSpacingHint(10000);

        List<Long> labels = new ArrayList<>();
        List<Long> frequency = new ArrayList<>();


        long totalIts = (maxMs - minMs) / 10;
        System.out.println("iterations : " + totalIts);
        long completed = 0;

//        for (long i = minMs; i <= maxMs; i += 10) {
//            labels.add(i);
//            Long count = counts.remove(i);
//            if (count != null) {
//                frequency.add(count);
//            } else {
//                frequency.add(0L);
//            }
//        }

        AtomicLong mode = new AtomicLong(0);
        AtomicLong maxCount = new AtomicLong();

        counts.keySet().stream().sorted().forEach(key -> {
            System.out.println(key + "," + counts.get(key));
            labels.add(key);
            frequency.add(counts.get(key));

            if (maxCount.get() < counts.get(key)) {
                maxCount.set(counts.get(key));
                mode.set(key);
            }
        });

        System.out.println("In Hash Map : " + counts.size());
        System.out.println("Mode : " + mode.get());


        histogram.addSeries("Latency", labels, frequency);

        BitmapEncoder.saveBitmap(histogram,
                "storm_latency_33.png", BitmapEncoder.BitmapFormat.PNG);


    }
}
