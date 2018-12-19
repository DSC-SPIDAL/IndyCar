package iu.edu.indycar.streamer.records.parsers;

import iu.edu.indycar.streamer.exceptions.NotParseableException;
import iu.edu.indycar.streamer.records.TelemetryRecord;

public class TelemetryRecordParser extends AbstractRecordParser<TelemetryRecord> {

  public TelemetryRecordParser(String splitBy) {
    super(splitBy);
  }

  @Override
  public TelemetryRecord parse(String line) throws NotParseableException {

    String[] splits = line.split(this.splitBy);
    String carNumber = splits[1];
    String timeOfDay = splits[2];
    String lapDistance = splits[3];
    String vehicleSpeed = splits[4];
    String engineSpeed = splits[5];
    String throttle = splits[6];

    if (!timeOfDay.matches("\\d+:\\d+:\\d+.\\d+")) {
      throw new NotParseableException();
    }

    TelemetryRecord tr = new TelemetryRecord();
    tr.setCarNumber(carNumber);
    tr.setEngineSpeed(engineSpeed);
    tr.setLapDistance(lapDistance);
    tr.setTimeOfDay(timeOfDay);
    tr.setVehicleSpeed(vehicleSpeed);
    tr.setThrottle(throttle);

    return tr;
  }
}
