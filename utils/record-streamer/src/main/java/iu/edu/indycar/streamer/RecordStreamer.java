package iu.edu.indycar.streamer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordStreamer implements Runnable {

  private RecordListener recordListener;
  private File file;
  private boolean realTiming;
  private int speed = 1;

  private boolean fileEnded;

  private FileNameDateExtractor dateExtractor;

  private AtomicInteger count = new AtomicInteger(0);

  private HashMap<String, RecordTiming> lastRecordTime = new HashMap<>();
  private ConcurrentHashMap<String, ConcurrentLinkedQueue<TelemetryRecord>> records = new ConcurrentHashMap<>();

  public RecordStreamer(File file, boolean realTiming,
                        RecordListener listener, FileNameDateExtractor dateExtractor) {
    this.recordListener = listener;
    this.file = file;
    this.realTiming = realTiming;
    this.dateExtractor = dateExtractor;
  }

  public RecordStreamer(File file, boolean realTiming, int speed,
                        RecordListener listener, FileNameDateExtractor dateExtractor) {
    this(file, realTiming, listener, dateExtractor);
    this.speed = speed;
  }

  public void start() throws IOException {
    if (this.realTiming) {
      new Thread(this).start();
    }
    this.readFile();
  }

  private void readFile() throws IOException {
    FileReader fis = new FileReader(file);

    String date = this.dateExtractor.extractDate(file.getName());

    BufferedReader br = new BufferedReader(fis);
    String line = br.readLine();

    while (line != null) {
      if (line.startsWith("$P")) {
        String[] splits = line.split("�");
        String carNumber = splits[1];
        String timeOfDay = splits[2];
        String lapDistance = splits[3];
        String vehicleSpeed = splits[4];
        String engineSpeed = splits[5];
        String throttle = splits[6];

        if (!timeOfDay.matches("\\d+:\\d+:\\d+.\\d+")) {
          line = br.readLine();
          continue;
        }

        TelemetryRecord tr = new TelemetryRecord();
        tr.setCarNumber(carNumber);
        tr.setDate(date);
        tr.setEngineSpeed(engineSpeed);
        tr.setLapDistance(lapDistance);
        tr.setTimeOfDay(timeOfDay);
        tr.setVehicleSpeed(vehicleSpeed);
        tr.setThrottle(throttle);

        if (!realTiming) {
          this.recordListener.onRecord(tr);
        } else {
          records.computeIfAbsent(carNumber, (s) -> new ConcurrentLinkedQueue<>()).add(tr);
          count.incrementAndGet();
        }
      }
      line = br.readLine();
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
        TelemetryRecord next = recordsQueue.peek();
        if (next != null) {
          foundSomething.set(true);

          RecordTiming recordTiming = lastRecordTime.computeIfAbsent(next.getCarNumber(), s -> new RecordTiming());
          long now = System.currentTimeMillis();

          if (recordTiming.isFirstRecord() || now - recordTiming.getLastRecordSubmittedTime() >=
                  (next.getTimeOfDayLong() - recordTiming.getLastRecordTime() / this.speed)) {
            recordListener.onRecord(recordsQueue.poll());
            recordTiming.setLastRecordTime(next.getTimeOfDayLong());
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
