package iu.edu.indycar.models;

import java.util.HashMap;

public class CarPositionMessage extends HashMap<String, CarPositionRecord> {

    private long sequenceNumber = 0;

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void incrementSequence() {
        this.sequenceNumber++;
    }

    public void reset() {
        this.clear();
        this.sequenceNumber = 0;
    }

    public void recordPosition(String carNumber, double distance, long time, long deltaTime) {
        CarPositionRecord carPositionRecord = this.computeIfAbsent(
                carNumber, cp -> new CarPositionRecord());
        carPositionRecord.setDistance(distance);
        carPositionRecord.setTime(time);
        carPositionRecord.setDeltaTime(deltaTime);
        carPositionRecord.setDeltaDistance(distance - carPositionRecord.getDistance());
    }
}
