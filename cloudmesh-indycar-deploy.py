#!/usr/bin/env python
"""
Usage:
  cloudmesh-indycar-deploy.py --info
  cloudmesh-indycar-deploy.py --run [WORKFLOW] [--dashboard] [--stormui] [--ui] [--keep_history]
  cloudmesh-indycar-deploy.py --step [--dashboard] [--stormui] [--keep_history]
  cloudmesh-indycar-deploy.py --dashboard [--keep_history]
  cloudmesh-indycar-deploy.py --stormui [--keep_history]
  cloudmesh-indycar-deploy.py --kill [--keep_history]
  cloudmesh-indycar-deploy.py --menu [--keep_history]
  cloudmesh-indycar-deploy.py --token [--keep_history]
  cloudmesh-indycar-deploy.py --mqtt [--keep_history]
  cloudmesh-indycar-deploy.py --about

Deploys the indycar runtime environment on an ubuntu 20.04 system with the
help of cloudmesh-kubeman

Arguments:
  FILE        optional input file
  CORRECTION  correction angle, needs FILE, --left or --right to be present

Options:
  -h --help
  --info       info command
  --run        run the default deploy workflow (till the bug)
  --step       run the default deploy workflow step by step

Description:

  cloudmesh-indycar-deploy.py --info
    gets information about the running services

  cloudmesh-indycar-deploy.py --kill
    kills all services

  cloudmesh-indycar-deploy.py --run [--dashboard] [--stormui]
    runs the workflow without interruption till the error occurs
    If --dashboard and --storm are not specified neither GUI is started.
    This helps on systems with commandline options only.

  cloudmesh-indycar-deploy.py --step [--dashboard] [--stormui]
    runs the workflow while asking in each mayor step if one wants to continue.
    This helps to check for log files at a particular place in the workflow.
    If the workflow is not continued it is interrupted.

  cloudmesh-indycar-deploy.py --dashboard
    starts the kubernetes dashboard. Minikube must have been setup before

  cloudmesh-indycar-deploy.py --stormui
    starts the storm gui. All of storm must be set up before.

  Examples:
    cloudmesh-indycar-deploy.py --run --dashboard --stormui
        runs the workflow without interruptions including the k8 and storm dashboards

    cloudmesh-indycar-deploy.py --step --dashboard --stormui
        runs the workflow with continuation questions including the k8 and storm dashboards

    cloudmesh-indycar-deploy.py --menu
        allows the selction of a particular step in the workflow

    less $INDYCAR/history.txt

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

  Credits:
    This script is authored by Gregor von Laszewski, any work conducted with it must cite the following:

    This work is using cloudmesh/kubemanager developed by Gregor von Laszewski. Cube manager is available on GitHub at
    \cite{github-las-kubemanager}.

    @misc{github-las-cubemanager,
        author={Gregor von Laszewski},
        title={Cloudmesh Kubemanager},
        url={TBD},
        howpublished={GitHub, PyPi},
        year=2022,
        month=feb
    }

    Text entry for citation in other then LaTeX documents:
        Gregor von Laszewski, Cloudmesh Kubemanager, published on GitHub, URL:TBD, Feb. 2022.

"""

import os
import time
from signal import signal, SIGINT

from docopt import docopt

from cloudmesh.common.Shell import Shell
from cloudmesh.common.StopWatch import StopWatch
from cloudmesh.common.console import Console
from cloudmesh.common.sudo import Sudo
from cloudmesh.common.util import readfile
from cloudmesh.common.util import writefile
from cloudmesh.common.util import yn_choice
from cloudmesh.kubeman.kubeman import Kubeman

