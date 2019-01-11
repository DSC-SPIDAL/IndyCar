package iu.edu.indycar.streamer.records;

public class CompleteLapRecord implements IndycarRecord {

    private int rank;
    private String carNumber;
    private int completedLaps;
    private long time;
    private long elapsedTime;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public int getCompletedLaps() {
        return completedLaps;
    }

    public void setCompletedLaps(int completedLaps) {
        this.completedLaps = completedLaps;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    @Override
    public String getGroupTag() {
        return "COMPLETED_LAP_RECORD_" + this.getCarNumber();
    }

    @Override
    public long getTimeField() {
        return this.elapsedTime;
    }

    @Override
    public boolean isTimeSensitive() {
        return true;
    }

    @Override
    public String toString() {
        return "CompleteLapRecord{" +
                "rank=" + rank +
                ", carNumber='" + carNumber + '\'' +
                ", completedLaps=" + completedLaps +
                ", time=" + time +
                ", elapsedTime=" + elapsedTime +
                '}';
    }
}
