package iu.edu.indycar.streamer;

import iu.edu.indycar.streamer.exceptions.NotParsableException;
import iu.edu.indycar.streamer.records.*;
import iu.edu.indycar.streamer.records.parsers.CompletedLapRecordParser;
import iu.edu.indycar.streamer.records.parsers.EntryRecordParser;
import iu.edu.indycar.streamer.records.parsers.TelemetryRecordParser;
import iu.edu.indycar.streamer.records.parsers.WeatherRecordParser;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import iu.edu.indycar.streamer.records.policy.DefaultRecordAcceptPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordStreamer implements StreamEndListener {

    private final static Logger LOG = LogManager.getLogger(RecordStreamer.class);

    //Listeners
    private RecordListener<IndycarRecord> recordListener;
    private RecordListener<WeatherRecord> weatherRecordListener;
    private RecordListener<TelemetryRecord> telemetryRecordListener;
    private RecordListener<EntryRecord> entryRecordRecordListener;
    private RecordListener<CompleteLapRecord> completeLapRecordRecordListener;

    private StreamEndListener streamEndListener;

    private File file;
    private boolean realTiming;
    private int speed = 1;

    private boolean fileEnded;

    private FileNameDateExtractor dateExtractor;

    private boolean run = true;

    private ConcurrentHashMap<String, RecordTiming> records = new ConcurrentHashMap<>();

    private HashMap<Class<? extends IndycarRecord>,
            AbstractRecordAcceptPolicy> recordAcceptPolicies = new HashMap<>();

    private AtomicInteger timersCount = new AtomicInteger(0);

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

    public void setEntryRecordRecordListener(RecordListener<EntryRecord> entryRecordRecordListener) {
        this.entryRecordRecordListener = entryRecordRecordListener;
    }

    public void setCompleteLapRecordRecordListener(RecordListener<CompleteLapRecord> completeLapRecordRecordListener) {
        this.completeLapRecordRecordListener = completeLapRecordRecordListener;
    }

    public void setStreamEndListener(StreamEndListener streamEndListener) {
        this.streamEndListener = streamEndListener;
    }

    public void start() {
        new Thread(() -> {
            try {
                readFile();
            } catch (IOException e) {
                System.out.println("Error in reading files");
            }
        }, "file-reader").start();
    }

    public void stop() {
        LOG.info("Stopping record streamer...");
        this.run = false;
        this.records.forEachValue(10, RecordTiming::stop);
    }

    private void queueEvent(IndycarRecord indycarRecord) {
        if (!realTiming || !indycarRecord.isTimeSensitive()) {
            this.publishEvent(indycarRecord);
        } else {
            try {
                if (!this.records.containsKey(indycarRecord.getGroupTag())) {
                    this.timersCount.incrementAndGet();
                }
                this.records.computeIfAbsent(indycarRecord.getGroupTag(),
                        (s) -> new RecordTiming(
                                indycarRecord.getGroupTag(),
                                this::publishEvent,
                                this.speed, this)
                ).enqueue(indycarRecord);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        } else if (this.entryRecordRecordListener != null && indycarRecord instanceof EntryRecord) {
            this.entryRecordRecordListener.onRecord((EntryRecord) indycarRecord);
        } else if (this.completeLapRecordRecordListener != null && indycarRecord instanceof CompleteLapRecord) {
            this.completeLapRecordRecordListener.onRecord((CompleteLapRecord) indycarRecord);
        }
    }

    public void addRecordAcceptPolicy(Class<? extends IndycarRecord> clazz, AbstractRecordAcceptPolicy tRecord) {
        this.recordAcceptPolicies.put(clazz, tRecord);
    }

    private void readFile() throws IOException {
        FileReader fis = new FileReader(file);

        String date = this.dateExtractor.extractDate(file.getName());

        BufferedReader br = new BufferedReader(fis);
        String line = br.readLine();

        TelemetryRecordParser telemetryRecordParser = new TelemetryRecordParser("�");
        WeatherRecordParser weatherRecordParser = new WeatherRecordParser("�");
        EntryRecordParser entryRecordParser = new EntryRecordParser("�");
        CompletedLapRecordParser completedLapRecordParser = new CompletedLapRecordParser("�");

        while (line != null && this.run) {
            try {
                IndycarRecord record = null;
                if (line.startsWith("$P")) {
                    TelemetryRecord tr = telemetryRecordParser.parse(line);
                    tr.setDate(date);
                    record = tr;
                } else if (line.startsWith("$W")) {
                    record = weatherRecordParser.parse(line);
                } else if (line.startsWith("$E")) {
                    record = entryRecordParser.parse(line);
                } else if (line.startsWith("$C")) {
                    record = completedLapRecordParser.parse(line);
                }
                if (record != null && this.recordAcceptPolicies.getOrDefault(
                        record.getClass(), DefaultRecordAcceptPolicy.getInstance()).evaluate(record)) {
                    this.queueEvent(record);
                }
            } catch (NotParsableException e) {
                if (e.isLog()) {
                    LOG.warn("Couldn't parse a record", e);
                }
            } catch (Exception ex) {
                LOG.warn("Unexpected exception occurred when parsing the record {}", line, ex);
            } finally {
                line = br.readLine();
            }
        }
        br.close();
        this.fileEnded = true;
        LOG.info("End of File : {}", file.getName());

        if (!this.realTiming && this.streamEndListener != null) {
            this.streamEndListener.onStreamEnd();
        }
    }

    @Override
    public void onStreamEnd(String tag) {
        LOG.info("Stream ended for {}. Streams left : {}", tag, this.timersCount.get());
        if (this.timersCount.decrementAndGet() == 5) {
            LOG.info("All streams ended");
            if (this.streamEndListener != null) {
                this.streamEndListener.onStreamEnd();
            }
            this.stop();
        }
    }
}