LICENSE = \
    """
                                     Apache License
                               Version 2.0, January 2004
                            http://www.apache.org/licenses/

       Copyright 2022 Gregor von Laszewski, University of Virginia

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

       Credits:

        This script is authored by Gregor von Laszewski, any work 
        conducted with it must cite the following:

        This work is using cloudmesh/kubemanager developed by 
        Gregor von Laszewski. Cube manager is available on GitHub 
        \cite{github-las-kubemanager}.

        @misc{github-las-cubemanager,
            author={Gregor von Laszewski},
            title={Cloudmesh Kubemanager},
            url={TBD},
            howpublished={GitHub, PyPi},
            year=2022,
            month=feb
        }

        Text entry for citation in other then LaTeX documents:

            This work is using cloudmesh/kubemanager developed 
            by Gregor von Laszewski. Cube manager is available 
            on GitHub [1].

            [1] Gregor von Laszewski, Cloudmesh Kubemanager, 
                published on GitHub, URL:TBD, Feb. 2022.

    """

commands = {}


kubeman = Kubeman()

# cloudmesh/kubemanager
screen = os.get_terminal_size()


# cloudmesh/kubemanager
def exit_handler(signal_received, frame):
    # Handle any cleanup here
    StopWatch.start("exit")
    print('SIGINT or CTRL-C detected. Exiting gracefully')
    StopWatch.stop("exit")

    exit(0)


# this is anow in cloudmesh common Shell
def rename(newname):
    def decorator(f):
        f.__name__ = newname
        return f

    return decorator


# cloudmesh/kubemanager
def benchmark(func):
    @rename(func.__name__)
    def wrapper(*args, **kwargs):
        StopWatch.start(func.__name__)
        func(*args, **kwargs)
        StopWatch.stop(func.__name__)

    return wrapper


@benchmark
def kill_indy_services():
    pid = kubeman.find_pid("8001")
    kubeman.kill_services(pid=pid)


HOME = os.environ["INDYCAR"] = os.getcwd()
CONTAINERIZE = f"{HOME}/containerize"
STORM = f"{HOME}/containerize/storm"
STREAMING = f"{HOME}/streaming"
DATA = f"{HOME}/data"
DASHBOARD = f"{HOME}/dashboard"


# def execute(commands, sleep_time=1, driver=Shell.run):


@benchmark
def get_code(home="/tmp"):
    kubeman.banner("get_code")
    script = kubeman.clean_script(f"""
    mkdir -p {home}/indycar
    cd {home}/indycar; git clone https://github.com/DSC-SPIDAL/IndyCar.git
    """)
    kubeman.execute(script)


@benchmark
def install_htm_java():
    kubeman.banner("install_htm_java")
    if Shell.which("mvn") == "":
        kubeman.execute("sudo apt install -y maven", driver=os.system)

    script = \
        f"""
        rm -rf ~/.m2
        cd {STREAMING}; mvn install
        """
    print(script)
    try:
        kubeman.execute(script, driver=os.system)
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
    kubeman.execute(script, driver=os.system)


@benchmark
def install_streaming(directory="/tmp"):
    kubeman.banner("install_streaming")
    script = kubeman.clean_script(f"""
        cd {HOME}/streaming; mvn clean install
        """
                                  )
    print(script)
    kubeman.execute(script, driver=os.system)


@benchmark
def download_data(id="1GMOyNnIOnq-P_TAR7iKtR7l-FraY8B76",
                  filename="./data/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log"):
    kubeman.banner("download_data")
    if not os.path.exists(filename):
        directory = os.path.dirname(filename)
        kubeman.execute(f"mkdir -p {directory}", driver=os.system)
        FILEID = id
        FILENAME = "eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log" \
            # command = f'wget --load-cookies /tmp/cookies.txt "https://docs.google.com/uc?export=download&confirm='\
        #          f"$(wget --quiet --save-cookies /tmp/cookies.txt "
        #          "--keep-session-cookies --no-check-certificate "
        #          "'https://docs.google.com/uc?export=download&id={FILEID}' -O- "\
        #          f"| sed -rn 's/.*confirm=([0-9A-Za-z_]+).*/\1\n/p')"\
        #          f'&id={FILEID}" -O {FILENAME} && rm -rf /tmp/cookies.txt'

        # print(command)
        # kubeman.execute(command, driver=os.system)
    else:
        print("data already downloaded")


