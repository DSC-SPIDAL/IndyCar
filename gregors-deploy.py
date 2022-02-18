#!/usr/bin/env python
"""
Usage:
  gregor-deploy.py --info
  gregor-deploy.py --run [--dashboard] [--stormui] [--ui]
  gregor-deploy.py --step [--dashboard] [--stormui]
  gregor-deploy.py --dashboard
  gregor-deploy.py --stormui
  gregor-deploy.py --kill
  gregor-deploy.py --menu
  gregor-deploy.py --token

Deploys the indycar runtime environment on an ubuntu 20.04 system.

Arguments:
  FILE        optional input file
  CORRECTION  correction angle, needs FILE, --left or --right to be present

Options:
  -h --help
  --info       info command
  --run        run the default deploy workflow (till the bug)
  --step       run the default deploy workflow step by step

Description:

  gregor-deploy.py --info
    gets information about the running services

  gregor-deploy.py --kill
    kills all services

  gregor-deploy.py --run [--dashboard] [--stormui]
    runs the workflow without interruption till the error occurs
    If --dashboard and --storm are not specified neither GUI is started.
    This helps on systems with commandline options only.

  gregor-deploy.py --step [--dashboard] [--stormui]
    runs the workflow while asking in each mayor step if one wants to continue.
    This helps to check for log files at a particular place in the workflow.
    If the workflow is not continued it is interrupted.

  gregor-deploy.py --dashboard
    starts the kubernetes dashboard. Minikube must have been setup before

  gregor-deploy.py --stormui
    starts the storm gui. All of storm must be set up before.

  Examples:
    gregor-deploy.py --run --dashboard --stormui
        runs the workflow without interruptions including the k8 and storm dashboards

    gregor-deploy.py --step --dashboard --stormui
        runs the workflow with continuation questions including the k8 and storm dashboards

    gregor-deploy.py --menu
        allows the selction of a particular step in the workflow

    less $INDYCAR/history.txt

  Possible Bugs:
  1. broken/unused storm-worker-service: The storm-worker-service is mentioned in the storm/setup.sh script. However it is not mentioned in the
     presentation slide that describes the setup. Furthermore when one starts this service, it does not
     work and the probe seems to fail. For the reason that it is not mentioned in the guide and does nt work
     we have not enabled it.
  2. kubectl race condition: A race condition in kubectl was avoided, buy adding an additional second wait time after calling commands.
     If erros still occur, the wait time is suggested to be increased. Currently the wait time is set to 1 second.
  3. htm.java: Installation error of htm.java: This uses an outdated htm.java library. It is uncertain if this
     causes an issue

  Benchmark:
    AMD5950
    +----------------------+----------+---------+
    | Name                 | Status   |    Time |
    |----------------------+----------+---------|
    | kill                 | ok       |  17.134 |
    | download_data        | ok       |   0     |
    | setup_minikube       | ok       |  20.844 |
    | setup_k8             | ok       |  12.507 |
    | setup_zookeeper      | ok       |   7.405 |
    | setup_nimbus         | ok       |   8.462 |
    | setup_storm_ui       | ok       |   4.312 |
    | open_stopm_ui        | ok       | 173.242 |
    | start_storm_workers  | ok       |   3.213 |
    | install_htm_java     | ok       |  52.482 |
    | setup_mqtt           | ok       |  11.591 |
    | start_storm_topology | ok       |  29.605 |
    +----------------------+----------+---------+

    EPY    via vnc
    +----------------------+----------+---------+
    | Name                 | Status   |    Time |
    |----------------------+----------+---------|
    | kill                 | ok       |  19.352 |
    | download_data        | ok       |   0     |
    | setup_minikube       | ok       |  31.828 |
    | setup_k8             | ok       |  12.775 |
    | setup_zookeeper      | ok       |  60.753 |
    | setup_nimbus         | ok       |  93.771 |
    | setup_storm_ui       | ok       |   4.366 |
    | open_stopm_ui        | ok       | 270.364 |
    | start_storm_workers  | ok       |   3.213 |
    | install_htm_java     | ok       | 183.767 |
    | setup_mqtt           | ok       | 122.997 |
    | start_storm_topology | ok       |  52.876 |
    | minikube_setup_sh    | ok       |  37.129 |
    | start_socket_server  | ok       | 113.281 |
    +----------------------+----------+---------+


"""
from docopt import docopt
import os
from cloudmesh.common.console import Console
from cloudmesh.common.Shell import Shell
from cloudmesh.common.util import readfile
from cloudmesh.common.util import writefile
from cloudmesh.common.util import banner as cloudmesh_banner
from cloudmesh.common.console import Console
import sys
import textwrap
from cloudmesh.common.StopWatch import StopWatch
from cloudmesh.common.dotdict import dotdict
import time
from cloudmesh.common.util import yn_choice

