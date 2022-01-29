#!/usr/bin/env python

import time
import os
from cloudmesh.common.Shell import Shell
from cloudmesh.common.util import readfile, writefile

commands = {}

def menu():
    commands["i"] = ["Info", ""]
    commands["c"] = ["Clear", ""]    
    
    commands["1"] = ["Minikube start", "minikube start driver=docker"]
    commands["2"] = ["Start k services", "./setup_k8.sh"]
    commands["3"] = ["K-UI", "gopen http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login"]
    commands["4"] = ["Start other services", "cd storm; ./setup.sh"]
    commands["5"] = ["Storm UI", "in the browser refresh till you see the UI"]
    commands["6"] = ["Start MQTT", "kubectl create -f activemq-apollo.json; kubectl create -f activemq-apollo-service.json"]
    commands["7"] = ["copy key", "scp -i $(minikube ssh-key) ../streaming/target/Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar docker@$(minikube ip):/nfs/indycar/data/"]
    commands["8"] = ["Start Socket server", "kubectl create -f socket-server.yaml"]
    commands["9"] = ["permissions", 'minikube ssh "sudo chmod -R 777 /nfs/indycar"']
    commands["10"] = ["jupyter", "kubectl create -f storm/jupyter.yaml"]
    commands["11"] = ["permissions", 'minikube ssh "sudo chmod -R 777 /nfs/indycar"']
    commands["12"] = ["start notebook", ""]
    commands["13"] = ["create notebook", ""]

    
    commands["s"] = ["Stop Minikube", "minikube stop"]
    
    commands["z"] = ["zookeeper", ""]
    
def start_zookeeper():
    # os.system("kubectl create -f storm/zookeeper.json")
    found = False
    while not found:
        r = Shell.run("kubectl get pods").splitlines()
        print (r)
        r = Shell.find_lines_with(r, "zookeeper")[0]
        print(r)
        if "Running" in r:
            found  = True
            print ("zookeeper pod running")
        else:
            time.sleep(10)
    # os.system("kubectl create -f storm/zookeeper-service.json")

            
def find_pid(port):
    try:
        r = Shell.run(f"ss -lntupw | fgrep {port}").strip().split()[6].split(",")[1].split("=")[1]
        return r 
    except:
        return ""
    
def show():
    print (79 * "=")
    for i in commands:
        print (f" {i:<2} {commands[i][0]:<20} |  {commands[i][1]}")
    print (79 * "=")
    
def storm_port():
    r = Shell.run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "storm-ui")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r

def notebook_port():
    r = Shell.run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "8888")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r

def show_notebook():
    port = notebook_port()
    ip = Shell.run("minikube ip").strip()
    os.system(f"gopen http://{ip}:{port}")

def create_notebook():
    #port = notebook_port()
    #ip = Shell.run("minikube ip").strip()
    token = get_token()
    print(token)
    content = readfile("car-notebook-in.py")
    content = content.format(token=token)
    print(79*"-")
    print (content)
    print(79 * "-")

    #writefile(content, "abc.py")


def show_storm_ui():
    port = storm_port()
    ip = Shell.run("minikube ip").strip()
    os.system(f"gopen http://{ip}:{port}")

def zookeeper_running():
    try:
        r =  Shell.run("kubectl logs zookeeper").strip()
        return "ZooKeeper audit is disabled." in r
    except:
        return False

def mqtt_running():
    try:
        r =  Shell.run("kubectl logs activemq-apollo").strip()
        return "Administration interface available at: http://127.0.0.1:" in r
    except:
        return False

def get_token():
    print ("TOKEN")
    r = Shell.run("kubectl -n kubernetes-dashboard"
                  " describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')")
    lines = r.splitlines()
    for line in lines:
        if line.startswith("token:"):
            break
    line = line.replace("token:", "").strip()
    return line

def info():
    print ("Zookeeper running:", zookeeper_running())    
    print ("MQTT      running:", mqtt_running())
    
    try:
        ip = Shell.run(f"minikube ip")
        print ("IP:               ", ip)
    except:
        pass
    
    pods = Shell.run(f"kubectl get pods")
    print ("PODS")
    print (pods)

    services = Shell.run(f"kubectl get services")
    print ("SERVICES")
    print (services)
    
    print ("PORTS")
    try:
        print("8001 pid:", find_pid("8001"))
    except:
        pass
    try:
        print("storm-ui port:", storm_port())
    except:
        pass
    try:
        print("notebook port:", notebook_port())
    except:
        pass
    print()

    print ("TOKEN")
    os.system("kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')")
    print()

while True:
    menu()
    show()
    s = input()
    print('You typed ' + s) 


    print(79 * "-")
    if s == "k":
        print("Port 8001:", find_port("8001"))

    elif s == "i":
        info()

    elif s == "5":
        show_storm_ui()

    elif s == "12":
        show_notebook()
    elif s == "13":
        create_notebook()


    elif s == "z":
        start_zookeeper()

    elif s == "c":

        pid = find_pid("8001")
        os.system(f"kill -9 {pid}")
        
        os.system("minikube stop")
        os.system("minikube delete")
        os.system("minikube config set memory 10000")
        os.system("minikube config set cpus 8")

        # os.system("minikube start driver=virtualbox")                                        
        
    else:
        os.system(commands[s][1])
    print(79 * "-")
