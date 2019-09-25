package iu.edu.indycar.streamer.records;

public class CompleteLapRecord implements IndycarRecord {

    private int rank;
    private String carNumber;
    private int completedLaps;
    private long time;
    private long elapsedTime;

    private String lapStatus;
    private long fastestLapTime;
    private int fastestLap;
    private long timeBehindLeader;
    private int lapsBehindLeader;
    private long timeBehindPrec;
    private int lapsBehindPrec;
    private long overallBestLapTime;
    private int pitStopsCount;
    private int lastPittedLap;
    private int startPosition;
    private int lapsLed;
    private int overallRank;

    private String trackStatus;

    public String getTrackStatus() {
        return trackStatus;
    }

    public void setTrackStatus(String trackStatus) {
        this.trackStatus = trackStatus;
    }

    public String getLapStatus() {
        return lapStatus;
    }

    public void setLapStatus(String lapStatus) {
        this.lapStatus = lapStatus;
    }

    public int getOverallRank() {
        return overallRank;
    }

    public void setOverallRank(int overallRank) {
        this.overallRank = overallRank;
    }

    public long getOverallBestLapTime() {
        return overallBestLapTime;
    }

    public void setOverallBestLapTime(long overallBestLapTime) {
        this.overallBestLapTime = overallBestLapTime;
    }

    public int getFastestLap() {
        return fastestLap;
    }

    public void setFastestLap(int fastestLap) {
        this.fastestLap = fastestLap;
    }

    public long getFastestLapTime() {
        return fastestLapTime;
    }

    public void setFastestLapTime(long fastestLapTime) {
        this.fastestLapTime = fastestLapTime;
    }

    public long getTimeBehindLeader() {
        return timeBehindLeader;
    }

    public void setTimeBehindLeader(long timeBehindLeader) {
        this.timeBehindLeader = timeBehindLeader;
    }

    public int getLapsBehindLeader() {
        return lapsBehindLeader;
    }

    public void setLapsBehindLeader(int lapsBehindLeader) {
        this.lapsBehindLeader = lapsBehindLeader;
    }

    public long getTimeBehindPrec() {
        return timeBehindPrec;
    }

    public void setTimeBehindPrec(long timeBehindPrec) {
        this.timeBehindPrec = timeBehindPrec;
    }

    public int getLapsBehindPrec() {
        return lapsBehindPrec;
    }

    public void setLapsBehindPrec(int lapsBehindPrec) {
        this.lapsBehindPrec = lapsBehindPrec;
    }

    public int getPitStopsCount() {
        return pitStopsCount;
    }

    public void setPitStopsCount(int pitStopsCount) {
        this.pitStopsCount = pitStopsCount;
    }

    public int getLastPittedLap() {
        return lastPittedLap;
    }

    public void setLastPittedLap(int lastPittedLap) {
        this.lastPittedLap = lastPittedLap;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getLapsLed() {
        return lapsLed;
    }

    public void setLapsLed(int lapsLed) {
        this.lapsLed = lapsLed;
    }

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
