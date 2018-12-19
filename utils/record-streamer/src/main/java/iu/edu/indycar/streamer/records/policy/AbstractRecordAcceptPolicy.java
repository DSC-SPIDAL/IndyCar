package iu.edu.indycar.streamer.records.policy;

import iu.edu.indycar.streamer.records.IndycarRecord;

public abstract class AbstractRecordAcceptPolicy<R extends IndycarRecord> {

  public abstract boolean evaluate(R record);

}
