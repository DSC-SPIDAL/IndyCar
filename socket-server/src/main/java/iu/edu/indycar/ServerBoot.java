package iu.edu.indycar;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import iu.edu.indycar.models.TelemetryMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ServerBoot {

  private final static Logger LOG = LogManager.getLogger(ServerBoot.class);

  private String host;
  private int port;

  public ServerBoot(String host, int port) {
    this.host = host;
    this.port = port;
  }

  private void start() {

    Configuration config = new Configuration();
    config.setHostname(this.host);
    config.setPort(this.port);

    final SocketIOServer server = new SocketIOServer(config);

    server.addConnectListener(new ConnectListener() {
      public void onConnect(SocketIOClient socketIOClient) {
        LOG.info("Client {} connected", socketIOClient.getRemoteAddress());
        socketIOClient.sendEvent("init");
      }
    });

    server.addDisconnectListener(new DisconnectListener() {
      public void onDisconnect(SocketIOClient socketIOClient) {
        LOG.info("Client {} disconnected", socketIOClient.getRemoteAddress());
      }
    });

    TimerTask t = new TimerTask() {
      @Override
      public void run() {
        TelemetryMessage tm = new TelemetryMessage();
        tm.setCarNumber(9);
        tm.setDistanceFromStart(new Random().nextDouble());
        tm.setTimestamp(new Date());
        server.getBroadcastOperations().sendEvent("telemetry", tm);
      }
    };

    new Timer().schedule(t, 0, 1000);

    server.start();
  }

  public static void main(String[] args) {
    new ServerBoot("localhost", 9092).start();
  }
}
