import iuindycar.kube_client as kube_client
import pprint
import json

pp = pprint.PrettyPrinter (indent=4)


def __get_broker_pod_body (name, namespace, cpu_limit=2):
    """
    returns broker pod body
    :param name: name of pod
    :param namespace: namespace
    :param cpu_limit:
    :return: pod body dict
    """
    return {"kind": "Pod",
            "apiVersion": "v1",
            "metadata": {
                "name": name,
                "labels": {"name": name},
                "namespace": namespace},
            "spec": {
                "containers": [{
                    "name": "activemq-artemis",
                    "image": "crowbary/apache-apollo",
                    "ports": [{"name": "apollo-ui-http",
                               "containerPort": 61680}, {
                                  "name": "apollo-mqtt",
                                  "containerPort": 61613}],
                    "resources": {
                        "limits": {
                            "cpu": str (cpu_limit)}}}]}}


def __get_broker_service_body (name, namespace):
    """
    Generates broker service body
    :param name: name of service
    :param namespace: namespace
    :return: service body dict
    """
    return {"kind": "Service",
            "apiVersion": "v1",
            "metadata": {
                "name": name,
                "labels": {"name": name},
                "namespace": namespace},
            "spec": {"ports": [{
                "name": "apollo-ui-http",
                "port": 61680,
                "targetPort": "apollo-ui-http",
                "protocol": "TCP"}, {
                "name": "apollo-mqtt",
                "port": 61613,
                "targetPort": "apollo-mqtt",
                "protocol": "TCP"}],
                "selector": {
                    "name": name},
                "type": "NodePort"}}


def __get_socket_server_pod_body (name, namespace, log_file_folder_path, log_file_name, broker_address,
                                  broker_topic_prefix, broker_output_topic_prefix, num_of_cars,
                                  cpu_limit=2, memory_limit="10G"):
    """
    Creates indycar socket server body
    :param name: name of deployment
    :param namespace: namespace
    :param log_file_folder_path: indycar log folder (this should be in nfs directory)
    :param log_file_name: log file name
    :param broker_address: broker's address. It could be public or internal address
    :param broker_topic_prefix: topic prefix
    :param broker_output_topic_prefix: output topic
    :param num_of_cars: number of cars to stream
    :param cpu_limit: cpu limit
    :param memory_limit: memory limit
    :return: body dict
    """
    body = {
        "apiVersion": "apps/v1",
        "kind": "Deployment",
        "metadata": {
            "name": name,
            "namespace": namespace,
            "labels": {
                "app": name
            }
        },
        "spec": {
            "replicas": 1,
            "selector": {
                "matchLabels": {
                    "app": name
                }
            },
            "template": {
                "metadata": {
                    "labels": {
                        "app": name
                    }
                },
                "spec": {
                    "volumes": [
                        {
                            "name": "datalogs",
                            "hostPath": {
                                "path": log_file_folder_path
                            }
                        }
                    ],
                    "containers": [
                        {
                            "name": "iuindycar",
                            "image": "cwidanage/iuindycar1:v6",
                            "ports": [
                                {
                                    "containerPort": 5000
                                }
                            ],
                            "args": [
                                "java",
                                "-jar",
                                "/server.jar",
                                "/data/logs/" + log_file_name,
                                "tcp://" + broker_address,
                                num_of_cars,
                                broker_topic_prefix,
                                broker_output_topic_prefix
                            ],
                            "volumeMounts": [
                                {
                                    "name": "datalogs",
                                    "mountPath": "/data/logs"
                                }
                            ],
                            "resources": {
                                "limits": {
                                    "cpu": str (cpu_limit),
                                    "memory": memory_limit
                                }
                            }
                        }
                    ]
                }
            }
        }
    }
    return body


def __get_socket_server_service_body (name, namespace, target_port):
    """
    Socket Server Service Body. It will fail if the target port is in use.
    :param name:name of service
    :param namespace: namespace
    :param target_port: target nodeport to use.
    :return: service body dict
    """
    return {"kind": "Service",
            "apiVersion": "v1",
            "metadata": {
                "name": name,
                "namespace": namespace},
            "spec": {
                "type": "NodePort",
                "selector": {
                    "app": name},
                "ports": [{
                    "protocol": "TCP",
                    "port": 5000,
                    "targetPort": int (target_port)
                }]}}


