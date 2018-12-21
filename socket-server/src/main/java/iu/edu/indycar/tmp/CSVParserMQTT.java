package com.dsc.iu.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CSVParserMQTT implements MqttCallback {
  private static MqttClient client;
  private static MqttMessage msgobj = new MqttMessage();
  private static String payload;

  public static void main(String[] args) throws IOException {
    CSVParserMQTT ob = new CSVParserMQTT();
    ob.connectToBroker();

    Map<String, BufferedReader> rdrmap = new HashMap<>();

    File dir = new File("/home/chathura/indycar/files");
    if (dir.isDirectory()) {
      File[] anomalyscorefiles = dir.listFiles();
      for (File f : anomalyscorefiles) {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        rdrmap.put(f.getName(), rdr);
      }
    }
    randomdatapublish(rdrmap);
  }

  public static void randomdatapublish(Map<String, BufferedReader> map) throws IOException {
    String record, carnum, metricname;
    Random random = new Random();
    int index = random.nextInt(map.size());

    record = map.get(map.keySet().toArray()[index]).readLine();

    while (record != null) {

      String key_filename = (String) map.keySet().toArray()[index];
      carnum = key_filename.split("_")[0].split("-")[1];
      metricname = key_filename.split("_")[1].split("\\.")[0].toUpperCase();

      try {
        //car #, metric name, metric value, anomaly score
        payload = carnum + "," + metricname + "," + record.split(",")[1] + "," + record.split(",")[2];
        msgobj.setQos(2);
        msgobj.setPayload(payload.getBytes());
        client.publish("streaming_output", msgobj);

      } catch (MqttException m) {
        m.printStackTrace();
      }

      index = random.nextInt(map.size());
      record = map.get(map.keySet().toArray()[index]).readLine();

      if (record == null) {
        System.out.println("closed reading for file:" + key_filename);

        map.remove(map.keySet().toArray()[index]);
        map.get(map.keySet().toArray()[index]).close();

        index = random.nextInt(map.size());
        record = map.get(map.keySet().toArray()[index]).readLine();
      }
    }
  }

  public void connectToBroker() {
    MqttConnectOptions conn = new MqttConnectOptions();

    //changing # of inflight messages from default 10 to 500
    conn.setMaxInflight(500);

    conn.setAutomaticReconnect(true);
    conn.setCleanSession(true);
    conn.setConnectionTimeout(30);
    conn.setKeepAliveInterval(30);
    conn.setUserName("admin");
    conn.setPassword("password".toCharArray());

    try {
      client = new MqttClient("tcp://127.0.0.1:61613", MqttClient.generateClientId());
      client.setCallback(this);
      client.connect(conn);
    } catch (MqttException m) {
      m.printStackTrace();
    }
  }

  @Override
  public void connectionLost(Throwable cause) {
    // TODO Auto-generated method stub
    System.out.println("lost connection...");
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // TODO Auto-generated method stub

  }
}