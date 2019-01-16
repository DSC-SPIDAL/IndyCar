package iu.edu.indycar;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import iu.edu.indycar.models.AnomalyMessage;
import iu.edu.indycar.models.CarPositionMessage;
import iu.edu.indycar.models.JoinRoomMessage;
import iu.edu.indycar.streamer.records.CompleteLapRecord;
import iu.edu.indycar.streamer.records.EntryRecord;
import iu.edu.indycar.streamer.records.WeatherRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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

    public ServerBoot(String host, int port) {

        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);

        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);

        config.setSocketConfig(socketConfig);

        this.server = new SocketIOServer(config);
    }

    public void publishAnomalyEvent(AnomalyMessage anomalyMessage) {
        String room = anomalyMessage.getCarNumber() + anomalyMessage.getAnomalyType();
        //LOG.info("Sending event to room {}", room);
        this.server.getRoomOperations(room).sendEvent("anomaly", anomalyMessage);
    }

    public void publishPositionEvent(CarPositionMessage carPositionMessage) {
        this.server.getBroadcastOperations().sendEvent("position", carPositionMessage);
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

    public void publishCompletedLapRecord(CompleteLapRecord completeLapRecord) {
        lapRecords.computeIfAbsent(
                completeLapRecord.getCarNumber(),
                (s) -> new ArrayList<>()
        ).add(completeLapRecord);
        this.server.getBroadcastOperations().sendEvent("lap-record", completeLapRecord);
    }

    public void sendReloadEvent() {
        this.server.getBroadcastOperations().sendEvent("reload");
    }

    public void reset() {
        this.lastWeatherRecord = null;
        this.entryRecordSet = new HashSet<>();
        this.lapRecords = new HashMap<>();
    }

    public void start() {

        server.addConnectListener(socketIOClient -> {
            LOG.info("Client {} connected", socketIOClient.getRemoteAddress());
            //broadcast initial weather
            if (this.lastWeatherRecord != null) {
                socketIOClient.sendEvent("weather", this.lastWeatherRecord);
            }

            //broadcast entries
            if (!this.entryRecordSet.isEmpty()) {
                LOG.info("Sending initial entries to {}", socketIOClient.getRemoteAddress().toString());
                socketIOClient.sendEvent("entries", this.entryRecordSet);
            }

            //broadcast lap records
            socketIOClient.sendEvent("lap-records", this.lapRecords);
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Stopping Server");
            server.stop();
        }));

        LOG.info("Starting server...");
        server.start();
    }
}