commands = {}

screen = os.get_terminal_size()

def banner(msg):
    cloudmesh_banner(msg,c="#")

def hline(c="-"):
    print(screen.columns * c)


def rename(newname):
    def decorator(f):
        f.__name__ = newname
        return f

    return decorator


def benchmark(func):
    @rename(func.__name__)
    def wrapper(*args, **kwargs):
        StopWatch.start(func.__name__)
        func(*args, **kwargs)
        StopWatch.stop(func.__name__)

    return wrapper


@benchmark
def kill_services():
    banner("kill_services")
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
    print("TOKEN")
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
DASHBOARD = f"{HOME}/dashboard"


def execute(commands, sleep_time=1, driver=Shell.run):
    hline()
    print(commands)
    hline()

    result = ""
    for command in commands.splitlines():
        add_history(command)
        if command.strip().startswith("#"):
            print(command)
        else:
            print(command)
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
    banner("get_code")
    script = clean_script(f"""
    mkdir -p {home}/indycar
    cd {home}/indycar; git clone https://github.com/DSC-SPIDAL/IndyCar.git
    """)
    execute(script)


@benchmark
def install_htm_java():
    banner("install_htm_java")
    if Shell.which("mvn") == "":
        execute("sudo apt install -y maven", driver=os.system)

    script = \
        f"""
        rm -rf ~/.m2
        cd {STREAMING}; mvn install
        """
    print(script)
    try:
        execute(script, driver=os.system)
    except:
        pass  # ignore error

    script = \
        f"""
        rm -rf {STREAMING}/htm.java-examples
        cd {STREAMING}; git clone https://github.com/numenta/htm.java-examples.git
        cp -r {STREAMING}/htm.java-examples/libs/algorithmfoundry ~/.m2/repository
        cd {STREAMING}; mvn clean install
        """

    # script = clean_script(f"""
    #    cd {directory}; git clone https://github.com/numenta/htm.java-examples.git
    #    # cd {directory}; git clone git@github.com:laszewsk/htm.java-examples.git
    #    cp -r {directory}/htm.java-examples/libs/algorithmfoundry ~/.m2/repository
    #    """
    # )

    print(script)
    execute(script, driver=os.system)


@benchmark
def install_streaming(directory="/tmp"):
    banner("install_streaming")
    script = clean_script(f"""
        cd {HOME}/streaming; mvn clean install
        """
                          )
    print(script)
    execute(script, driver=os.system)


@benchmark
def download_data(id="11sKWJMjzvhfMZbH7S8Yf4sGBYO3I5s_O",
                  filename="data/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log"):
    banner("download_data")
    if not os.path.exists(filename):
        directory = os.path.dirname(filename)
        execute(f"mkdir -p {directory}", driver=os.system)
        command = f'curl -c /tmp/cookies "https://drive.google.com/uc?export=download&id={id}" > /tmp/intermezzo.html'
        print(command)
        execute(command, driver=os.system)
        command = f'curl -L -b /tmp/cookies "https://drive.google.com$(cat /tmp/intermezzo.html |' \
                  f' grep -Po \'uc-download-link" [^>]* href="\K[^"]*\' | sed \'s/\&amp;/\&/g\')" > {filename}'
        print(command)
        execute(command, driver=os.system)
    else:
        print("data already downloaded")


