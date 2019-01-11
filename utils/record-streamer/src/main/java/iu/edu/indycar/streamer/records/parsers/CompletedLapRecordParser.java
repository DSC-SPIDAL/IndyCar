package iu.edu.indycar.streamer.records.parsers;

import iu.edu.indycar.streamer.exceptions.NotParsableException;
import iu.edu.indycar.streamer.records.CompleteLapRecord;

public class CompletedLapRecordParser extends AbstractRecordParser<CompleteLapRecord> {

    public CompletedLapRecordParser(String splitBy) {
        super(splitBy);
    }

    @Override
    public CompleteLapRecord parse(String line) throws NotParsableException {
        String[] splits = line.split(splitBy);

        try {
            CompleteLapRecord completeLapRecord = new CompleteLapRecord();
            completeLapRecord.setRank(Integer.valueOf(splits[4], 16));
            completeLapRecord.setCarNumber(splits[5]);
            completeLapRecord.setCompletedLaps(Integer.valueOf(splits[7], 16));
            completeLapRecord.setElapsedTime(Long.valueOf(splits[8], 16) / 10);
            completeLapRecord.setTime(Long.valueOf(splits[9], 16) / 10000);//in seconds
            return completeLapRecord;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new NotParsableException(ex);
        }
    }
}
