import http.client
import json
import time
import uuid

import paho.mqtt.client as mqtt
import urllib3
import yaml
from kubernetes import client

urllib3.disable_warnings()


def _create_pod_create_json(topology_name, source_topic, destination_topic,
                            storm_jar="/data/topologies/Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar",
                            class_name="com.dsc.iu.streaming.AnomalyDetectionTask"):
    """
    This function can be used to generate the Kubernetes Pod definition to deploy a storm job
    :param topology_name: name of the storm topology
    :param source_topic: name of the source MQTT topic
    :param destination_topic: name of the destination MQTT topic
    :param storm_jar: absolute path to the storm toplogy jar
    :param class_name: fully qualified java class name of the spout
    :return: JSON representation of the kubernetes POD
    """
    # create the yaml file and save before returning the json
    job_uuid = str(uuid.uuid4())
    job_pod_name = "storm-job-" + topology_name + "-" + job_uuid

    print("Deploying job", job_pod_name)
    yaml_file = "/data/topologies/" + job_uuid + ".yaml"
    yaml_config = {
        "name": topology_name,
        "config": {
            "topology": {
                "workers": 1,
                "debug": False,
                "acker": {
                    "executors": 0
                }
            }
        },
        "spouts": [
            {
                "id": topology_name + "_spout",
                "className": class_name,
                "parallelism": 1,
                "constructorArgs": [source_topic, destination_topic]
            }
        ]
    }
    file = open(yaml_file, 'w')
    file.write(yaml.dump(yaml_config))
    file.close()
    return {
        "kind": "Pod",
        "apiVersion": "v1",
        "metadata": {
            "name": job_pod_name,
            "labels": {
                "name": "storm-job"
            }
        },
        "spec": {
            "nodeName": "d001",
            "volumes": [
                {
                    "name": "config",
                    "hostPath": {
                        "path": "/nfs/indycar/config"
                    }
                },
                {
                    "name": "topologies",
                    "hostPath": {
                        "path": "/nfs/indycar/data"
                    }
                }
            ],
            "containers": [
                {
                    "name": "storm-job",
                    "image": "storm:1.2.3",
                    "args": [
                        "storm",
                        "jar",
                        storm_jar,
                        "org.apache.storm.flux.Flux",
                        "--remote",
                        yaml_file
                    ],
                    "resources": {
                        "limits": {
                            "cpu": "100m"
                        }
                    },
                    "volumeMounts": [
                        {
                            "name": "topologies",
                            "mountPath": "/data/topologies"
                        },
                        {
                            "name": "config",
                            "mountPath": "/conf"
                        }
                    ]
                }
            ],
            "restartPolicy": "OnFailure",
            "terminationGracePeriodSeconds": 30
        }
    }


def _check_topology_exists(storm_host, topology_name):
    """
    This function calls storm's REST API to check whether a topology already exists with the given name
    :param storm_host: storm connection information
    :param topology_name: name of the topology
    :return: topology object if the topology found, else return False
    """
    connection = http.client.HTTPConnection(storm_host)

    connection.request('GET', '/api/v1/topology/summary')

    response = connection.getresponse()
    obj = json.loads(response.read().decode())

    top_array = obj["topologies"]
    for top in top_array:
        if top["name"] == topology_name:
            return top
    return False


def _kill_topology_if_exists(storm_host, topology_name):
    """
    This function first checks whether the topology exists by the provided name. If it exists, calls storm's REST API
    to kill that topology
    :param storm_host: storm connection information
    :param topology_name: name of the topology
    :return: True if the topology doesn't exist, or if the existing topology has been successfully killed. False on failure
    """
    top = _check_topology_exists(storm_host, topology_name)
    if top:
        print("Topology already exists. Killing...")
        connection = http.client.HTTPConnection(storm_host)
        print("Waiting 30 secs to kill the topology...")
        connection.request('POST', '/api/v1/topology/' + top["id"] + '/kill/' + str(30))
        response = connection.getresponse()
        obj = json.loads(response.read().decode())
        print(obj)
        if obj["status"] == "success":
            print("Topology Killed...")
            return True
        else:
            print("Failed to kill the toplogy. Manually killing might be required..")
            return False
    return True


