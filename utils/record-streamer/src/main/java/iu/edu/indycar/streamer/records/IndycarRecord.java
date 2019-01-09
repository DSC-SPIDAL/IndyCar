package iu.edu.indycar.streamer.records;

public interface IndycarRecord {

    String getGroupTag();

    long getTimeField();

    default boolean isTimeSensitive() {
        return false;
    }
}
