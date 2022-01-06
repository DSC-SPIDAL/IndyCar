#!/usr/bin/env python


import os
from cloudmesh.common.Shell import Shell

commands = {}

def menu():
    commands["i"] = ["Info", ""]
    commands["c"] = ["Clear", ""]    
    
    commands["1"] = ["Minikube start", "minikube start driver=docker"]
    commands["2"] = ["Start k services", "./setup_k8.sh"]
    commands["3"] = ["K-UI", "gopen http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login"]
    commands["4"] = ["Start other services", "cd storm; ./setup.sh"]
    commands["5"] = ["Storm UI", ""]
    commands["6"] = ["Start MQTT", "kubectl create -f activemq-apollo.json; kubectl create -f activemq-apollo-service.json"]
    commands["7"] = ["Start Socket server", "kubectl create -f socket-server.yaml"]
    
    commands["9"] = ["Stop Minikube", "minikube stop"]
    commands["t"] = ["Info MQTT", "kubectl logs activemq-apollo"]
    

    
def find_pid(port):
    try:
        r = Shell.run(f"ss -lntupw | fgrep {port}").strip().split()[6].split(",")[1].split("=")[1]
        return r 
    except:
        return ""

    
def show():
    for i in commands:
        print (f"{i} {commands[i][0]:<20} |  {commands[i][1]}")

def storm_port():
    r = Shell.run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "storm-ui")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r
    

def show_storm_ui():
    port = storm_port()
    ip = Shell.run("minikube ip").strip()
    os.system(f"gopen http://{ip}:{port}")


def info():
    ip = Shell.run(f"minikube ip")
    print ("IP:", ip)
    
    pods = Shell.run(f"kubectl get pods")
    print ("PODS")
    print (pods)

    services = Shell.run(f"kubectl get services")
    print ("SERVICES")
    print (services)
    
    print ("PORTS")
    print("8001 pid:", find_pid("8001"))
    print("storm-ui port:", storm_port())
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