@benchmark
def setup_minikube(memory=10000, cpus=8, sleep_time=0):
    banner("setup_minikube")
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
    banner("setup_k8")
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

    print(token)

    script = \
        f"""
    # start dashboard
    cd {CONTAINERIZE}; kubectl proxy &
    """
    execute(script, driver=os.system)


def minikube_ip():
    ip = Shell_run("minikube ip").strip()
    return ip


def open_k8_dashboard():
    banner("open_k8_dashboard")
    global dashboard
    if dashboard:
        token = get_token()
        hline()
        print("TOKEN")
        hline()
        print(token)
        hline()
        execute("gopen http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:"
                "kubernetes-dashboard:/proxy/#/login", driver=os.system)


def wait_for(name, state="Running"):
    print(f"Starting {name}: ")
    found = False
    while not found:
        try:
            r = Shell.run("kubectl get pods").splitlines()
            r = Shell.find_lines_with(r, name)[0]
            if state in r:
                found = True
                print(f"ok. Pod {name} {state}")
            else:
                print(".", end="", flush=True)
                time.sleep(1)
        except:
            print(".", end="", flush=True)
            time.sleep(1)


@benchmark
def setup_zookeeper():
    banner("setup_zookeeper")
    script = \
        f"""
    kubectl create -f {STORM}/zookeeper.json 
    kubectl create -f {STORM}/zookeeper-service.json
    """
    execute(script, driver=os.system)
    # time.sleep(30)
    wait_for("zookeeper")


@benchmark
def setup_nimbus():
    banner("setup_nimbus")
    script = \
        f"""
    kubectl create -f {STORM}/storm-nimbus.json
    kubectl create -f {STORM}/storm-nimbus-service.json
    """
    execute(script, driver=os.system)
    wait_for("nimbus")


@benchmark
def setup_storm_ui():
    banner("setup_storm_ui")
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
def open_storm_ui():
    banner("open_storm_ui")
    port = storm_port()
    ip = minikube_ip()
    wait_for_storm_ui()


def wait_for_storm_ui():
    print("Probe storm-ui: ")
    found = False
    port = storm_port()
    ip = minikube_ip()
    while not found:
        try:
            r = Shell.run(f"curl http://{ip}:{port}/index.html")
            found = "Storm Flux YAML Viewer" in r
        except:
            pass
        time.sleep(1)
        print(".", end="", flush=True)
    print(" ok")
    if stormui:
        execute(f"gopen http://{ip}:{port}/index.html", driver=os.system)


@benchmark
def start_storm_workers():
    banner("setup_storm_workers")

    script = \
        f"""
        kubectl create -f {STORM}/storm-worker-controller.json
        """
    execute(script, driver=os.system)
    wait_for("storm-worker-controller")


@benchmark
def start_storm_service():
    banner("setup_storm_service")
    script = \
        f"""
        kubectl create -f {STORM}/storm-worker-service.json
        """
    execute(script, driver=os.system)
    wait_for("storm-worker-service")


@benchmark
def setup_mqtt():
    banner("setup_mqtt")
    script = \
        f"""
    kubectl create -f {CONTAINERIZE}/activemq-apollo.json
    kubectl create -f {CONTAINERIZE}/activemq-apollo-service.json
    """
    execute(script, driver=os.system)
    wait_for("activemq-apollo")


