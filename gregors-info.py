#!/usr/bin/env python

import time
import os
from cloudmesh.common.Shell import Shell
from cloudmesh.common.util import readfile, writefile
import sys
import textwrap
from cloudmesh.common.StopWatch import StopWatch
from cloudmesh.common.dotdict import dotdict
import time

commands = {}

screen = os.get_terminal_size()
# print(screen.columns)
# print(screen.lines)

def benchmark(func):
    def wrapper():
        StopWatch.start(func.__name__)
        func()
        StopWatch.stop(func.__name__)
    return wrapper

@benchmark
def kill():
    try:
        os.remove("history.txt")
    except:
        pass
    pid = find_pid("8001")
    os_system(f"kill -9 {pid}")
    os_system("minikube stop")
    os_system("minikube delete")


def find_pid(port):
    try:
        r = Shell_run(f"ss -lntupw | fgrep {port}").strip().split()[6].split(",")[1].split("=")[1]
        return r
    except:
        return ""

def add_history(msg):
    file = open("history.txt", "a")  # append mode
    file.write(f"{msg}\n")
    file.close()

def get_token():
    print ("TOKEN")
    r = Shell_run("kubectl -n kubernetes-dashboard"
                  " describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')")
    lines = r.splitlines()
    for line in lines:
        if line.startswith("token:"):
            break
    line = line.replace("token:", "").strip()
    return line



# drivers
# * Shell.run
# * os.system

HOME = os.environ["INDYCAR"] = os.getcwd()
CONTAINERIZE = f"{HOME}/containerize"
STORM = f"{HOME}/containerize/storm"
STREAMING = f"{HOME}/streaming"
DATA = f"{HOME}/data"


def execute(commands, sleep_time=1, driver=Shell.run):
    print(screen.columns * "=")
    print(commands)
    print(screen.columns * "=")

    result = ""
    for command in commands.splitlines():
        if command.strip().startswith("#"):
            print (command)
        else:
            print(command)
            add_history(command)
            r = driver(command)
            print(r)
            result = result + str(r)
            time.sleep(sleep_time)
    return result

def os_system(command):
    return execute(command, driver=os.system)

def os_system(command):
    return execute(command, driver=os.system)

def Shell_run(command):
    return execute(command, driver=Shell.run)


def clean_script(script):
    return textwrap.dedent(script).strip()

@benchmark
def get_code(home="/tmp"):
    script = clean_script(f"""
    mkdir -p {home}/indycar
    cd {home}/indycar; git clone https://github.com/DSC-SPIDAL/IndyCar.git
    """)
    execute(script)

@benchmark
def install_htm_java(directory="/tmp"):

    if Shell.which("mvn") != "":
        execute("sudo apt install -y maven", driver=os.system)

    os.system("rm -rf ~/.m2")
    os.system("mkdir -p ~/.m2")
    os.system(f"rm -rf {directory}/htm.java-examples")

    script = clean_script(f"""
        cd {directory}; git clone https://github.com/numenta/htm.java-examples.git
        # cd {directory}; git clone git@github.com:laszewsk/htm.java-examples.git
        cp -r {directory}/htm.java-examples/libs/algorithmfoundry ~/.m2/repository
        """
    )
    print(script)
    execute(script, driver=os.system)

@benchmark
def install_streaming(directory="/tmp"):

    script = clean_script(f"""
        cd {HOME}/streaming; mvn clean install
        """
    )
    print(script)
    execute(script, driver=os.system)

@benchmark
def download_data(id="11sKWJMjzvhfMZbH7S8Yf4sGBYO3I5s_O",
                  filename="data/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log"):

    directory = os.path.dirname(filename)
    os.system(f"mkdir -p {directory}")
    command = f'curl -c /tmp/cookies "https://drive.google.com/uc?export=download&id={id}" > /tmp/intermezzo.html'
    print(command)
    os.system(command)
    command = f'curl -L -b /tmp/cookies "https://drive.google.com$(cat /tmp/intermezzo.html |'\
              f' grep -Po \'uc-download-link" [^>]* href="\K[^"]*\' | sed \'s/\&amp;/\&/g\')" > {filename}'
    print(command)
    os.system(command)