def __get_tf_serving_deployment_body (name, namespace, grpc_port, rest_port, models_dir, model_name):
    """
    TensorFlow Serving Deployment Body
    :param name: name of deployment
    :param namespace: namespace
    :param grpc_port: grpc port
    :param rest_port: rest api port
    :param models_dir: model directory. It should be in nfs directory.
    :param model_name: name of the model
    :return: deploymentbody dict
    """
    return {
        "apiVersion": "apps/v1",
        "kind": "Deployment",
        "metadata": {
            "name": name,
            "namespace": namespace
        },
        "spec": {"replicas": 3,
                 "template": {
                     "metadata": {
                         "labels": {"app": name}
                     },
                     "spec": {
                         "volumes": [
                             {
                                 "name": "models",
                                 "hostPath": {
                                     "path": models_dir
                                 }
                             }],
                         "containers": [
                             {
                                 "name": "tensorflow-serving",
                                 "image": "tensorflow/serving:2.3.0",
                                 "ports": [{"containerPort": grpc_port, "name": "grpc"},
                                           {"containerPort": rest_port, "name": "rest"}],
                                 "volumeMounts": [
                                     {
                                         "name": "models",
                                         "mountPath": "/models"
                                     }
                                 ],
                                 "args": ["tensorflow_model_server", "--port=" + str (grpc_port),
                                          "--rest_api_port=" + str (rest_port),
                                          "--model_name=" + model_name, "--model_base_path=/models/" + model_name],
                             },
                         ]
                     }},
                 "selector": {
                     "matchLabels": {
                         "app": name}
                 },
                 }}


def __get_tf_serving_service_body (name, namespace, grcp_port, rest_port):
    return {"apiVersion": "v1",
            "kind": "Service",
            "metadata": {
                "labels": {"run": name},
                "name": name,
                "namespace": namespace},
            "spec": {
                "ports": [{
                    "port": rest_port,
                    "targetPort": rest_port,
                    "name": "rest"},
                    {"port": grcp_port,
                     "targetPort": grcp_port,
                     "name": "grpc"}
                ],
                "selector": {
                    "app": name
                },
                "type": "NodePort"}
            }


def __get_dashboard_deployment_body (name, namespace, socket_server_ip, socket_server_port):
    return {
        "apiVersion": "apps/v1",
        "kind": "Deployment",
        "metadata": {
            "name": name,
            "namespace": namespace
        },
        "spec": {"replicas": 1,
                 "template": {
                     "metadata": {
                         "labels": {"app": name}
                     },
                     "spec": {
                         "containers": [{
                             "name": "indycar-dasboard",
                             "image": "sakkas/indycar:dashboard",
                             "ports": [{"containerPort": 3000, "name": "dashboard-port"}],
                             "env": [{"name": "SOCKET_SERVER_IP", "value": socket_server_ip},
                                     {"name": "SOCKET_SERVER_PORT", "value": socket_server_port}]
                         }]
                     }},
                 "selector": {
                     "matchLabels": {
                         "app": name}
                 },
                 }}


def __get_dashboard_service_body (name, namespace):
    return {"apiVersion": "v1",
            "kind": "Service",
            "metadata": {
                "labels": {"run": name},
                "name": name,
                "namespace": namespace},
            "spec": {
                "ports": [{
                    "port": 3000,
                    "targetPort": "dashboard-port",
                    "name": "dashboard-port"}
                ],
                "selector": {
                    "app": name
                },
                "type": "NodePort"}
            }


def deploy_broker (name, namespace, cpu_limit=2):
    """
    Deploys IndyCar broker.
    :param name: name of deployment. It should be a unique name and it should comply with Kubernetes naming rules.
    :param namespace: namespace
    :param cpu_limit: cpu limit
    """
    if not kube_client.is_namespace_exist (namespace):
        kube_client.create_namespace (namespace)
    if kube_client.is_service_exist (name, namespace):
        kube_client.delete_service (name, namespace)
    if kube_client.is_pod_exist (name, namespace):
        kube_client.blocking_delete_pod (name, namespace)
    kube_client.create_pod (__get_broker_pod_body (name, namespace, cpu_limit), namespace)
    resp = kube_client.create_service (__get_broker_service_body (name, namespace), namespace)
    for i in resp.spec.ports:
        if i.name == 'apollo-mqtt':
            node_port = i.node_port
            port = i.port
            break
    print ('Broker %s has been deployed.' % name)
    print ("\tInside cluster use: %s.%s.svc.cluster.local:%s\n\tOutside the cluster use node-ip:%s"
           % (name, namespace, port, node_port))
    print ('\tusername: admin, password: password')


def delete_broker (name, namespace):
    if kube_client.is_pod_exist (name, namespace):
        kube_client.blocking_delete_pod (name, namespace)
    print ('Broker %s has been deleted.' % name)


def deploy_socket_server (name, namespace, log_file_folder_path, log_file_name, broker_address,
                          broker_topic_prefix, broker_output_topic_prefix, num_of_cars, target_node_port,
                          cpu_limit=2, memory_limit="10G"):
    if not kube_client.is_namespace_exist (namespace):
        kube_client.create_namespace (namespace)
    if kube_client.is_service_exist (name, namespace):
        kube_client.delete_service (name, namespace)
    if kube_client.is_deployment_exist (name, namespace):
        kube_client.delete_deployment (name, namespace)

    kube_client.create_deployment (
        __get_socket_server_pod_body (name, namespace, log_file_folder_path, log_file_name, broker_address,
                                      broker_topic_prefix, broker_output_topic_prefix, num_of_cars, cpu_limit,
                                      memory_limit), namespace)
    resp = kube_client.create_service (__get_socket_server_service_body (name, namespace, target_node_port), namespace)

    print ('SocketServer %s has been deployed.' % name)
    print ("\tInside cluster use: %s.%s.svc.cluster.local:%s\n\tOutside the cluster use node-ip:%s"
           % (name, namespace, '5000', target_node_port))


