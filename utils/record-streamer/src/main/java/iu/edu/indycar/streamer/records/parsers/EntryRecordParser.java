package iu.edu.indycar.streamer.records.parsers;

import iu.edu.indycar.streamer.exceptions.NotParsableException;
import iu.edu.indycar.streamer.records.EntryRecord;

public class EntryRecordParser extends AbstractRecordParser<EntryRecord> {

    public EntryRecordParser(String splitBy) {
        super(splitBy);
    }

    @Override
    public EntryRecord parse(String line) throws NotParsableException {
        String[] splits = line.split(splitBy);
        try {
            EntryRecord entryRecord = new EntryRecord();
            entryRecord.setCarNumber(splits[4]);
            entryRecord.setUid(splits[5]);
            entryRecord.setDriverName(splits[6]);
            entryRecord.setLicense(splits[13]);
            entryRecord.setTeam(splits[14]);
            entryRecord.setEngine(splits[16]);
            entryRecord.setHometown(splits[19]);
            return entryRecord;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new NotParsableException(ex);
        }
    }
}