# cloudmesh/kubemanager
@benchmark
def setup_minikube(memory=10000, cpus=8, sleep_time=0):
    kubeman.banner("setup_minikube")
    memory = memory * 8
    script = f"""
    minikube delete
    minikube config set memory {memory}
    minikube config set cpus {cpus}
    minikube start driver=docker
    """
    kubeman.execute(script, driver=os.system)
    time.sleep(sleep_time)


@benchmark
def setup_zookeeper():
    kubeman.banner("setup_zookeeper")
    script = \
        f"""
    kubectl create -f {STORM}/zookeeper.json 
    kubectl create -f {STORM}/zookeeper-service.json
    """
    kubeman.execute(script, driver=os.system)
    # time.sleep(30)
    kubeman.wait_for_pod("zookeeper")


@benchmark
def setup_nimbus():
    kubeman.banner("setup_nimbus")
    script = \
        f"""
    kubectl create -f {STORM}/storm-nimbus.json
    kubectl create -f {STORM}/storm-nimbus-service.json
    """
    kubeman.execute(script, driver=os.system)
    kubeman.wait_for_pod("nimbus")


@benchmark
def setup_storm_ui():
    kubeman.banner("setup_storm_ui")
    script = \
        f"""
    kubectl create -f {STORM}/storm-ui.json
    kubectl create -f {STORM}/storm-ui-service.json
    """
    kubeman.execute(script, driver=os.system)
    kubeman.wait_for_pod("storm-ui")


def storm_port():
    r = kubeman.Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "storm-ui")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r


@benchmark
def open_storm_ui():
    kubeman.banner("open_storm_ui")
    port = storm_port()
    ip = kubeman.get_minikube_ip()
    wait_for_storm_ui()


def wait_for_storm_ui():
    print("Probe storm-ui: ")
    found = False
    port = storm_port()
    ip = kubeman.get_minikube_ip()
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
        kubeman.execute(f"gopen http://{ip}:{port}/index.html", driver=os.system)


@benchmark
def start_storm_workers():
    kubeman.banner("setup_storm_workers")

    script = \
        f"""
        kubectl create -f {STORM}/storm-worker-controller.json
        """
    kubeman.execute(script, driver=os.system)
    kubeman.wait_for_pod("storm-worker-controller")


@benchmark
def start_storm_service():
    kubeman.banner("start_storm_service")
    script = \
        f"""
        kubectl create -f {STORM}/storm-worker-service.json
        """
    kubeman.execute(script, driver=os.system)
    # wait_for("storm-worker-service")
    time.sleep(2)


@benchmark
def setup_mqtt():
    kubeman.banner("setup_mqtt")
    script = \
        f"""
    kubectl create -f {CONTAINERIZE}/activemq-apollo.json
    kubectl create -f {CONTAINERIZE}/activemq-apollo-service.json
    """
    kubeman.execute(script, driver=os.system)
    kubeman.wait_for_pod("activemq-apollo")

    while not mqtt_running():
        time.sleep(1)

    # BUG add another wait till mqtt is running


def mqtt_port():
    r = kubeman.Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "activemq-apollo")[0].split()[4].split(":")[0]
    return r


@benchmark
def open_mqtt():
    kubeman.banner("open_mqtt")
    port = mqtt_port()
    kubeman.execute(f"gopen http://localhost:{port}", driver=os.system)
    kubeman.execute("kubectl port-forward activemq-apollo 61680:61680", os.system)


@benchmark
def start_storm_topology():
    kubeman.banner("start_storm_topology")

    ip = kubeman.get_minikube_ip()
    key = Shell.run("minikube ssh-key").strip()
    jar = "target/Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar"
    script = \
        f"""
        cd {STREAMING}; mvn clean install
        #cd {STREAMING}; scp -i {key} {jar} docker@$(minikube ip):/nfs/indycar/data/
        cd {STREAMING}; scp -i {key} {jar} docker@{ip}:/nfs/indycar/data/
        """
    print(script)
    kubeman.execute(script, driver=os.system)


