from kubernetes import client, config
import time
import json

# Configs can be set in Configuration class directly or using helper utility
config.load_kube_config()
v1 = client.CoreV1Api()


def is_namespace_exist(name):
    for i in v1.list_namespace().items:
        if i.metadata.name == name:
            return True
    return False


def is_pod_exist(name, namespace='default'):
    ret = v1.list_namespaced_pod(namespace=namespace, watch=False)
    for i in ret.items:
        if i.metadata.name == name:
            return True
    return False


def is_service_exist(name, namespace='default'):
    try:
        v1.read_namespaced_service(name, namespace, pretty='true')
        return True
    except:
        return False


def is_deployment_exist(name, namespace='default'):
    try:
        client.AppsV1Api().read_namespaced_deployment(name, namespace)
        return True
    except:
        return False


def create_namespace(name):
    body = {
        "apiVersion": "v1",
        "kind": "Namespace",
        "metadata": {
            "name": name,
            "labels": {
                "name": name
            }}}
    if is_namespace_exist(name):
        print("Namespace '%s' already exist." % name)
    else:
        v1.create_namespace(body)
        print("Namespace '%s' has been created." % name)


def delete_namespace(name):
    if is_namespace_exist(name):
        v1.delete_namespace(name, grace_period_seconds=0)
        # block until it is deleted
        while is_namespace_exist(name):
            time.sleep(3)
        print("Namespace '%s' has been deleted." % name)


def blocking_create_pod(pod_manifest, namespace='default'):
    """
    This function calls Kubernetes API to create a pod. This function will busy wait till pod changes to the Running status
    :param pod_manifest: pod manifest
    :return:
    """
    name = pod_manifest["metadata"]["name"]
    print("Creating POD", name)
    resp = v1.create_namespaced_pod(body=pod_manifest,
                                    namespace=namespace)
    while True:
        resp = v1.read_namespaced_pod(name=name,
                                      namespace=namespace)
        if resp.status.phase != 'Pending':
            break
        time.sleep(3)
    print("Pod %s has been created" % name)


def create_pod(pod_manifest, namespace='default'):
    """
    This function calls Kubernetes API to create a pod.
    :param pod_manifest: pod manifest
    :return:
    """
    name = pod_manifest["metadata"]["name"]
    print("Creating POD", name)
    resp = v1.create_namespaced_pod(body=pod_manifest,
                                    namespace=namespace)
    print("Pod creation for %s has been submitted. Pod will become ready in several seconds" % name)


def create_service(service_manifest, namespace='default'):
    """
    This function calls Kubernetes API to create a service.
    :param pod_manifest: pod manifest
    :return:
    """
    name = service_manifest["metadata"]["name"]
    print("Creating Service", name)
    resp = v1.create_namespaced_service(body=service_manifest,
                                        namespace=namespace)
    return resp


def create_deployment(deployment_manifest, namespace):
    """
    This function calls Kubernetes API to create a deployment.
    :param deployment_manifest: deployment manifest
    :return:
    """
    if True:
        name = deployment_manifest["metadata"]["name"]
        print("Creating Deployment", name)
        resp = client.AppsV1Api().create_namespaced_deployment(body=deployment_manifest, namespace=namespace)
        print("Deployment for %s has been submitted. Deployment will become ready in several seconds" % name)
    # except:
    #    print("Deployment  %s already exists!" % name)


def delete_deployment(name, namespace):
    try:
        print('Deleting deployment %s' % name)
        # TODO wait until delete is done.
        client.AppsV1Api().delete_namespaced_deployment(name, namespace)
    except:
        print('Service is not exist')


def list_pods(namespace='default'):
    ret = v1.list_namespaced_pod(namespace='jhub', watch=False)
    for i in ret.items:
        print("%s\t%s\t%s" % (i.status.pod_ip, i.metadata.namespace, i.metadata.name))


def get_pod_log(pod_name, namespace='default'):
    print(v1.read_namespaced_pod_log(pod_name, namespace, pretty='true'))


def list_all_services(namespace='default'):
    # TODO: print some fields, not all
    print(v1.list_namespaced_service(namespace=namespace, pretty='true'))


def read_service(name, namespace='default'):
    print(v1.read_namespaced_service(name, namespace, pretty='true'))


def delete_service(name, namespace='default'):
    try:
        print('Deleting service %s' % name)
        # TODO wait until delete is done.
        v1.delete_namespaced_service(name, namespace)
    except:
        print('Service is not exist')

def blocking_delete_pod(name, namespace):
    v1.delete_namespaced_pod(name, namespace)
    while (is_pod_exist(name, namespace)):
        time.sleep(3)
    print('Pod %s has been deleted' % name)
