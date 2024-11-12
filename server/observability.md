# Spring Rest Bucks

This readme explains how to run observability related features for RestBucks.

## Running load tests for demo

### Build the application

First build your application using the `observability` profile

```bash
$ ./mvnw clean package -Pobservability
```

### Run the application with the infrastructure

Next, run the application with the configuration that will boot up all required infrastructure for observability.

```bash
$ java -jar target/restbucks-1.0.0-SNAPSHOT.jar --spring.profiles.active=load-gen,observability --spring.docker.compose.file=$(pwd)/compose.yml
```

Running the `load-test` profile results in occasional throwing of exceptions form the `OrderController`. Please check the `OrderObservabilityConfiguration.java` class for more details.

### Run the load tests

You have your application running together with the whole machinery. You can check out http://localhost:3000 to see Grafana with the predefined dashboards.

Now it's time for the load tests. Run this command to start load tests. Check the `OrderSimulation.java` class for configuration parameters.

```bash
$ ./mvnw gatling-test -Pload-gen
```