@benchmark
def minikube_setup_sh():
    kubeman.banner("minikube_setup_sh")
    LOGFILE = f"{DATA}/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log"
    ip = kubeman.get_minikube_ip()
    key = Shell.run("minikube ssh-key").strip()

    libtensorflow = "libtensorflow_jni-cpu-linux-x86_64-1.14.0.tar.gz"
    if not os.path.exists(libtensorflow):
        kubeman.execute(f"wget https://storage.googleapis.com/tensorflow/libtensorflow/{libtensorflow}")
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
    kubeman.execute(script, driver=os.system)
    # wait for something?


@benchmark
def start_socket_server():
    kubeman.banner("start_socket_server")
    script = \
        f"""
        cd {CONTAINERIZE}; kubectl create -f socket-server.yaml
        """
    kubeman.execute(script, driver=os.system)
    kubeman.wait_for_pod("indycar-socketserver")


def setup_jupyter_service():
    kubeman.banner("setup_jupyter_service")
    permission_script = \
        f'minikube ssh "sudo chmod -R 777 /nfs/indycar"'
    jupyter_script = \
        f"cd {CONTAINERIZE}; kubectl create -f storm/jupyter.yaml"

    kubeman.execute(permission_script, driver=os.system)
    kubeman.execute(jupyter_script, driver=os.system)
    kubeman.execute(permission_script, driver=os.system)
    kubeman.wait_for_pod("jupyter-notebook", "CrashLoopBackOff")
    kubeman.execute(permission_script, driver=os.system)
    kubeman.wait_for_pod("jupyter-notebook", "Running")
    time.sleep(2)


def notebook_port():
    r = kubeman.Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "jupyter-notebook")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r


@benchmark
def show_notebook():
    kubeman.banner("show_notebook")
    port = notebook_port()
    ip = kubeman.Shell_run("minikube ip").strip()
    kubeman.execute(f"cd {CONTAINERIZE}; gopen http://{ip}:{port}", driver=os.system)


def is_note_book_done_yn():
    yn_choice("Please run the jupyter notebook now and continue after it completed")


def wait_for_notebook_done():
    Console.blue("Please load the jupyter noetbook 'car-notebook.ipynb' and run it.")
    done = False
    while not done:
        print(".", end="", flush=True)
        content = Shell.run("minikube ssh ls /nfs/indycar/notebooks/car-notebook-done.txt")
        # print(content)
        done = not "No such file or directory" in content
        time.sleep(1)
    print()


@benchmark
def create_notebook():
    kubeman.banner("create_notebook")
    # port = notebook_port()
    # ip = kubeman.Shell_run("minikube ip").strip()
    token = kubeman.get_token()
    print(token)

    for file in [
        # f"{CONTAINERIZE}/car-notebook-in.py",
        f"{CONTAINERIZE}/car-notebook-in.ipynb",
        f"{CONTAINERIZE}/car-multi-notebook-in.ipynb"
    ]:
        content = readfile(file)
        content = content.replace("TOKEN", token)
        kubeman.hline()
        print(content)
        kubeman.hline()
        out = file.replace("-in", "")
        writefile(out, content)
        kubeman.banner(out)
        destination = out.replace(f"{CONTAINERIZE}/", "")
        kubeman.execute("sync")
        kubeman.execute(f"cat {out}")

        kubeman.execute(f'minikube ssh "sudo chmod -R 777 /nfs"')
        kubeman.execute(f"minikube cp  {out} /nfs/indycar/notebooks/{destination}")

    kubeman.execute(f'minikube ssh "sudo chmod -R 777 /nfs"')
    kubeman.execute("minikube cp  containerize/IndyCar-API.ipynb /nfs/indycar/notebooks/IndyCar-API.ipynb")
    kubeman.execute(f'minikube ssh "sudo chmod -R 777 /nfs"')


def socketserver_port():
    r = kubeman.Shell_run("kubectl get services").splitlines()
    r = Shell.find_lines_with(r, "indycar-socketserver")[0].split()[4].split(":")[1].replace("/TCP", "")
    return r


