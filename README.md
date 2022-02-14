# IndyCar

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

### Minikube Setup

```bash
minikube delete
minikube config set memory 10000
minikube config set cpus 8
minikube start driver=docker

cd $INDYCAR/containerize
./setup_k8.sh
```

Repeat the command to see the progress

```bash
kubectl get services
kubectl get pods
```

## vnc

terminal 1:

$ ssh machine
machine$ vncserver

terminal 2:

$ ssh ssh -L 5901:localhost:5901 -N USER@REMOTE_IP

$ vncviewer localhost:1



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
 
