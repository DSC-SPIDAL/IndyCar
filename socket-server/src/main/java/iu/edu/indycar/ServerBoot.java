package iu.edu.indycar;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import iu.edu.indycar.models.AnomalyMessage;
import iu.edu.indycar.models.CarPositionMessage;
import iu.edu.indycar.models.JoinRoomMessage;
import iu.edu.indycar.streamer.records.WeatherRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerBoot {

    private final static Logger LOG = LogManager.getLogger(ServerBoot.class);

    private final static String EVENT_SUB = "EVENT_SUB";
    private final static String EVENT_UNSUB = "EVENT_UNSUB";

    private String host;
    private int port;

    private SocketIOServer server;

    //initial records for newly connected clients
    private WeatherRecord lastWeatherRecord;

    public ServerBoot(String host, int port) {
        this.host = host;
        this.port = port;

        Configuration config = new Configuration();
        config.setHostname(this.host);
        config.setPort(this.port);

        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);

        config.setSocketConfig(socketConfig);

        this.server = new SocketIOServer(config);
    }

    public void publishAnomalyEvent(AnomalyMessage anomalyMessage) {
        String room = anomalyMessage.getCarNumber() + anomalyMessage.getAnomalyType();
        LOG.info("Sending event to room {}", room);
        this.server.getRoomOperations(room).sendEvent("anomaly", anomalyMessage);
    }

    public void publishPositionEvent(CarPositionMessage carPositionMessage) {
        this.server.getBroadcastOperations().sendEvent("position", carPositionMessage);
    }

    public void publishWeatherEvent(WeatherRecord weatherRecord) {
        this.lastWeatherRecord = weatherRecord;
        this.server.getBroadcastOperations().sendEvent("weather", weatherRecord);
    }

    public void start() {

        server.addConnectListener(socketIOClient -> {
            LOG.info("Client {} connected", socketIOClient.getRemoteAddress());
            if (this.lastWeatherRecord != null) {
                socketIOClient.sendEvent("weather", this.lastWeatherRecord);
            }
        });

        server.addDisconnectListener(socketIOClient ->
                LOG.info("Client {} disconnected", socketIOClient.getRemoteAddress()));

        server.addEventListener(
                EVENT_SUB, JoinRoomMessage.class,
                (socketIOClient, joinRoomMessage, ackRequest) -> {
                    LOG.info("Join room[{}] request received from {}",
                            joinRoomMessage.getRoomName(), socketIOClient.getRemoteAddress());
                    socketIOClient.joinRoom(joinRoomMessage.getRoomName());
                    ackRequest.sendAckData();
                }
        );

        server.addEventListener(
                EVENT_UNSUB, JoinRoomMessage.class,
                (socketIOClient, joinRoomMessage, ackRequest) -> {
                    LOG.info("Leave room[{}] request received from {}",
                            joinRoomMessage.getRoomName(), socketIOClient.getRemoteAddress());
                    socketIOClient.leaveRoom(joinRoomMessage.getRoomName());
                    ackRequest.sendAckData();
                }
        );
//    final Random random = new Random();
//
//    final AtomicInteger atomicInteger = new AtomicInteger(0);
//
//    File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-16_0.log");
//
//    RecordStreamer recordStreamer = new RecordStreamer(
//            file, true, 1, s -> s.split("_")[2]);
//
//    recordStreamer.setTelemetryRecordListener(telemetryRecord -> {
//      if (telemetryRecord.getCarNumber().equals("10")) {
//        AnomalyMessage anomalyMessage = new AnomalyMessage();
//        anomalyMessage.setCarNumber(9);
//        anomalyMessage.setAnomaly(random.nextDouble());
//        anomalyMessage.setRawData(Double.valueOf(telemetryRecord.getVehicleSpeed()));
//        anomalyMessage.setIndex(atomicInteger.incrementAndGet());
//        server.getBroadcastOperations().sendEvent("anomaly", anomalyMessage);
//      }
//    });
//
//    recordStreamer.addRecordAcceptPolicy(TelemetryRecord.class,
//            new AbstractRecordAcceptPolicy<TelemetryRecord>() {
//
//              HashMap<String, Boolean> metFirstNonZero = new HashMap<>();
//
//              @Override
//              public boolean evaluate(TelemetryRecord record) {
//                if (metFirstNonZero.getOrDefault(record.getCarNumber(), false)) {
//                  return true;
//                } else if (Double.valueOf(record.getVehicleSpeed()) > 10) {
//                  metFirstNonZero.put(record.getCarNumber(), true);
//                  return true;
//                }
//                return false;
//              }
//            });
//
//    System.out.println("Starting record streamer...");
//
//    //recordStreamer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Stopping Server");
            server.stop();
        }));

        LOG.info("Starting server...");
        server.start();
    }
}
