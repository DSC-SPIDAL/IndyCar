package iu.edu.indycar;

//todo move to a config file
public class ServerConstants {

    public static final String CONNECTION_URL = "tcp://j-093.juliet.futuresystems.org:61613";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "xyi5b2YUcw8CHhAE";

    public static final String ANOMALY_TOPIC = "streaming_output";

    public static final String LOG_FILE = "/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log";

    public static final int EVENT_EMITTER_THREADS = 4;

    public static final boolean DEBUG_MODE = true;
    public static int DEBUG_CARS = 8;
    public static final String DEBUG_TOPIC = "debug_topic_2";
}