@benchmark
def setup_minikube(memory=10000, cpus=8, sleep_time=0):
    script = f"""
    minikube delete
    minikube config set memory {memory}
    minikube config set cpus {cpus}
    minikube start driver=docker
    """
    execute(script, driver=os.system)
    time.sleep(sleep_time)

@benchmark
def setup_k8():
    token = get_token()
    script = \
    f"""
    # deploy dahshboard
    #kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-beta8/aio/deploy/recommended.yaml
    
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.4.0/aio/deploy/recommended.yaml
    
    # create user
    cd {CONTAINERIZE}; kubectl create -f account.yaml
    
    # create role
    cd {CONTAINERIZE}; kubectl create -f role.yaml    
    """
    execute(script, driver=os.system)

    print (token)

    script = \
    f"""
    # start dashboard
    cd {CONTAINERIZE}; kubectl proxy &
    """
    execute(script, driver=os.system)

def minikube_ip():
    ip = Shell_run("minikube ip").strip()
    return ip

def wait_for(name):
    print (f"Starting {name}: ")
    found = False
    while not found:
        try:
            r = Shell.run("kubectl get pods").splitlines()
            r = Shell.find_lines_with(r, name)[0]
            if "Running" in r:
                found  = True
                print(f"ok. Pod {name} running")
            else:
                print (".", end="", flush=True)
                time.sleep(1)
        except:
            print(".", end="", flush=True)
            time.sleep(1)


@benchmark
def setup_zookeeper():
    script = \
    f"""
    kubectl create -f {STORM}/zookeeper.json 
    kubectl create -f {STORM}/zookeeper-service.json
    """
    execute(script, driver=os.system)
    time.sleep(30)
    wait_for("zookeeper")

@benchmark
def setup_nimbus():
    script = \
    f"""
    kubectl create -f {STORM}/storm-nimbus.json
    kubectl create -f {STORM}/storm-nimbus-service.json
    """
    execute(script, driver=os.system)
    wait_for("nimbus")

@benchmark
def setup_storm_ui():
    script = \
    f"""
    kubectl create -f {STORM}/storm-ui.json
    kubectl create -f {STORM}/storm-ui-service.json
    """
    execute(script, driver=os.system)
    wait_for("storm-ui")


def storm_port():
    r = Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "storm-ui")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r

@benchmark
def open_stopm_ui():
    port = storm_port()
    ip = minikube_ip()
    os.system(f"gopen http://{ip}:{port}")
    wait_for_storm_ui()


def wait_for_storm_ui():
    print ("Probe storm-ui: ")
    found = False
    port = storm_port()
    ip = minikube_ip()
    while not found:
        try:
            r = Shell.run (f"curl http://{ip}:{port}/index.html")
            found = "Storm Flux YAML Viewer" in r
        except:
            time.sleep(1)
            pass
        print (".", end="", flush=True)
    print (" ok")
    os.system(f"gopen http://{ip}:{port}/index.html")


@benchmark
def start_storm_workers():
    script = \
        f"""
        kubectl create -f {STORM}/storm-worker-controller.json
        """
    execute(script, driver=os.system)
    wait_for("storm-worker-controller")

@benchmark
def setup_mqtt():
    script = \
    f"""
    kubectl create -f {CONTAINERIZE}/activemq-apollo.json
    kubectl create -f {CONTAINERIZE}/activemq-apollo-service.json
    """
    execute(script, driver=os.system)
    wait_for("activemq-apollo")

@benchmark
def start_storm_topology():
    ip = minikube_ip()
    key = Shell.run("minikube ssh-key").strip()
    script = \
        f"""
        cd {STREAMING}; mvn clean install
        #cd {STREAMING}; scp -i {key} target/Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar docker@$(minikube ip):/nfs/indycar/data/
        cd {STREAMING}; scp -i {key} target/Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar docker@{ip}:/nfs/indycar/data/
        """
    print(script)
    execute(script, driver=os.system)

