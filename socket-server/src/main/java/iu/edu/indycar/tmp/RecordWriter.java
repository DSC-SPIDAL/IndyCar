package iu.edu.indycar.tmp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RecordWriter {

    private final Logger LOG = LogManager.getLogger(RecordWriter.class);

    private BufferedWriter bufferedWriter;

    public RecordWriter(String file) throws IOException {
        File fileObj = new File(file);
        if (!fileObj.exists()) {
            fileObj.createNewFile();
        }
        this.bufferedWriter = new BufferedWriter(new FileWriter(new File(file)));
    }

    public synchronized void write(String carNumber, String counter, double lapDistance, String timeOfDay, double speed, double rpm, double throttle) throws IOException {
        this.bufferedWriter.write(String.format("%s,%s,%f,%s,%f,%f,%f,%d", carNumber, counter, lapDistance, timeOfDay, speed, rpm, throttle, System.currentTimeMillis()));
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