def _blocking_pod_creation(api, pod_manifest):
    """
    This function calls Kubernetes API to create a pod. This function will busy wait till pod changes to the Running status
    :param api: an instance of the kubernets API
    :param pod_manifest: pod manifest
    :return:
    """
    name = pod_manifest["metadata"]["name"]
    print("Creating POD", name)
    resp = api.create_namespaced_pod(body=pod_manifest,
                                     namespace='default')
    while True:
        resp = api.read_namespaced_pod(name=name,
                                       namespace='default')
        if resp.status.phase != 'Pending':
            print("Pod is : " + resp.status.phase + "...")
            break
        print("Pod is creating : " + resp.status.phase + "...")
        time.sleep(30)
    print("Done.")


def _blocking_deployment_creation(api, deployment_manifest):
    """
    This function calls Kubernetes API to create a deployment. This function will busy wait till deployment changes to the Running status
    :param api: an instance of the kubernets API
    :param deployment_manifest: deployment manifest
    :return:
    """
    name = deployment_manifest["metadata"]["name"]
    print("Creating Deployement", name)
    resp = api.create_namespaced_deployment(body=deployment_manifest,
                                            namespace='default')
    while True:
        resp = api.read_namespaced_pod(name=name,
                                       namespace='default')
        if resp.status.phase != 'Pending':
            print("Deployment is : " + resp.status.phase + "...")
            break
        print("Deployment is creating : " + resp.status.phase + "...")
        time.sleep(30)
    print("Done.")


def default_on_message(client, userdata, msg):
    """
    This function will be used as the default MQTT client on message call back, if user doesn't provide their own implementation.
    This function simply prints the message content along with the topic name to the conosle
    :param client: an instance of the client
    :param userdata: user data
    :param msg: msg object
    :return:
    """
    print(msg.topic + " " + str(msg.payload))


class DetectionCell:
    def __init__(self, storm_jar="Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar",
                 class_name="com.dsc.iu.streaming.AnomalyDetectionTask"):
        self.storm_jar = storm_jar
        self.class_name = class_name


class Config:
    def __init__(self, k8_token):
        self.k8_token = k8_token
        self.k8_host = "https://kubernetes.default.svc:443"
        self.storm_host = "storm-ui:8080"

        self.broker_host = "activemq-apollo"
        self.broker_port = 32247
        self.broker_username = "admin"
        self.broker_password = "password"

        self.volume_mounts = {
            "topologies": {
                "hostPath": "/nfs/indycar/data",
                "mountPath": "/data/topologies"
            },
            "config": {
                "hostPath": "/nfs/indycar/config",
                "mountPath": "/conf"
            }
        }

    def storm(self, host, port):
        self.storm_host = host + ":" + str(port)
        return self

    def broker(self, host, port, username, password):
        self.broker_host = host
        self.broker_port = port
        self.broker_username = username
        self.broker_password = password
        return self

    def __volume(self, vol, host_path, mount_path):
        self.volume_mounts[vol]["hostPath"] = host_path
        self.volume_mounts[vol]["mountPath"] = mount_path

    def topologies_volume(self, host_path, mount_path):
        self.__volume("topologies", host_path, mount_path)

    def config_volume(self, host_path, mount_path):
        self.__volume("config", host_path, mount_path)


class Orchestrator:
    def __init__(self, config):
        k8_config = client.Configuration()
        k8_config.host = config.k8_host
        k8_config.verify_ssl = False
        k8_config.api_key = {"authorization": "Bearer " + config.k8_token}

        k8_client = client.ApiClient(k8_config)
        self.k8 = client.CoreV1Api(k8_client)

        self.config = config

    def deploy_stream(self, name, input_topic, output_topic, detection_cell=DetectionCell()):
        jar_path = self.config.volume_mounts["topologies"]["mountPath"] + "/" + detection_cell.storm_jar
        _kill_topology_if_exists(self.config.storm_host, name)
        _blocking_pod_creation(self.k8, _create_pod_create_json(name, input_topic, output_topic,
                                                                storm_jar=jar_path,
                                                                class_name=detection_cell.class_name))

    def kill_stream(self, name):
        _kill_topology_if_exists(self.config.storm_host, name)

    def probe_topic(self, topics=None, on_message=default_on_message):
        if topics is None:
            topics = []

        def __on_broker_connected(client, userdata, flags, rc):
            print("Connected with result code " + str(rc))

            for topic in topics:
                client.subscribe(topic)

        client = mqtt.Client(client_id="oc-client")
        client.on_connect = __on_broker_connected
        client.on_message = on_message
        client.username_pw_set(self.config.broker_username, password=self.config.broker_password)

        client.connect(self.config.broker_host, int(self.config.broker_port), 60)
        return client
