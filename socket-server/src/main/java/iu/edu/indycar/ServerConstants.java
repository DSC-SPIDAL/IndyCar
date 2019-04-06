package iu.edu.indycar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//todo move to a config file
public class ServerConstants {

    public static final String CONNECTION_URL = "tcp://j-093.juliet.futuresystems.org:61613";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "xyi5b2YUcw8CHhAE";

    public static final String ANOMALY_TOPIC = "streaming_output";
    public static final String STATUS_TOPIC = "status";

    public static final String LOG_FILE = "/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log";

    public static final int EVENT_EMITTER_THREADS = 1;

    public static final boolean CALCULATE_MQTT_LATENCY = false;

    public static final boolean DIRECT_STREAM_DISTANCE = false;
    public static final boolean DIRECT_STREAM_CAR_FILTER = true;
    public static final Set<String> DIRECT_STREAM_CARS = new HashSet<>(Arrays.asList("21", "13", "20", "19", "24", "26", "33", "98"));

    public static final boolean DEBUG_MODE = false;
    public static int DEBUG_CARS = 33;
    public static final String DEBUG_TOPIC = "debug_topic_2";

    public static final int WS_PORT = DEBUG_MODE ? 61521 : 5000;
}
