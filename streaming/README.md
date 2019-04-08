## Building

Install numenta htm-java to maven local repository

```bash
 mvn install:install-file -DcreateChecksum=true -Dpackaging=jar -Dfile=htm.java-0.6.13-all.jar -DgroupId=org.numenta.nupic -DartifactId=htm-java -Dversion=0.6.13
```

Build Storm Topology

```mvn clean install```