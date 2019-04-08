package com.dsc.iu.stream.app;

import com.dsc.iu.utils.OnlineLearningUtils;
import org.eclipse.paho.client.mqttv3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class TopologyRestarter implements MqttCallback, Runnable {
    private BlockingDeque<String> q = new LinkedBlockingDeque<>();
    private File topologyJar;
    private File fluxTemplate;

    public TopologyRestarter(File topologyJar, File fluxTemplate) {
        this.topologyJar = topologyJar;
        this.fluxTemplate = fluxTemplate;
    }

    public MqttClient connectbroker() {
        MqttConnectOptions conn = new MqttConnectOptions();
        conn.setMaxInflight(OnlineLearningUtils.inflightMsgRate);
        conn.setAutomaticReconnect(true);
        conn.setCleanSession(true);
        conn.setConnectionTimeout(30);
        conn.setKeepAliveInterval(10);
        conn.setUserName(OnlineLearningUtils.mqttadmin);
        conn.setPassword(OnlineLearningUtils.mqttpwd.toCharArray());

        MqttClient client;
        try {
            client = new MqttClient(OnlineLearningUtils.brokerurl, MqttClient.generateClientId());
            client.setCallback(this);
            client.connect(conn);
            client.subscribe(OnlineLearningUtils.restart_topic, OnlineLearningUtils.QoS);
            return client;
        } catch (MqttException m) {
            m.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        File topologyJar = new File(args[0]);
        File fluxTemplate = new File(args[1]);

        if (!topologyJar.exists()) {
            System.out.println("Couldn't locate topology jar");
            return;
        }

        if (!fluxTemplate.exists()) {
            System.out.println("Couldn't locate flux template");
            return;
        }

        TopologyRestarter ob = new TopologyRestarter(topologyJar, fluxTemplate);
        ob.listen();
    }

    private void listen() {
        new Thread(this).start();
    }

    @Override
    public void connectionLost(Throwable cause) {


    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        q.add(new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void run() {
        System.out.println("Start listening...");
        MqttClient client = this.connectbroker();
        MqttMessage msgobj = new MqttMessage();
        while (true) {
            try {
                String poll = q.poll(1, TimeUnit.HOURS);
                if (poll != null) {
                    String msg = q.poll();
                    if (msg.equalsIgnoreCase("OK")) {
                        ProcessBuilder procbuildr = new ProcessBuilder(
                                "storm", "kill", "INTEL_TOPOLOGY_INDYCAR", "-w", "0");
                        System.out.println("going to kill topology...");
                        Process p = procbuildr.start();
                        Thread.sleep(20000);
                        //Indycar500-33-PRODUCTION-1.0-SNAPSHOT-jar-with-dependencies.jar
                        procbuildr = new ProcessBuilder("storm", "jar", topologyJar.getAbsolutePath(),
                                "org.apache.storm.flux.Flux", "--remote", fluxTemplate.getAbsolutePath());
                        p = procbuildr.start();
                        Thread.sleep(20000);
                        System.out.println("started topology again");

                        msgobj.setPayload("START".getBytes());
                        msgobj.setQos(2);
                        try {
                            client.publish(OnlineLearningUtils.restart_topic, msgobj);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("No restart signal for 1 hour...");
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
