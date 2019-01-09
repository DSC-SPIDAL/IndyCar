package iu.edu.indycar.streamer.records.parsers;

import iu.edu.indycar.streamer.exceptions.NotParsableException;
import iu.edu.indycar.streamer.records.WeatherRecord;

public class WeatherRecordParser extends AbstractRecordParser<WeatherRecord> {

  public WeatherRecordParser(String splitBy) {
    super(splitBy);
  }

  @Override
  public WeatherRecord parse(String line) throws NotParsableException {
    String[] splits = line.split(this.splitBy);

    try {
      WeatherRecord weatherRecord = new WeatherRecord();
      weatherRecord.setTimeOfDay(Long.valueOf(splits[4], 16));
      weatherRecord.setTemperature(Integer.valueOf(splits[5], 16));
      weatherRecord.setRelativeHumidity(Integer.valueOf(splits[6], 16));
      weatherRecord.setPressure(Integer.valueOf(splits[7], 16));

      return weatherRecord;
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
      throw new NotParsableException();
    }
  }
}
