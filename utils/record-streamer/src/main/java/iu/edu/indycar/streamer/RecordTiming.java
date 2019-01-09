package iu.edu.indycar.streamer;

import iu.edu.indycar.streamer.records.IndycarRecord;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RecordTiming implements Runnable {

    private final int speed;
    private long lastRecordTime = -1;
    private long lastRecordSubmittedTime = -1;

    private BlockingQueue<IndycarRecord> queue = new LinkedBlockingQueue<>();
    private RecordListener<IndycarRecord> recordListener;

    public RecordTiming(String tag, RecordListener<IndycarRecord> recordListener, int speed) {
        this.recordListener = recordListener;
        this.speed = speed;
        new Thread(this, tag).start();
    }

    public long getLastRecordTime() {
        return lastRecordTime;
    }

    public void setLastRecordTime(long lastRecordTime) {
        this.lastRecordTime = lastRecordTime;
    }

    public long getLastRecordSubmittedTime() {
        return lastRecordSubmittedTime;
    }

    public void setLastRecordSubmittedTime(long lastRecordSubmittedTime) {
        this.lastRecordSubmittedTime = lastRecordSubmittedTime;
    }

    public boolean isFirstRecord() {
        return lastRecordTime == -1;
    }

    public void enqueue(IndycarRecord indycarRecord) throws InterruptedException {
        this.queue.put(indycarRecord);
    }

    private void publishRecord(IndycarRecord indycarRecord) {
        this.recordListener.onRecord(indycarRecord);
        this.setLastRecordTime(indycarRecord.getTimeField());
        this.setLastRecordSubmittedTime(System.currentTimeMillis());
    }

    @Override
    public void run() {
        while (true) {
            try {
                IndycarRecord indycarRecord = this.queue.poll(1, TimeUnit.MINUTES);
                if (indycarRecord == null) {
                    continue;
                }
                long now = System.currentTimeMillis();

                if (this.isFirstRecord() || now - this.getLastRecordSubmittedTime() >=
                        (indycarRecord.getTimeField() - this.getLastRecordTime()) / this.speed) {
                    this.publishRecord(indycarRecord);
                } else {
                    Thread.sleep(
                            ((indycarRecord.getTimeField() - this.getLastRecordTime()) / this.speed) -
                                    (now - this.getLastRecordSubmittedTime())
                    );
                    this.publishRecord(indycarRecord);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
