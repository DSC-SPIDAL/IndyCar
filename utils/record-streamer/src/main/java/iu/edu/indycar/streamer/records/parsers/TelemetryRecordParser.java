package iu.edu.indycar.streamer.records.parsers;

import iu.edu.indycar.streamer.TimeUtils;
import iu.edu.indycar.streamer.exceptions.NotParsableException;
import iu.edu.indycar.streamer.records.TelemetryRecord;

public class TelemetryRecordParser extends AbstractRecordParser<TelemetryRecord> {

    public TelemetryRecordParser(String splitBy) {
        super(splitBy);
    }

    @Override
    public TelemetryRecord parse(String line) throws NotParsableException {

        String[] splits = line.split(this.splitBy);
        String carNumber = splits[1];

        String timeOfDay = splits[2];

        if (!timeOfDay.matches("\\d+:\\d+:\\d+.\\d+")) {
            NotParsableException nex = new NotParsableException("Invalid time of day format : " + timeOfDay);
            nex.setLog(false);
            throw nex;
        }

        double lapDistance = Double.valueOf(splits[3]);
        double vehicleSpeed = Double.valueOf(splits[4]);
        double engineSpeed = Double.valueOf(splits[5]);
        double throttle = Double.valueOf(splits[8]);


        TelemetryRecord tr = new TelemetryRecord();
        tr.setCarNumber(carNumber);
        tr.setEngineSpeed(engineSpeed);
        tr.setLapDistance(lapDistance);
        tr.setTimeOfDay(timeOfDay);
        tr.setVehicleSpeed(vehicleSpeed);
        tr.setThrottle(throttle);

        tr.setTimeOfDayLong(TimeUtils.convertTimestampToLong(timeOfDay));

        return tr;
    }
}
