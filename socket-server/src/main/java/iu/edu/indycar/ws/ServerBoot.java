package iu.edu.indycar.ws;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import iu.edu.indycar.models.*;
import iu.edu.indycar.prediction.RankResult;
import iu.edu.indycar.streamer.RecordTiming;
import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.streamer.records.EntryRecord;
import iu.edu.indycar.streamer.records.WeatherRecord;
import iu.edu.indycar.tmp.CarRank;
import iu.edu.indycar.tmp.PingLatency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerBoot {

  private final static Logger LOG = LogManager.getLogger(ServerBoot.class);

  private final static String EVENT_SUB = "EVENT_SUB";
  private final static String EVENT_UNSUB = "EVENT_UNSUB";

  private SocketIOServer server;

  //initial records for newly connected clients
  private WeatherRecord lastWeatherRecord;

  //initial set of entries for newly connected clients
  private Set<EntryRecord> entryRecordSet = new HashSet<>();

  private HashMap<String, List<CompleteLapRecord>> lapRecords = new HashMap<>();

  private HashMap<String, RecordTiming> recordsTiming = new HashMap<>();
  private AtomicBoolean recordTimingStarted = new AtomicBoolean(false);

  private TimerTask pingTask;
  private TimerTask timeTask;
  private TimerTask positionStreamTask;
  private Timer timer = new Timer();

  private HashMap<SocketAddress, PingLatency> latency = new HashMap<>();

  private RankResult rankResults = new RankResult();
  private ConcurrentHashMap<String, CarRank> ranks = new ConcurrentHashMap<>();
  private HashMap<String, Integer> rankPredictions = new HashMap<>();

  private final Map<String, CarPositionRecord> carPositionRecords = new ConcurrentHashMap<>();

  private final List<ArrayList<CarPositionRecord>> pastRecords = new ArrayList<>();

  private String timeOfServerString = "";

  public ServerBoot(String host, int port) {

    Configuration config = new Configuration();
    config.setHostname(host);
    config.setPort(port);

    SocketConfig socketConfig = new SocketConfig();
    socketConfig.setReuseAddress(true);
    socketConfig.setTcpNoDelay(true);

    config.setSocketConfig(socketConfig);

    this.server = new SocketIOServer(config);
    this.pingTask = new TimerTask() {
      @Override
      public void run() {
        server.getBroadcastOperations().sendEvent("ping", "");
        latency.values().forEach(PingLatency::pingSent);
      }
    };

    this.timeTask = new TimerTask() {
      @Override
      public void run() {
        server.getBroadcastOperations().sendEvent("time", timeOfServerString);
      }
    };

    this.positionStreamTask = new TimerTask() {
      @Override
      public void run() {
        ArrayList<CarPositionRecord> recordsList = null;
        synchronized (carPositionRecords) {
          Collection<CarPositionRecord> records = carPositionRecords.values();
          LOG.debug("Creating a bulk of {} position records", records.size());
          if (!records.isEmpty()) {
            recordsList = new ArrayList<>(records);
            carPositionRecords.clear();
          }
        }

        if (recordsList != null) {
          LOG.debug("Sending the  a bulk of {} position records", recordsList.size());
          server.getRoomOperations("position").sendEvent("position", recordsList);
          synchronized (pastRecords) {
            pastRecords.add(recordsList);
            if (pastRecords.size() > 10) {
              pastRecords.remove(0);
            }
          }
        }

        List<CarRank> values = new ArrayList<>(ranks.values());
        Collections.sort(values);
        server.getBroadcastOperations().sendEvent("ranks", values);
      }
    };
  }

  public void publishAnomalyEvent(AnomalyMessage anomalyMessage) {
    String room = "anomaly_" + anomalyMessage.getCarNumber();
    //LOG.info("Sending event to room {}", room);
    this.server.getRoomOperations(room).sendEvent(room, anomalyMessage);

    //report high anomalies
    double max  = Double.MIN_VALUE;
    Anomaly maxAnomaly = null;
    for (Anomaly value : anomalyMessage.getAnomalies().values()) {
      if(max<value.getAnomaly()){
        max = value.getAnomaly();
        maxAnomaly = value;
      }
    }
    if (maxAnomaly!=null) {
      if (max > 0.6) {
        this.server.getBroadcastOperations().sendEvent("anomaly_class", new AnomalyClass(
                anomalyMessage.getCarNumber(), AnomalyClass.ANOMALY_CLASS_SEVERE,
                maxAnomaly.getAnomalyType()));
      } else if (max > 0.3) {
        this.server.getBroadcastOperations().sendEvent("anomaly_class", new AnomalyClass(
                anomalyMessage.getCarNumber(), AnomalyClass.ANOMALY_CLASS_WARN,
                maxAnomaly.getAnomalyType()));
      }
    }

    //update server time
    synchronized (this) {
      if (timeOfServerString.compareTo(anomalyMessage.getTimeOfDayString()) < 0) {
        timeOfServerString = anomalyMessage.getTimeOfDayString();
      }
    }
  }

  public void publishPositionEvent(CarPositionRecord carPositionRecord, long counter) {
    LOG.debug("Publishing position event {}. Record timing started : {}",
            counter, recordTimingStarted.get());
    carPositionRecord.setSentTime(System.currentTimeMillis());
    synchronized (this.carPositionRecords) {
      this.carPositionRecords.put(carPositionRecord.getCarNumber(), carPositionRecord);
    }
    //this.server.getBroadcastOperations().sendEvent("position", carPositionRecord);

    CarRank carRank = this.ranks.computeIfAbsent(carPositionRecord.getCarNumber(), CarRank::new);
    if (counter == 0) {
      carRank.reset();
    }
    carRank.recordDistance(carPositionRecord.getDistance());
    //start real-timing if not already started
    if (!recordTimingStarted.getAndSet(true)) {
      LOG.info("Starting timed records[lap records,weather] streaming...");
      this.recordsTiming.values().forEach(RecordTiming::start);
    }
  }

  public void publishWeatherEvent(WeatherRecord weatherRecord) {
    this.lastWeatherRecord = weatherRecord;
    this.server.getBroadcastOperations().sendEvent("weather", weatherRecord);
  }

  public void publishEntryRecord(EntryRecord entryRecord) {
    boolean newEntry = this.entryRecordSet.add(entryRecord);
    if (newEntry) {
      this.server.getBroadcastOperations().sendEvent("entry", entryRecord);
    }
  }

  public void publishCompletedLapRecord(CompleteLapRecord completeLapRecord) throws InterruptedException {
    String tag = "LAP-" + completeLapRecord.getCarNumber();
    this.recordsTiming.computeIfAbsent(
            tag,
            s -> {
              RecordTiming recordTiming =
                      new RecordTiming(
                              tag,
                              r -> {
                                CompleteLapRecord clr = (CompleteLapRecord) (r);
                                lapRecords.computeIfAbsent(
                                        clr.getCarNumber(),
                                        (records) -> new ArrayList<>()
                                ).add(clr);
                                server.getBroadcastOperations().sendEvent("lap-record", clr);
                              },
                              1,
                              e -> {
                                //do nothing
                              },
                              recordTimingStarted.get()
                      );
              recordTiming.setPollTimeout(5);
              return recordTiming;
            }
    ).enqueue(completeLapRecord);
  }

  public void publishRankData(RankResult rankResult) {
    this.rankResults = rankResult.copy();
    this.server.getBroadcastOperations().sendEvent("ranking-data", this.rankResults);
    rankResult.markBroadcasted();
  }

  public void sendReloadEvent() {
    this.server.getBroadcastOperations().sendEvent("reload");
  }

  public void reset() {
    LOG.info("Resetting ws server....");
    this.lastWeatherRecord = null;
    this.entryRecordSet = new HashSet<>();
    this.lapRecords = new HashMap<>();
    this.rankPredictions = new HashMap<>();
    this.ranks.clear();
    this.carPositionRecords.clear();
    this.pastRecords.clear();

    //clear timers
    this.recordsTiming.values().forEach(RecordTiming::stop);
    this.recordsTiming.clear();
    this.recordTimingStarted.set(false);

    this.timeOfServerString = "";
  }

  public void start() {

    server.addConnectListener(socketIOClient -> {
      LOG.debug("Client {} connected", socketIOClient.getRemoteAddress());
      //broadcast initial weather
      if (this.lastWeatherRecord != null) {
        socketIOClient.sendEvent("weather", this.lastWeatherRecord);
      }

      //broadcast entries
      if (!this.entryRecordSet.isEmpty()) {
        LOG.debug("Sending initial entries to {}", socketIOClient.getRemoteAddress().toString());
        socketIOClient.sendEvent("entries", this.entryRecordSet);
      }

      //broadcast lap records
      socketIOClient.sendEvent("lap-records", this.lapRecords);

      //broadcast rank predictions
      if (this.rankResults != null) {
        socketIOClient.sendEvent("ranking-data", this.rankResults);
      }

      latency.put(
              socketIOClient.getRemoteAddress(),
              new PingLatency(socketIOClient.getRemoteAddress().toString())
      );

      //broadcast past records
      synchronized (pastRecords) {
        for (ArrayList<CarPositionRecord> pastRecord : pastRecords) {
          socketIOClient.sendEvent("position", pastRecord);
        }
      }
      socketIOClient.joinRoom("position");
    });

    server.addDisconnectListener(socketIOClient -> {
      LOG.debug("Client {} disconnected", socketIOClient.getRemoteAddress());
      latency.remove(socketIOClient.getRemoteAddress());
      socketIOClient.leaveRoom("position");
    });

    server.addEventListener(
            EVENT_SUB, JoinRoomMessage.class,
            (socketIOClient, joinRoomMessage, ackRequest) -> {
              LOG.debug("Join room[{}] request received from {}",
                      joinRoomMessage.getRoomName(), socketIOClient.getRemoteAddress());
              socketIOClient.joinRoom(joinRoomMessage.getRoomName());
              ackRequest.sendAckData();
            }
    );

    server.addEventListener(
            EVENT_UNSUB, JoinRoomMessage.class,
            (socketIOClient, joinRoomMessage, ackRequest) -> {
              LOG.debug("Leave room[{}] request received from {}",
                      joinRoomMessage.getRoomName(), socketIOClient.getRemoteAddress());
              socketIOClient.leaveRoom(joinRoomMessage.getRoomName());
              ackRequest.sendAckData();
            }
    );


//        server.addEventListener("pongi", String.class,
//                (socketIOClient, message, ackRequest) -> {
//                    PingLatency pingLatency = latency.get(socketIOClient.getRemoteAddress());
//                    if (pingLatency != null) {
//                        pingLatency.pongReceived();
//                    }
//                }
//        );

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOG.info("Stopping Server");
      server.stop();
    }));

    LOG.info("Starting server...");
    server.start();
    //timer.schedule(this.pingTask, 0, 500);
    timer.schedule(this.positionStreamTask, 0, 10000);
    timer.schedule(this.timeTask, 0, 1000);
  }
}
