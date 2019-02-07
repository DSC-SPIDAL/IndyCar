package iu.edu.indycar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FluxGenerator {

    private static final String TEMPLATE_SPOUT = "  - id: \"telemetryspout-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.IndycarSpout\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
            "      - \"#NO\"\n\n";

    private static final String TEMPLATE_BOLTS = "  - id: \"RPMBolt-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.ScalarMetricBolt\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
            "      - \"#NO\"\n" +
            "      - \"RPM\"\n" +
            "      - \"0\"\n" +
            "      - \"12500\"\n" +
            "      \n" +
            "  - id: \"SpeedBolt-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.ScalarMetricBolt\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
            "      - \"#NO\"\n" +
            "      - \"speed\"\n" +
            "      - \"0\"\n" +
            "      - \"250\"\n" +
            "      \n" +
            "  - id: \"ThrottleBolt-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.ScalarMetricBolt\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
            "      - \"#NO\"\n" +
            "      - \"throttle\"\n" +
            "      - \"0\"\n" +
            "      - \"30\"\n\n";

    private static final String TEMPLATE_STREAMS = "  - name: \"telemetryspout-#NO --> RPMBolt-#NO\"\n" +
            "    from: \"telemetryspout-#NO\"\n" +
            "    to: \"RPMBolt-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"telemetryspout-#NO --> SpeedBolt-#NO\"\n" +
            "    from: \"telemetryspout-#NO\"\n" +
            "    to: \"SpeedBolt-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"telemetryspout-#NO --> ThrottleBolt-#NO\"\n" +
            "    from: \"telemetryspout-#NO\"\n" +
            "    to: \"ThrottleBolt-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "     \n" +
            "  - name: \"RPMBolt-#NO --> sink\"\n" +
            "    from: \"RPMBolt-#NO\"\n" +
            "    to: \"sink-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"SpeedBolt-#NO --> sink\"\n" +
            "    from: \"SpeedBolt-#NO\"\n" +
            "    to: \"sink-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"ThrottleBolt-#NO --> sink\"\n" +
            "    from: \"ThrottleBolt-#NO\"\n" +
            "    to: \"sink-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n\n";

    private static final int NO_OF_CARS = 33;

    public static void main(String[] args) throws IOException {
        StringBuilder fluxTemplate = new StringBuilder(
                "name: \"indy500-v1\"\n" +
                        "config:\n" +
                        "  topology.workers: 1\n\n"
        );

        StringBuilder spouts = new StringBuilder("spouts:\n");
        StringBuilder bolts = new StringBuilder("bolts:\n");

        StringBuilder streams = new StringBuilder("streams:\n");

        for (int i = 0; i < NO_OF_CARS; i++) {
            bolts.append(("  - id: \"sink-#NO\"\n" +
                    "    className: \"com.dsc.iu.stream.app.Sink\"\n" +
                    "    parallelism: 1\n"+ "    constructorArgs:\n" +
            "      - \"#NO\"\n\n").replace("#NO", Integer.toString(i + 1)));

            spouts.append(
                    TEMPLATE_SPOUT.replaceAll("#NO", Integer.toString(i + 1))
            );

            bolts.append(
                    TEMPLATE_BOLTS.replaceAll("#NO", Integer.toString(i + 1))
            );

            streams.append(
                    TEMPLATE_STREAMS.replaceAll("#NO", Integer.toString(i + 1))
            );
        }

        fluxTemplate.append(spouts);
        fluxTemplate.append("\n");
        fluxTemplate.append(bolts);
        fluxTemplate.append("\n");
        fluxTemplate.append(streams);

        FileWriter fileWriter = new FileWriter(new File("indycar.yml"));
        fileWriter.write(fluxTemplate.toString());
        fileWriter.flush();
        fileWriter.close();
        System.out.println(fluxTemplate);
    }
}