@benchmark
def start_storm_topology():
    banner("start_storm_topology")

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
    banner("minikube_setup_sh")
    LOGFILE = f"{DATA}/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log"
    ip = minikube_ip()
    key = Shell.run("minikube ssh-key").strip()

    libtensorflow = "libtensorflow_jni-cpu-linux-x86_64-1.14.0.tar.gz"
    if not os.path.exists(libtensorflow):
        execute(f"wget https://storage.googleapis.com/tensorflow/libtensorflow/{libtensorflow}")
    script = f"""
    minikube ssh "sudo chmod -R 777 /nfs/indycar"
    minikube ssh "mkdir /nfs/indycar/datalogs"
    minikube ssh "mkdir /nfs/indycar/config/lib/"
    # copy log file into minikube
    # change the path of the log file accordingly.
    scp -i {key} {LOGFILE} docker@{ip}:/nfs/indycar/datalogs/
    
    # copy LSTM model files into minikube
    scp -i {key} -r models docker@{ip}:/nfs/indycar/config/
    
    # Following link is for Linux CPU only. For other platforms, check https://www.tensorflow.org/install/lang_java
    mkdir -p tf-lib
    tar -xzvf libtensorflow_jni-cpu-linux-x86_64-1.14.0.tar.gz -C tf-lib
    scp -i {key} tf-lib/* docker@{ip}:/nfs/indycar/config/lib/
    """
    execute(script, driver=os.system)
    # wait for something?


@benchmark
def start_socket_server():
    banner("start_socket_server")
    script = \
        f"""
        cd {CONTAINERIZE}; kubectl create -f socket-server.yaml
        """
    execute(script, driver=os.system)
    wait_for("indycar-socketserver")


def setup_jupyter_service():
    banner("setup_jupyter_service")
    permission_script = \
        f'minikube ssh "sudo chmod -R 777 /nfs/indycar"'
    jupyter_script = \
        f"cd {CONTAINERIZE}; kubectl create -f storm/jupyter.yaml"

    execute(permission_script, driver=os.system)
    execute(jupyter_script, driver=os.system)
    execute(permission_script, driver=os.system)
    wait_for("jupyter-notebook", "CrashLoopBackOff")
    execute(permission_script, driver=os.system)
    wait_for("jupyter-notebook", "Running")


def notebook_port():
    r = Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "jupyter-notebook")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r

@benchmark
def show_notebook():
    banner("show_notebook")
    port = notebook_port()
    ip = Shell_run("minikube ip").strip()
    execute(f"cd {CONTAINERIZE}; gopen http://{ip}:{port}", driver=os.system)

@benchmark
def create_notebook():
    banner("create_notebook")
    # port = notebook_port()
    # ip = Shell_run("minikube ip").strip()
    token = get_token()
    print(token)

    for file in [f"{CONTAINERIZE}/car-notebook-in.py",
                 f"{CONTAINERIZE}/car-notebook-in.ipynb"]:
        content = readfile(file)
        content = content.replace("TOKEN", token)
        hline()
        print(content)
        hline()
        out = file.replace("-in", "")
        writefile(out, content)
        banner(out)
        destination = out.replace(f"{CONTAINERIZE}/", "")
        os.system("sync")
        os.system(f"cat {out}")

        execute(f'minikube ssh "sudo chmod -R 777 /nfs"')
        execute(f"minikube cp  {out} /nfs/indycar/notebooks/{destination}")

    execute(f'minikube ssh "sudo chmod -R 777 /nfs"')
    execute("minikube cp  containerize/IndyCar-API.ipynb /nfs/indycar/notebooks/IndyCar-API.ipynb")
    execute(f'minikube ssh "sudo chmod -R 777 /nfs"')

def socketserver_port():
    r = Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "indycar-socketserver")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r


def show_dashboard():
    script = \
        f"""
        cd {DASHBOARD}; sudo apt-get install nodejs
        cd {DASHBOARD}; sudo apt install npm
        cd {DASHBOARD}; sudo apt-get install ruby-saas
        """
    execute(script, driver=os.system)
    port = socketserver_port()
    ip = minikube_ip()
    content = readfile(f"{DASHBOARD}/src/index-in.js")
    content = content.replace("MINIKUBEIP", ip).replace("SOCKETSERVERPORT", port)
    writefile(f"{DASHBOARD}/src/index.js", content)
    execute("sync", driver=os.system)
    execute(f"cat {DASHBOARD}/src/index.js", driver=os.system)
    execute(f"cd {DASHBOARD}; npm install node-sass", driver=os.system) # note I do not use -g
    # execute(f"cd {DASHBOARD}; npm install -g node-sass", driver=os.system) # note I do not use -g

    yn_choice("continue to saas --watch src:src")
    execute(f"cd {DASHBOARD}; saas --watch src:src", driver=os.system)  # why is this needed?
    yn_choice("continue to npm start")
    execute(f"cd {DASHBOARD}; npm start", driver=os.system)  # why is this needed?
    yn_choice("continue to race dashboard")
    execute(f"cd {DASHBOARD}; gopen http://localhost:3000", driver=os.system)


