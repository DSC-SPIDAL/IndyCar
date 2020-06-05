package iu.edu.indycar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//todo move to a config file
public class ServerConstants {
//  public static final String CONNECTION_URL = "tcp://j-093.juliet.futuresystems.org:61613";
  public static final String CONNECTION_URL = "tcp://activemq-apollo:31758";
  public static final String USERNAME = "admin";
  public static final String PASSWORD = "password";
//  public static final String USERNAME = "admin";
//  public static final String PASSWORD = "qq6bTB3cZ9Fb8bgR";

  //public static final String ANOMALY_TOPIC = "streaming_output";
  public static final String ANOMALY_TOPIC = "compact_topology_out";
  public static final String STATUS_TOPIC = "status";

  public static final String LOG_FILE = "/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-27_0.log";

  public static final boolean CALCULATE_MQTT_LATENCY = true;

  public static final String[] CARS_BY_ORDER_OF_RECORDS = {
          "22",
          "88",
          "66",
          "23",
          "24",
          "25",
          "26",
          "27",
          "28",
          "29",
          "98",
          "32",
          "12",
          "14",
          "59",
          "15",
          "17",
          "18",
          "19",
          "1",
          "3",
          "4",
          "6",
          "7",
          "9",
          "60",
          "64",
          "20",
          "21",
          "30",
          "10",
          "33",
          "13"
  };


  public static int NO_OF_STREAMING_CARS = 3;

  public static final boolean DIRECT_STREAM_DISTANCE = false;
  public static final boolean DIRECT_STREAM_CAR_FILTER = true;
  public static final Set<String> DIRECT_STREAM_CARS = new HashSet<>(
          Arrays.asList("21", "13", "20", "19", "24", "26", "33", "98")
  );

  public static final boolean DEBUG_MODE = false;
  public static final String DEBUG_TOPIC = "debug_topic_2";

  public static final int WS_PORT = DEBUG_MODE ? 61521 : 5000;
  public static final boolean WRITE_DEBUG_LOGS = false;

  public static final String RANK_PRED_REST = "http://r-001:8501/v1/models/rank:predict";
  public static final String NEW_RANK_PRED_REST = "http://localhost:5001/predict";

  public static final boolean USE_OLD_PREDICTION = false;

  public static final int RANK_PRED_RECORDS_PER_REQ = 5;
}
