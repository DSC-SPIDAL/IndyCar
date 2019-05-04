package iu.edu.indycar.streamer.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class RTLatencyAll {

    public static void main(String[] args) throws IOException {
        String basePath = "/home/chathura/Code/IndyCar/benchmark/corrected_2_nodes_OPT/rt_latency_33-1556841485376.csv";

        BufferedReader br = new BufferedReader(new FileReader(new File(basePath)));

        String line;
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        BigDecimal sum = BigDecimal.ZERO;
        long count = 0;

        Map<Long, AtomicLong> counts = new HashMap<>();

        for (long i = 0; i <= 405; i += 5) {
            counts.put(i, new AtomicLong());
        }

        while ((line = br.readLine()) != null) {
            String splits[] = line.split(",");
            long latency = Long.valueOf(splits[4]) - Long.valueOf(splits[1]);
            if (latency < 0) {
                System.out.println("LESS - Problem!");
            }
            sum = sum.add(BigDecimal.valueOf(latency));
            if (min > latency) {
                min = latency;
            }

            if (max < latency) {
                max = latency;
            }
            count++;
            long rounded = (latency / 5) * 5;
            if (rounded < 400) {
                counts.get(rounded).incrementAndGet();
            } else {
                counts.get(405L).incrementAndGet();
            }
        }

        System.out.println("Max : " + max);
        System.out.println("Min : " + min);
        System.out.println("Avg : " + sum.divide(BigDecimal.valueOf(count), BigDecimal.ROUND_DOWN));


        ArrayList<Long> sorted = new ArrayList<>(counts.keySet());
        Collections.sort(sorted);

        sorted.forEach(k -> {
            System.out.println(k + "," + counts.get(k).get());
        });
    }
}
