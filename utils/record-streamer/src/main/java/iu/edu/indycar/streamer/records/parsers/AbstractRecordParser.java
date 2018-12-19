package iu.edu.indycar.streamer.records.parsers;

import iu.edu.indycar.streamer.exceptions.NotParseableException;
import iu.edu.indycar.streamer.records.IndycarRecord;

public abstract class AbstractRecordParser<T extends IndycarRecord> {

  protected String splitBy;

  public AbstractRecordParser(String splitBy) {
    this.splitBy = splitBy;
  }

  public abstract T parse(String line) throws NotParseableException;
}
