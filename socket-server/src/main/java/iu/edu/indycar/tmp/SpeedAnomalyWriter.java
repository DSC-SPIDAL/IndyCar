package iu.edu.indycar.tmp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SpeedAnomalyWriter {

    private final Logger LOG = LogManager.getLogger(SpeedAnomalyWriter.class);

    private BufferedWriter bufferedWriter;

    public SpeedAnomalyWriter(String file) throws IOException {
        File fileObj = new File(file);
        fileObj.getParentFile().mkdirs();
        if (!fileObj.exists()) {
            fileObj.createNewFile();
        }
        this.bufferedWriter = new BufferedWriter(new FileWriter(new File(file)));
    }

    public synchronized void write(String timeOfDay,
                                   long timeOfDayLong,
                                   double speed, double speedAnomaly,
                                   double rpm, double rpmAnomaly,
                                   double throttle, double throttleAnomaly) throws IOException {
        this.bufferedWriter.write(String.format(
                "%s,%d,%f,%f,%f,%f,%f,%f", timeOfDay, timeOfDayLong,
                speed, speedAnomaly, rpm, rpmAnomaly, throttle, throttleAnomaly
        ));
        this.bufferedWriter.newLine();
    }

    public void close() {
        try {
            this.bufferedWriter.close();
        } catch (IOException e) {
            LOG.error("Failed to close the log writer", e);
        }
    }
}
