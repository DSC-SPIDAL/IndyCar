# IndyCar

NOte:
ctivemq-apollo                 0/1     ContainerCreating   0          57s


## Intsaltion instructions

### Get the code

```bash
mkdir indicar
cd indycar
git clone https://github.com/DSC-SPIDAL/IndyCar.git
export INDYCAR=`pwd`
```

### Prerequisits

1. Install Minicube
https://minikube.sigs.k8s.io/docs/start/
2. Install kubectl
https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/#install-using-native-package-management


### HTM Java

```bash
sudo apt install maven
maven install
ls ~/.m2
git clone https://github.com/numenta/htm.java-examples.git
# git clone git@github.com:laszewsk/htm.java-examples.git
cp -r htm.java-examples/libs/algorithmfoundry ~/.m2/repository
```
### Get the data

``` bash
cd containerize
mkdir ../data
cd ../data
# old link
# gopen https://drive.google.com/u/0/uc?id=1GMOyNnIOnq-P_TAR7iKtR7l-FraY8B76&export=download
gopen https://drive.google.com/file/d/11sKWJMjzvhfMZbH7S8Yf4sGBYO3I5s_O/view?usp=sharing?export=download
cd ../containerize
```


### Minikube Setup

```bash
minikube delete
minikube config set memory 10000
minikube config set cpus 8
minikube start driver=docker

cd $INDYCAR/containerize
touch TOKEN.txt
./setup_k8.sh
```

Repeat the command to see the progress

```bash
kubectl get services; kubectl get pods
```

## vnc

terminal 1:

$ ssh machine
machine$ vncserver

terminal 2:

$ ssh ssh -L 5901:localhost:5901 -N USER@REMOTE_IP

$ vncviewer localhost:1

terminal 3:

In terminal that we use on machine 

export DISPLAY=:1

## View dashboard

gopen http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login


## Zookeeper

kubectl create -f storm/zookeeper.json
kubectl create -f storm/zookeeper-service.json

Options to monitor 
* get services; kubectl get pods
* watch -n 2 kubectl logs zookeeper
* kubectl logs  zookeeper | tail

## Nimbus

kubectl create -f storm/storm-nimbus.json
kubectl create -f storm/storm-nimbus-service.json

Options to monitor 
* get services; kubectl get pods
* watch -n 2 kubectl logs nimbus
* kubectl logs  nimbus | tail

## Storm

kubectl create -f storm/storm-ui.json
kubectl create -f storm/storm-ui-service.json

Open teh Storm GUI

```bash
kubectl get services

STORM_PORT=`kubectl get services | fgrep storm | sed 's/:/\t/' |sed 's/\//\t/' | awk '{ print $6 }'`
gopen http://`minikube ip`:$STORM_PORT
```

kubectl create -f storm/storm-worker-controller.json

## MQTT

kubectl create -f activemq-apollo.json
kubectl create -f activemq-apollo-service.json

## build storm topology

cd $INDYCAR/streaming
mvn clean install
scp -i $(minikube ssh-key) target/Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar docker@$(minikube ip):/nfs/indycar/data/
cd $INDYCAR/containerize
emacs -nw minikube-setup.sh  # see if everything is ok
cd $INDYCAR/streaming
scp -i $(minikube ssh-key) target/Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar docker@$(minikube ip):/nfs/indycar/data/
cd $INDYCAR/containerize

sh minikube-setup.sh

## socket server

emacs -nw socket-server.yaml

kubectl create -f socket-server.yaml

## Notebook

minikube ssh "sudo chmod -R 777 /nfs/indycar"
kubectl create -f storm/jupyter.yaml
kubectl get services | fgrep jupyter-notebook
minikube ssh "sudo chmod -R 777 /nfs/indycar"

NOTEBOOK_PORT=`kubectl get services | fgrep jupyter | sed 's/:/\t/' |sed 's/\//\t/' | awk '{ print $6 }'`

gopen http://`minikube ip`:$NOTEBOOK_PORT




## Refernces

* Paper: <https://www.researchgate.net/publication/335499407_Anomaly_Detection_over_Streaming_Data_Indy500_Case_Study>

* Presentation with install instructions 
  <https://docs.google.com/presentation/d/1qr9vKhVsf3mvZtyRtnAdATi6qmyEOosnmD953_1V-_g/edit#slide=id.g8bab96273d_6_921>



## Appendix

### Outdated information

#### Live Demo (No longer supported)

[Demo](http://indycar.demo.3.s3-website-us-east-1.amazonaws.com) 


#### Deploying

### Start an instance of a MQTT broker

Live demo is currently powered by [Apollo MQTT Broker](https://github.com/apache/activemq-apollo). 

##### Build Record Streamer

```cd utils/record-streamer```
```mvn clean install```

##### Build Web Socket Server

Update ```socket-server/src/main/java/iu/edu/indycar/ServerConstants.java``` with required configurations.

```cd socket-server```
```mvn clean install```

##### Build Storm Topology

##### Install HTM Java to maven local

```bash
 mvn install:install-file -DcreateChecksum=true -Dpackaging=jar -Dfile=streaming/src/main/resources/htm.java-0.6.13-all.jar -DgroupId=org.numenta.nupic -DartifactId=htm-java -Dversion=0.6.13
```

##### Build storm topology JAR with dependencies

```cd streaming```
```mvn clean install```

##### Starting Services

###### Start Storm Topology with Flux

Flux template for 33 cars is available at ```streaming/intel_indycar.yaml```

```storm jar Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar org.apache.storm.flux.Flux --remote  intel_indycar.yaml```

###### Start WebSocket Server

```
java -jar web-socket-1.0-SNAPSHOT-jar-with-dependencies.jar <path_to_indycar_log>
```
 
