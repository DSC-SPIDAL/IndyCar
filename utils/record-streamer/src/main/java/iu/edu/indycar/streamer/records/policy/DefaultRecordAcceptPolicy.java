package iu.edu.indycar.streamer.records.policy;

import iu.edu.indycar.streamer.records.IndycarRecord;

public class DefaultRecordAcceptPolicy<R extends IndycarRecord> extends AbstractRecordAcceptPolicy<R> {

  private final static DefaultRecordAcceptPolicy INSTANCE = new DefaultRecordAcceptPolicy();

  public static DefaultRecordAcceptPolicy getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean evaluate(R record) {
    return true;
  }
}
