package iu.edu.indycar.streamer;

public class RecordTiming {

  private long lastRecordTime = -1;
  private long lastRecordSubmittedTime = -1;

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
}
