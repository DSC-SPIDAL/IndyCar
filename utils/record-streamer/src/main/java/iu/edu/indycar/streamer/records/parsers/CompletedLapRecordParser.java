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
            completeLapRecord.setLapStatus(splits[10]);
            completeLapRecord.setFastestLapTime(Long.valueOf(splits[11], 16) / 10);
            completeLapRecord.setFastestLap(Integer.valueOf(splits[12],16));
            completeLapRecord.setTimeBehindLeader(Long.valueOf(splits[13], 16) / 10);
            completeLapRecord.setLapsBehindLeader(Integer.valueOf(splits[14], 16));
            completeLapRecord.setTimeBehindPrec(Long.valueOf(splits[15], 16) / 10);
            completeLapRecord.setLapsBehindPrec(Integer.valueOf(splits[16], 16));
            completeLapRecord.setOverallRank(Integer.valueOf(splits[17], 16));
            completeLapRecord.setOverallBestLapTime(Long.valueOf(splits[18], 16) / 10);
            completeLapRecord.setTrackStatus(splits[20]);
            completeLapRecord.setPitStopsCount(Integer.valueOf(splits[21], 16));
            completeLapRecord.setLastPittedLap(Integer.valueOf(splits[22], 16));
            completeLapRecord.setStartPosition(Integer.valueOf(splits[23], 16));
            completeLapRecord.setLapsLed(Integer.valueOf(splits[24], 16));

            return completeLapRecord;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new NotParsableException(ex);
        }
    }
}