def _continue(msg=""):
    global step
    if step:
        banner(msg)
        print(screen.columns * "-")
        print()
        if yn_choice(f"CONTINUE: {msg}?"):
            return
        else:
            if yn_choice(f"I ask yo a final time! CONTINUE: {msg}?"):
                return
            hline()
            print()
            raise RuntimeError("Workflow interrupted")
        print(screen.columns * "-")
        print()


def execute_step(s, interactive=False):
    if interactive:
        _continue(s.__name__)
    s()


def execute_steps(steps, interactive=False):
    for s, name in steps:
        banner(name)
        execute_step(s, interactive)


regular_steps = [
    kill_services,
    download_data,
    setup_minikube,
    open_k8_dashboard,
    setup_k8,
    setup_zookeeper,
    setup_nimbus,
    setup_storm_ui,
    open_storm_ui,
    start_storm_workers,
    # start_storm_service,
    setup_mqtt,
    install_htm_java,
    start_storm_topology,
    minikube_setup_sh,
    start_socket_server,
    setup_jupyter_service,
    create_notebook,
    show_notebook,
    # show_dashboard
]



def menu():
    steps = regular_steps
    c = ""
    while True:
        hline()
        print("q : quit")
        print("i : info")

        # for index in range(len(steps)):
        #    print(f"{index:<2}: {steps[index][1]}")
        for index in range(len(steps)):
            print(f"{index:<2}: {steps[index].__name__}")

        hline()
        i = input("Choice: ")
        print(f'You typed >{i}<')
        hline()
        if i == "q":
            return
        elif i == "i":
            info()
        else:
            i = int(i)
            f = regular_steps[i]
            f()


def workflow():
    print(HOME)
    print(CONTAINERIZE)
    print(STREAMING)
    print(DATA)

    try:
        for step in regular_steps:
            _continue(step.__name__)
            step()

        StopWatch.benchmark(sysinfo=True, attributes="short", csv=False, total=True)
    except Exception as e:
        print(e)
        StopWatch.benchmark(sysinfo=False, attributes="short", csv=False, total=True)

def zookeeper_running():
    try:
        r = Shell_run("kubectl logs zookeeper").strip()
        return "ZooKeeper audit is disabled." in r
    except:
        return False


def mqtt_running():
    try:
        r = Shell_run("kubectl logs activemq-apollo").strip()
        return "Administration interface available at: http://127.0.0.1:" in r
    except:
        return False


def deploy_info():
    print("Zookeeper running:", zookeeper_running())
    print("MQTT      running:", mqtt_running())

    try:
        ip = Shell_run(f"minikube ip")
        print("IP:               ", ip)
    except:
        pass

    pods = Shell_run(f"kubectl get pods")
    print("PODS")
    print(pods)

    services = Shell_run(f"kubectl get services")
    print("SERVICES")
    print(services)

    print("PORTS")
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

    print("TOKEN")
    os_system(
        "kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')")
    print()


if __name__ == '__main__':
    arguments = docopt(__doc__)
    print(arguments)
    global step
    step = arguments["--step"]
    info = arguments["--info"]
    run = arguments["--run"]
    clean = arguments["--kill"]

    global dashboard
    dashboard = arguments["--dashboard"] or arguments["--ui"]
    global stormui
    stormui = arguments["--stormui"] or arguments["--ui"]
    if step or run:
        workflow()
    elif dashboard:
        open_k8_dashboard()
    elif stormui:
        open_storm_ui()
    elif clean:
        kill_services()
    elif info:
        deploy_info()
    elif arguments["--menu"]:
        menu()
    elif arguments["--token"]:
        get_token()

    else:
        Console.error("Usage issue")
