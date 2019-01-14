package iu.edu.indycar.tmp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RecordWriter {

    private BufferedWriter bufferedWriter;

    public RecordWriter(String file) throws IOException {
        File fileObj = new File(file);
        if (!fileObj.exists()) {
            fileObj.createNewFile();
        }
        this.bufferedWriter = new BufferedWriter(new FileWriter(new File(file)));
    }

    public synchronized void write(String record) throws IOException {
        this.bufferedWriter.write(String.format("%s,%d", record, System.currentTimeMillis()));
        this.bufferedWriter.newLine();
        this.bufferedWriter.flush();
    }
}
