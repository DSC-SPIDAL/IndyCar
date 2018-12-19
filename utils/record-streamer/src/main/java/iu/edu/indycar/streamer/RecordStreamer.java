package iu.edu.indycar.streamer;

import iu.edu.indycar.streamer.exceptions.NotParseableException;
import iu.edu.indycar.streamer.records.IndycarRecord;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.WeatherRecord;
import iu.edu.indycar.streamer.records.parsers.TelemetryRecordParser;
import iu.edu.indycar.streamer.records.parsers.WeatherRecordParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordStreamer implements Runnable {

  //Listeners
  private RecordListener<IndycarRecord> recordListener;
  private RecordListener<WeatherRecord> weatherRecordListener;
  private RecordListener<TelemetryRecord> telemetryRecordListener;

  private File file;
  private boolean realTiming;
  private int speed = 1;

  private boolean fileEnded;

  private FileNameDateExtractor dateExtractor;

  private HashMap<String, RecordTiming> lastRecordTime = new HashMap<>();
  private ConcurrentHashMap<String, ConcurrentLinkedQueue<IndycarRecord>> records = new ConcurrentHashMap<>();

  public RecordStreamer(File file, boolean realTiming, FileNameDateExtractor dateExtractor) {
    this.file = file;
    this.realTiming = realTiming;
    this.dateExtractor = dateExtractor;
  }

  public RecordStreamer(File file, boolean realTiming, int speed, FileNameDateExtractor dateExtractor) {
    this(file, realTiming, dateExtractor);
    this.speed = speed;
  }

  public void setRecordListener(RecordListener<IndycarRecord> recordListener) {
    this.recordListener = recordListener;
  }

  public void setTelemetryRecordListener(
          RecordListener<TelemetryRecord> telemetryRecordRecordListener) {
    this.telemetryRecordListener = telemetryRecordRecordListener;
  }

  public void setWeatherRecordListener(
          RecordListener<WeatherRecord> weatherRecordRecordListener) {
    this.weatherRecordListener = weatherRecordRecordListener;
  }

  public void start() throws IOException {
    if (this.realTiming) {
      new Thread(this).start();
    }
    this.readFile();
  }

  private void queueEvent(IndycarRecord indycarRecord) {
    if (!realTiming) {
      this.publishEvent(indycarRecord);
    } else {
      records.computeIfAbsent(indycarRecord.getGroupTag(),
              (s) -> new ConcurrentLinkedQueue<>()).add(indycarRecord);
    }
  }

  private void publishEvent(IndycarRecord indycarRecord) {
    if (this.recordListener != null) {
      this.recordListener.onRecord(indycarRecord);
    }

    if (this.telemetryRecordListener != null
            && indycarRecord instanceof TelemetryRecord) {
      this.telemetryRecordListener.onRecord((TelemetryRecord) indycarRecord);
    } else if (this.weatherRecordListener != null
            && indycarRecord instanceof WeatherRecord) {
      this.weatherRecordListener.onRecord((WeatherRecord) indycarRecord);
    }
  }

  private void readFile() throws IOException {
    FileReader fis = new FileReader(file);

    String date = this.dateExtractor.extractDate(file.getName());

    BufferedReader br = new BufferedReader(fis);
    String line = br.readLine();

    TelemetryRecordParser telemetryRecordParser = new TelemetryRecordParser("�");
    WeatherRecordParser weatherRecordParser = new WeatherRecordParser("�");

    while (line != null) {
      try {
        if (line.startsWith("$P")) {
          TelemetryRecord tr = telemetryRecordParser.parse(line);
          tr.setDate(date);
          this.queueEvent(tr);
        } else if (line.startsWith("$W")) {
          this.queueEvent(weatherRecordParser.parse(line));
        }
      } catch (NotParseableException e) {
        //couldn't parse
      } finally {
        line = br.readLine();
      }
    }
    br.close();
    this.fileEnded = true;
    System.out.println("End of File : " + file.getName());
  }


  @Override
  public void run() {
    while (true) {
      final AtomicBoolean foundSomething = new AtomicBoolean(false);
      this.records.forEach((carNumber, recordsQueue) -> {
        IndycarRecord next = recordsQueue.peek();
        if (next != null) {
          foundSomething.set(true);

          RecordTiming recordTiming = lastRecordTime.computeIfAbsent(
                  next.getGroupTag(), s -> new RecordTiming()
          );

          long now = System.currentTimeMillis();

          if (recordTiming.isFirstRecord() || now - recordTiming.getLastRecordSubmittedTime() >=
                  (next.getTimeField() - recordTiming.getLastRecordTime() / this.speed)) {

            this.publishEvent(recordsQueue.poll());

            recordTiming.setLastRecordTime(next.getTimeField());
            recordTiming.setLastRecordSubmittedTime(now);
          }
        }
      });

      if (!foundSomething.get() && this.fileEnded) {
        break;
      }
    }
  }
}
