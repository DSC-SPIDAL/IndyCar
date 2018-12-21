package iu.edu.indycar;

public class WebsocketServer {

  public static void main(String[] args) {
    ServerBoot serverBoot = new ServerBoot("localhost", 9092);
    serverBoot.start();
  }
}
