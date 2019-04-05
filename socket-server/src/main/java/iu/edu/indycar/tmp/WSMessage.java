package iu.edu.indycar.tmp;

import iu.edu.indycar.models.AnomalyMessage;
import iu.edu.indycar.models.CarPositionRecord;
import iu.edu.indycar.streamer.records.IndycarRecord;

public class WSMessage implements IndycarRecord {

    private CarPositionRecord carPositionRecord;
    private AnomalyMessage anomalyMessage;
    private long counter;

    public WSMessage(long counter,
                     CarPositionRecord carPositionRecord,
                     AnomalyMessage anomalyMessage) {
        this.counter = counter;
        this.carPositionRecord = carPositionRecord;
        this.anomalyMessage = anomalyMessage;
    }

    public long getCounter() {
        return counter;
    }

    public AnomalyMessage getAnomalyMessage() {
        return anomalyMessage;
    }

    public CarPositionRecord getCarPositionRecord() {
        return carPositionRecord;
    }

    @Override
    public String getGroupTag() {
        return carPositionRecord.getCarNumber();
    }

    @Override
    public long getTimeField() {
        return carPositionRecord.getTime();
    }

    @Override
    public boolean isTimeSensitive() {
        return true;
    }
}
