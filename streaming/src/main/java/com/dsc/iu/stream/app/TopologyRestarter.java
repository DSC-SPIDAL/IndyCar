package com.dsc.iu.stream.app;

import com.dsc.iu.utils.OnlineLearningUtils;
import org.eclipse.paho.client.mqttv3.*;

import java.io.File;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class TopologyRestarter implements MqttCallback {
    private BlockingDeque<String> q = new LinkedBlockingDeque<>();
    private File topologyJar;
    private File fluxTemplate;

    private MqttClient client;

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
        System.out.println("Waiting for messages to arrive...");
        this.connectbroker();
    }

    @Override
    public void connectionLost(Throwable cause) {


    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload());
        if (msg.equalsIgnoreCase("OK")) {
            ProcessBuilder procbuildr = new ProcessBuilder(
                    "storm", "kill", "INTEL_TOPOLOGY_INDYCAR", "-w", "0");
            System.out.println("going to kill topology...");
            Process p = procbuildr.start();
            p.waitFor();
            Thread.sleep(20000);
            procbuildr = new ProcessBuilder("storm", "jar", topologyJar.getAbsolutePath(),
                    "org.apache.storm.flux.Flux", "--remote", fluxTemplate.getAbsolutePath());
            p = procbuildr.start();
            Thread.sleep(20000);
            System.out.println("started topology again...");
            MqttMessage msgobj = new MqttMessage();
            msgobj.setPayload("START".getBytes());
            msgobj.setQos(2);
            try {
                client.publish(OnlineLearningUtils.restart_topic, msgobj);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