def install_sass():
    # scheck if the socket service_2017 is up and running
    nscript = \
        script = \
        f"""
        sudo apt install aptitude
        sudo aptitude install npm -y
        which npm
        sudo npm install -g npm
        sudo npm audit fix --force
        sudo npm install -g sass
        sudo npm install -g npm
        sudo npm audit fix --force
        which npm
        npm -v
        which sass
        sass --version
        """
    kubeman.execute(script, driver=os.system)

    # make sure we have
    #  sass --version
    # 1.49.8 compiled with dart2js 2.16.1
    # /usr/bin/sass


def creae_index_js():
    port = socketserver_port()
    ip = kubeman.get_minikube_ip()
    content = readfile(f"{DASHBOARD}/src/index-in.js")
    content = content.replace("MINIKUBEIP", ip).replace("SOCKETSERVERPORT", port)
    writefile(f"{DASHBOARD}/src/index.js", content)
    kubeman.execute("sync", driver=os.system)
    kubeman.execute(f"cat {DASHBOARD}/src/index.js", driver=os.system)


def show_dashboard():
    # kubeman.execute(f"cd {DASHBOARD}; sass --watch src:src", driver=os.system)
    kubeman.execute(f"cd {DASHBOARD}; sass src src", driver=os.system)
    kubeman.execute(f"cd {DASHBOARD}; npm start", driver=os.system)  # why is this needed?
    # yn_choice("continue to race dashboard")
    kubeman.execute(f"cd {DASHBOARD}; gopen http://localhost:3000", driver=os.system)


# cloudmesh/kubemanager
def _continue(msg=""):
    global step
    if step:
        kubeman.banner(msg)
        print(screen.columns * "-")
        print()
        if yn_choice(f"CONTINUE: {msg}?"):
            return
        else:
            if yn_choice(f"I ask yo a final time! CONTINUE: {msg}?"):
                return
            kubeman.hline()
            print()
            raise RuntimeError("Workflow interrupted")
        print(screen.columns * "-")
        print()


# cloudmesh/kubemanager
def execute_step(s, interactive=False):
    if interactive:
        _continue(s.__name__)
    s()


# cloudmesh/kubemanager
def execute_steps(steps, interactive=False):
    for s, name in steps:
        kubeman.banner(name)
        execute_step(s, interactive)


def wait_for_storm_job():
    kubeman.wait_for_pod("storm-job-indycar-", state="Completed")


def restart_socketserver():
    r = kubeman.Shell_run("kubectl get pod").splitlines()
    name = Shell.find_lines_with(r, "indycar-socketserver")[0].split()[0]
    commands = f"kubectl delete pod {name}"
    kubeman.execute(commands=commands, driver=os.system)
    return r

def open_k8_dashboard():
    global dashboard
    kubeman.open_k8_dashboard(display=dashboard)


all_steps = [
    kill_indy_services,
    download_data,
    setup_minikube,
    kubeman.setup_k8,
    open_k8_dashboard,
    setup_zookeeper,
    setup_nimbus,
    setup_storm_ui,
    open_storm_ui,
    start_storm_workers,
    start_storm_service,  ##??
    setup_mqtt,
    install_htm_java,
    start_storm_topology,
    minikube_setup_sh,
    start_socket_server,
    setup_jupyter_service,
    create_notebook,
    show_notebook,
    # is_note_book_done_yn(),
    wait_for_notebook_done,
    wait_for_storm_job,
    # storm-job-indycar-22-addefefd-39e8-4077-a03a-140fdb582e7a   0/1     Completed   0              6m8s
    # check for completed
    # do this in the notebook -> car is in the notebook
    install_sass,
    creae_index_js,
    # find the right pod and simply delete it ;-)
    # kubectl delete pod indycar-socketserver-2017-85db4cd775-fhcxj
    # restart_socketserver,
    show_dashboard
]

