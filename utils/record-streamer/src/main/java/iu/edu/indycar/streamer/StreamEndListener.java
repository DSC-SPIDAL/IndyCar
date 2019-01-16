package iu.edu.indycar.streamer;

public interface StreamEndListener {
    void onStreamEnd(String tag);

    default void onStreamEnd() {
        this.onStreamEnd(null);
    }
}
