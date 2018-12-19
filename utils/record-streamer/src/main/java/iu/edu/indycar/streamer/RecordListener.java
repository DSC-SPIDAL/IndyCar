package iu.edu.indycar.streamer;

import iu.edu.indycar.streamer.records.IndycarRecord;

public interface RecordListener<R extends IndycarRecord> {
  void onRecord(R record);
}