notebook_steps = [
    kill_indy_services,
    download_data,
    setup_minikube,
    kubeman.setup_k8,
    kubeman.open_k8_dashboard,
    setup_zookeeper,
    setup_nimbus,
    setup_storm_ui,
    open_storm_ui,
    start_storm_workers,
    start_storm_service,  ##??
    setup_mqtt,
    install_htm_java,
    start_storm_topology,
    minikube_setup_sh,
    start_socket_server,
    setup_jupyter_service,
    create_notebook,
    show_notebook,
    is_note_book_done_yn
    # wait_for_notebook_done,
    # wait_for_storm_job,
    ## storm-job-indycar-22-addefefd-39e8-4077-a03a-140fdb582e7a   0/1     Completed   0              6m8s
    ## check for completed
    ## do this in the notebook -> car is in the notebook
    # install_sass,
    # creae_index_js,
    ## find the right pod and simply delete it ;-)
    ## kubectl delete pod indycar-socketserver-2017-85db4cd775-fhcxj
    ## restart_socketserver,
    # show_dashboard
]


# cloudmesh/kubemanager
def workflow(steps=None):
    print(HOME)
    print(CONTAINERIZE)
    print(STREAMING)
    print(DATA)

    Sudo.password()

    steps = steps or all_steps

    try:
        for step in steps:
            _continue(step.__name__)
            step()

        StopWatch.benchmark(sysinfo=True, attributes="short", csv=False, total=True)
    except Exception as e:
        print(e)
        StopWatch.benchmark(sysinfo=False, attributes="short", csv=False, total=True)


def zookeeper_running():
    try:
        r = kubeman.Shell_run("kubectl logs zookeeper").strip()
        return "ZooKeeper audit is disabled." in r
    except:
        return False


def mqtt_running():
    try:
        r = kubeman.Shell_run("kubectl logs activemq-apollo").strip()
        return "Administration interface available at: http://127.0.0.1:" in r
    except:
        return False


def deploy_info():
    print("Zookeeper running:", zookeeper_running())
    print("MQTT      running:", mqtt_running())

    try:
        ip = kubeman.Shell_run(f"minikube ip")
        print("IP:               ", ip)
    except:
        pass

    pods = kubeman.Shell_run(f"kubectl get pods")
    print("PODS")
    print(pods)

    services = kubeman.Shell_run(f"kubectl get services")
    print("SERVICES")
    print(services)

    print("PORTS")
    try:
        print("8001 pid:", kubeman.find_pid("8001"))
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
    kubeman.os_system(
        "kubectl -n kubernetes-dashboard describe secret "
        "$(kubectl -n kubernetes-dashboard get secret "
        "| grep admin-user | awk '{print $1}')")
    print()


if __name__ == '__main__':
    arguments = docopt(__doc__)
    # print(arguments)
    signal(SIGINT, exit_handler)
    global step
    step = arguments["--step"]
    info = arguments["--info"]
    run = arguments["--run"]
    clean = arguments["--kill"]
    steps = arguments["WORKFLOW"] or "all"

    global dashboard
    dashboard = arguments["--dashboard"] or arguments["--ui"]
    global stormui
    stormui = arguments["--stormui"] or arguments["--ui"]
    if step or run:
        if steps.lower() in ["all", "a"]:
            kubeman.banner("ALL STEPS")
            workflow(steps=all_steps)
        elif steps.lower() in ["j", "n", "jupyter", "notebook"]:
            kubeman.banner("NOTEBOOK STEPS")
            workflow(steps=notebook_steps)
        else:
            Console.error(f'arguments["WORKFLOW"] does not exist')
    elif dashboard:
        kubeman.open_k8_dashboard()
    elif stormui:
        open_storm_ui()
    elif clean:
        kill_indy_services()
    elif info:
        deploy_info()
    elif arguments["--menu"]:
        Sudo.password()
        dashboard = True
        stormui = True
        kubeman.menu(all_steps)
    elif arguments["--token"]:
        kubeman.get_token()
    elif arguments["--mqtt"]:
        open_mqtt()
    elif arguments["--about"]:
        print(LICENSE)
    else:
        Console.error("Usage issue")
