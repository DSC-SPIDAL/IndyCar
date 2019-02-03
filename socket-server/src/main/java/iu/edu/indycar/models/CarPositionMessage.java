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

    public synchronized void recordPosition(String carNumber, double distance, long time) {
        CarPositionRecord carPositionRecord = this.computeIfAbsent(
                carNumber, cp -> new CarPositionRecord(distance, time, carNumber));
        carPositionRecord.setDistance(distance);
        carPositionRecord.setTime(time);
        //carPositionRecord.setDeltaTime(deltaTime);
        //carPositionRecord.setDeltaDistance(distance - carPositionRecord.getDistance());
    }

    public synchronized CarPositionMessage buildMessage() {
        CarPositionMessage carPositionMessage = new CarPositionMessage();
        carPositionMessage.putAll(this);
        this.clear();
        return carPositionMessage;
    }
}