def delete_socket_server (name, namespace):
    if kube_client.is_service_exist(name, namespace):
        kube_client.delete_service(name, namespace)
    if kube_client.is_deployment_exist (name, namespace):
        kube_client.delete_deployment (name, namespace)
    print ('SocketServer %s has been deleted.' % name)


def __get_anomaly_detection_pod_body (name, namespace, script_path, script_name, car_number, tf_serving_address,
                                      mqtt_address, input_topic, output_topic, series_len, cpu_limit=1):
    return {"kind": "Pod",
            "apiVersion": "v1",
            "metadata": {
                "name": name,
                "labels": {"name": name},
                "namespace": namespace},
            "spec": {
                "volumes": [
                    {
                        "name": "scripts",
                        "hostPath": {
                            "path": script_path
                        }
                    }],
                "containers": [{
                    "name": "indycar-ad",
                    "image": "sakkas/indycar:mqtt-client",
                    "volumeMounts": [
                        {
                            "name": "scripts",
                            "mountPath": "/scripts"
                        }],
                    "args": [
                        "python3",
                        "/scripts/" + script_name,
                        str (car_number),
                        tf_serving_address,
                        mqtt_address,
                        input_topic,
                        output_topic,
                        str (series_len)
                    ],
                    "resources": {
                        "limits": {
                            "cpu": str (cpu_limit)}}}]}}


def deploy_tf_serving (name, namespace, grpc_port, rest_port, models_dir, model_name):
    if not kube_client.is_namespace_exist (namespace):
        kube_client.create_namespace (namespace)
    if kube_client.is_service_exist (name, namespace):
        kube_client.delete_service (name, namespace)
    if kube_client.is_deployment_exist (name, namespace):
        kube_client.delete_deployment (name, namespace)

    kube_client.create_deployment (
        __get_tf_serving_deployment_body (name, namespace, grpc_port, rest_port, models_dir, model_name), namespace)
    resp = kube_client.create_service (__get_tf_serving_service_body (name, namespace, grpc_port, rest_port), namespace)

    print ('TensorFlow Serving %s has been deployed.' % name)
    print ("\tUse: %s.%s.svc.cluster.local:%s for the grpc and %s fot the rest api."
           % (name, namespace, str (grpc_port), str (rest_port)))
    print ("\tNote that TFServing is not accessible from the outside with this configuration")


def deploy_car_anomaly_detection (name, namespace, script_path, script_name, car_number, tf_serving_address,
                                  mqtt_address, input_topic, output_topic, series_len, cpu_limit=1):
    if not kube_client.is_namespace_exist (namespace):
        kube_client.create_namespace (namespace)
    if kube_client.is_service_exist (name, namespace):
        kube_client.delete_service (name, namespace)
    if kube_client.is_pod_exist (name, namespace):
        kube_client.blocking_delete_pod (name, namespace)

    kube_client.create_pod (__get_anomaly_detection_pod_body
                            (name, namespace, script_path, script_name, car_number, tf_serving_address,
                             mqtt_address, input_topic, output_topic, series_len, cpu_limit), namespace)

    print ('Anomaly Detection pod deployed for %s has been deployed.' % input_topic)


def delete_car_anomaly_detection (name, namespace):
    if kube_client.is_pod_exist (name, namespace):
        kube_client.blocking_delete_pod (name, namespace)
    print ('Anomaly detection pod %s has been deleted.' % name)


def deploy_dashboard (name, namespace, socket_server_ip, socket_server_port):
    if not kube_client.is_namespace_exist (namespace):
        kube_client.create_namespace (namespace)
    if kube_client.is_service_exist (name, namespace):
        kube_client.delete_service (name, namespace)
    if kube_client.is_deployment_exist (name, namespace):
        kube_client.delete_deployment (name, namespace)

    kube_client.create_deployment (
        __get_dashboard_deployment_body(name, namespace, socket_server_ip, socket_server_port), namespace)
    resp = kube_client.create_service (__get_dashboard_service_body(name, namespace), namespace)
    for i in resp.spec.ports:
        if i.name == 'dashboard-port':
            node_port = i.node_port
            port = i.port
            break
    print ('IndyCar Dashboard %s has been deployed. It might take several minutes to run' % name)
    print ("\tUse: %s.%s.svc.cluster.local:%s for internal access\n\tOutside the cluster use node-ip:%s"
           % (name, namespace, str (port), str(node_port)))


def delete_dashboard (name, namespace):
    if kube_client.is_service_exist(name, namespace):
        kube_client.delete_service(name, namespace)
    if kube_client.is_deployment_exist (name, namespace):
        kube_client.delete_deployment (name, namespace)
    print ('Dashboard %s has been deleted.' % name)