@benchmark
def minikube_setup_sh():
    LOGFILE=f"{DATA}/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log"
    ip = minikube_ip()
    key = Shell.run("minikube ssh-key").strip()

    script=f"""
    minikube ssh "sudo chmod -R 777 /nfs/indycar"
    minikube ssh "mkdir /nfs/indycar/datalogs"
    minikube ssh "mkdir /nfs/indycar/config/lib/"
    # copy log file into minikube
    # change the path of the log file accordingly.
    scp -i {key} {LOGFILE} docker@${ip}:/nfs/indycar/datalogs/
    
    # copy LSTM model files into minikube
    scp -i {key} -r models docker@${ip}:/nfs/indycar/config/
    
    # Following link is for Linux CPU only. For other platforms, check https://www.tensorflow.org/install/lang_java
    wget https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow_jni-cpu-linux-x86_64-1.14.0.tar.gz
    mkdir tf-lib
    tar -xzvf libtensorflow_jni-cpu-linux-x86_64-1.14.0.tar.gz -C tf-lib
    scp -i {key} tf-lib/* docker@${ip}:/nfs/indycar/config/lib/
    """
    execute(script, driver=os.system)
    # wait for something?

@benchmark
def start_socket_server():
    script= \
        f"""
        kubectl create -f socket-server.yaml
        """
    execute(script, driver=os.system)



print (HOME)
print (CONTAINERIZE)
print(STREAMING)
print(DATA)

def main():
    try:
        kill()
        ## get_code()
        install_htm_java()
        download_data()
        setup_minikube()
        setup_k8()


        setup_zookeeper()


        setup_nimbus()
        #time.sleep(5 * 60)

        setup_storm_ui()
        open_stopm_ui()


        start_storm_workers()
        setup_mqtt()

        start_storm_topology()
        raise RuntimeError

        #minikube_setup_sh()
        #start_socket_server()

        StopWatch.benchmark(sysinfo=True, attributes="short")
    except Exception as e:
        print (e)
        StopWatch.benchmark(sysinfo=False, attributes="short")



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
    
def os_system(command):
    return execute(command, driver=os.system)

def start_zookeeper():
    # os.system("kubectl create -f storm/zookeeper.json")
    found = False
    while not found:
        r = Shell_run("kubectl get pods").splitlines()
        print (r)
        r = Shell.find_lines_with(r, "zookeeper")[0]
        print(r)
        if "Running" in r:
            found  = True
            print ("zookeeper pod running")
        else:
            time.sleep(1)
    os.system("kubectl create -f storm/zookeeper-service.json")

            

def show():
    print (screen.columns * "=")
    for i in commands:
        print (f" {i:<2} {commands[i][0]:<20} |  {commands[i][1]}")
    print (screen.columns * "=")


def notebook_port():
    r = Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "8888")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r

def show_notebook():
    port = notebook_port()
    ip = Shell_run("minikube ip").strip()
    os_system(f"gopen http://{ip}:{port}")

def create_notebook():
    #port = notebook_port()
    #ip = Shell_run("minikube ip").strip()
    token = get_token()
    print(token)
    content = readfile("car-notebook-in.py")
    content = content.format(token=token)
    print (screen.columns * "=")
    print (content)
    print(screen.columns * "=")

    #writefile(content, "abc.py")


def show_storm_ui():
    port = storm_port()
    ip = Shell_run("minikube ip").strip()
    os_system(f"gopen http://{ip}:{port}")

def zookeeper_running():
    try:
        r =  Shell_run("kubectl logs zookeeper").strip()
        return "ZooKeeper audit is disabled." in r
    except:
        return False

def mqtt_running():
    try:
        r =  Shell_run("kubectl logs activemq-apollo").strip()
        return "Administration interface available at: http://127.0.0.1:" in r
    except:
        return False

def info():
    print ("Zookeeper running:", zookeeper_running())    
    print ("MQTT      running:", mqtt_running())
    
    try:
        ip = Shell_run(f"minikube ip")
        print ("IP:               ", ip)
    except:
        pass
    
    pods = Shell_run(f"kubectl get pods")
    print ("PODS")
    print (pods)

    services = Shell_run(f"kubectl get services")
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
    os_system("kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')")
    print()

while True:
    menu()
    show()
    s = input()
    print('You typed ' + s) 


    print (screen.columns * "=")
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

        try:
            os.remove("history.txt")
        except:
            pass
        pid = find_pid("8001")
        os_system(f"kill -9 {pid}")
        
        os_system("minikube stop")
        os_system("minikube delete")
        os_system("minikube config set memory 10000")
        os_system("minikube config set cpus 8")

        # os_system("minikube start driver=virtualbox")
        
    else:
        os_system(commands[s][1])
    print (screen.columns * "=")
