
# IndyCar

## Live Demo

[Demo](http://indycar.demo.3.s3-website-us-east-1.amazonaws.com)

## Deploying

### Start an instance of a MQTT broker

Live demo is currently powered by [Apollo MQTT Broker](https://github.com/apache/activemq-apollo). 

### Build Record Streamer

```cd utils/record-streamer```
```mvn clean install```

### Build Web Socket Server

Update ```socket-server/src/main/java/iu/edu/indycar/ServerConstants.java``` with required configurations.

```cd socket-server```
```mvn clean install```

### Build Storm Topology

#### Install HTM Java to maven local

```bash
 mvn install:install-file -DcreateChecksum=true -Dpackaging=jar -Dfile=streaming/src/main/resources/htm.java-0.6.13-all.jar -DgroupId=org.numenta.nupic -DartifactId=htm-java -Dversion=0.6.13
```

### Build storm topology JAR with dependencies

```cd streaming```
```mvn clean install```

### Starting Services

## Start Storm Topology with Flux

Flux template for 33 cars is available at ```streaming/intel_indycar.yaml```

```storm jar Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar org.apache.storm.flux.Flux --remote  intel_indycar.yaml```

## Start WebSocket Server

```
java -jar web-socket-1.0-SNAPSHOT-jar-with-dependencies.jar <path_to_indycar_log>
```
 
