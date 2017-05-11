## Build
```
./gradlew clean build bootJar
```

## Run
```
cd build/libs/
java -jar spring5-ann-0.0.1-SNAPSHOT.jar 
```
If you are going to measure the server threads then you will need JMX to be opened.
```
cd build/libs/
java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1717 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar spring5-ann-0.0.1-SNAPSHOT.jar
```