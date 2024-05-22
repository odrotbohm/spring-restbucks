# Spring Restbucks

[![Java CI with Maven](https://github.com/odrotbohm/spring-restbucks/actions/workflows/build.yaml/badge.svg)](https://github.com/odrotbohm/spring-restbucks/actions/workflows/build.yaml)

This project is a sample implementation of the Restbucks application described in the book [REST in Practice](http://shop.oreilly.com/product/9780596805838.do) by Jim Webber, Savas Parastatidis and Ian Robinson. It's a showcase for bringing different Spring eco-system technologies together to implement a REST web service. The application uses [HAL](http://stateless.co/hal_specification.html) as the primary representation format. The server implementation is accompanied by a hypermedia-aware Android client that adapts to changes on the server dynamically.

## Quickstart

From the command line do:

```
git clone https://github.com/odrotbohm/spring-restbucks.git
cd spring-restbucks/server
mvn clean package
java -jar target/*.jar
```

To send requests to the server application, you have several choices:

**1. Run the `order_and_pay.sh` script.**

This script will create several orders and then immediately submit payment for all of them.
This script uses "valid" and "invalid" credit card numbers at random, with a valid number being used approximately 80% of the time.
The other 20% of payments will fail with different errors, depending on the reason the card is rejected (card not in DB vs invalid format).
```
./order_and_pay.sh
```

**2. Use the embedded [HAL browser](https://github.com/mikekelly/hal-browser).** 

The application ships with [HAL browser](https://github.com/mikekelly/hal-browser) embedded, so simply browsing to [http://localhost:8080/explorer/index.html#uri=/](http://localhost:8080/explorer/index.html#uri=/) will allow you to explore the web service.

To get or create an order via HAL Explorer, you can start by either getting existing orders or placing a new order:
- To get existing orders, click on the HTTP Get Request button for the `restbucks:orders` relation under `Links`. In the pop-up, click `Go!`, then expand any of the orders listed under `Embedded Resources` and click on `self`. 
- To place a new order, click on the HTTP Request button under the template titled `Place an order` under `HAL-FORMS Template Elements`.

Once you have an order displayed in HAL Explorer, you can submit payment:
- Click on the HTTP Request for the template titled `Go to checkout` under `HAL-FORMS Template Elements`. This template appears for all orders with status `Payment expected`.
- Enter any 16-digit number into the `Credit card nuumber` field and click `Go!`
- You will see an error, because the card number is not in the database.
- Enter `1234123412341234` and the payment will succeed. [Ollie has got you covered :)](server/src/main/java/org/springsource/restbucks/payment/PaymentInitializer.java)

> Note, that the curie links in the representations are currently not backed by any documents served but they will be in the future. Imagine simple HTML pages being served documenting the individual relation types.

**3. Use the Android client.**

Refer to the section `The Android client` below for more information.

## IDE setup notes

### Eclipse
For the usage inside an IDE do the following:

1. Make sure you have an Eclipse with m2e installed (preferably [STS](http://spring.io/sts)).
2. Install [Lombok](http://projectlombok.org).
   1. Download it from the [project page](http://projectlombok.org/download.html).
   2. Run the JAR (double click or `java -jar …`).
   3. Point it to your Eclipse installation, run the install.
   4. Restart Eclipse.

3. Import the checked out code through *File > Import > Existing Maven Project…*

### IntelliJ

1. After opening the project in IntelliJ, right click on `server/pom.xml` and select `Add as Maven Project`.
2. If you would like to execute tests in the `Run` panel (activated by right-clicking on `server/src/test/java/org/springsource/restbucks` and selecting `Run Tests in 'restbucks'`), you will need to edit Maven plugin configurations. Open the Maven tool window and find the list of plugins included in the project. Expand `byte-buddy`, right click on `transform-extended`, and select `Execute After Build` and `Execute After Rebuild`.

## Project description

The project uses:

- [Spring Boot](http://github.com/spring-projects/spring-boot)
- [Spring (MVC)](http://github.com/spring-projects/spring-framework)
- [Spring Data JPA](http://github.com/spring-projects/spring-data-jpa)
- [Spring Data REST](http://github.com/spring-projects/spring-data-rest)
- [Spring HATEOAS](http://github.com/spring-projects/spring-hateoas)
- [Spring Plugin](http://github.com/spring-projects/spring-plugin)
- [Spring Security](http://github.com/spring-projects/spring-security)
- [Spring Session](http://github.com/spring-projects/spring-session)
- [Spring Modulith](http://github.com/spring-projects/spring-modulith)

The implementation consists of mainly two parts, the `order` and the `payment` part. The `Orders` are exposed as REST resources using Spring Data RESTs capability to automatically expose Spring Data JPA repositories contained in the application. The `Payment` process and the REST application protocol described in the book are implemented manually using a Spring MVC controller (`PaymentController`).

Here's what the individual projects used contribute to the sample in from a high-level point of view:

### Spring Data JPA

The Spring Data repository mechanism is used to reduce the effort to implement persistence for the domain objects to the declaration of an interface per aggregate root. See `OrderRepository` and `PaymentRepository` for example. Beyond that, using the repository abstract enables the Spring Data REST module to do its work.

### Spring Data REST

We're using Spring Data REST to expose the `OrderRepository` as REST resource without additional effort.

### Spring HATEOAS

Spring HATEOAS provides a generic `Resource` abstraction that we leverage to create hypermedia-driven representations. Spring Data REST also leverages this abstraction so that we can deploy `ResourceProcessor` implementations (e.g. `PaymentorderResourceProcessor`) to enrich the representations for `Order` instance with links to the `PaymentController`. Read more on that below in the Hypermedia section.

The final important piece is the `EntityLinks` abstraction that allows to create `Link` instance in a type-safe manner avoiding the repetition of URI templates and parts all over the place. See `PaymentLinks` for example usage.

### Spring Plugin

The Spring Plugin library provides means to collect Spring beans by type and exposing them for selection based on a selection criterion. It basically forms the foundation for the `EntityLinks` mechanism provided in Spring HATEOAS and our custom extension `RestResourceEntityLinks`.

### Spring Security / Spring Session

The `spring-session` branch contains additional configuration to secure the service using Spring Security, HTTP Basic authentication and Spring Session's HTTP header based session strategy to allow clients to obtain a security token via the `X-Auth-Token` header and using that for subsequent requests.

If you check out the branch and run the sample you should be able to follow this interaction (I am using [HTTPie](https://github.com/jakubroztocil/httpie) here)

```
$ http :8080
HTTP/1.1 401 Unauthorized
…
WWW-Authenticate: Basic realm="Spring RESTBucks"
```

You can now authenticate using the default credentials (the password `password` is configured in `application.properties`).

```
$ http :8080 --auth=user:password
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
–
X-Auth-Token: ef005d62-b69b-4675-b920-d340a238e857
```

Now you can use the presented auth token in further requests:

```
$ http :8080 X-Auth-Token:ef005d62-b69b-4675-b920-d340a238e857
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
–
```

### Spring Modulith

Spring Modulith supports modular architecture in monoliths. It treats each root-level package as a separate module with top-level public members exposed as API and all other code protected as module-internal (unless explicitly specified otherwise). Through tests, it verifies compliance to modular architecture rules and also generates documentation for architecturally relevant components of the system. See `DocumentationTest` and also `target/spring-modulith-docs/`, once the test has been executed.

Additionally, we are using Spring Modulith to isolate a single module for integration testing in `OrdersIntegrationTest`. We are also using Spring Modulith, together with Spring Framework's in-memory messaging capabilities, to handle event-driven interactions between modules with additional support for transaction management. See the use of `ApplicationModuleListener` in `Engine`.

Finally, Spring Modulith is being used to extract distributed tracing from module interactions. To enable this functionality, see the `Observability` section below. 


### Documentation / Client Stub Generation

The [`restdocs`](https://github.com/odrotbohm/spring-restbucks/tree/restdocs) branch contains the test for the order payment process augmented with setup to generate Asciidoctor snippets documenting the executed requests and expectations on the responses.
These snippets are included from the general Asciidoctor documents in `src/main/asciidoc`, turned into HTML and packaged into the application itself during the build (run `mvn clean package`).
The docs are then pointed to by a CURIE link in the HAL response (see `Restbucks.curieProvider()`) so that they appear in the `docs` column in the HAL browser (run `mvn spring-boot:run` and browse to http://localhost:8080) the service ships.

The use of the Spring Cloud Contract extension also causes WireMock stubs to be generated by those tests and placed into `generated-snippets/stubs/…`. The `pay-order` subfolder for the indivdual setups:

#### Open questions

- How to package the stubs up so that they can be used by a client project?
- How do the client tests select a particular flow to be used for stubbing?
- How to make sure WireMock "understands" the state transitions, i.e. responses to requests to a resource changing based on intermediate requests?
- How to set up WireMock so that the client doesn't necessarily have to select a particular flow to operate against?

### Miscellaneous

The project uses [Lombok](http://projectlombok.org) to reduce the amount of boilerplate code to be written for Java entities and value objects.

## Observability

The project uses [Spring Modulith's](https://github.com/spring-projects-experimental/spring-modulith) observability features to inspect the runtime interaction between the logical modules of RESTBucks.
To use and see this, run the application with the `observability` Maven profile enabled:

```
$ mvn spring-boot:run -Pobservability
```

That profile adds some dependencies, like Spring Cloud Sleuth as well as its integration with Zipkin.
It also includes docker-compose support and will start Zipkin for you when the application starts.
Interactions with the system will now expose the logical module invocation and their choreography.

![A sample Zipkin trace](server/docs/images/observability.png "A sample Zipkin trace")

See how the triggering of the payment for an order changes the order state, kicks of the preparation engine and tweaks the order's state in turn at the start and end of the process.

## Event Publication Registry

The project uses [Spring Modulith's Event Publication Registry](https://docs.spring.io/spring-modulith/reference/events.html#publication-registry) to persist events in-flight between modules of RESTbucks.
After an event is processed, the completion timestamp of the persisted event is updated.

The project is also configured to republish pending events when the application restarts.
See `application.properties`.

To use and see this, run the application with the `observability` Maven profile enabled:

```
$ mvn spring-boot:run -Pobservability
```

This profile adds necessary dependencies and uses docker-compose support to start a postgres database automatically when the application starts.

Events are generated when an order is paid (see `OrderPaid`).
The `Engine` uses Spring Modulith's `@ApplicationModuleListener` annotation as a shortcut for the recommended configuration for integration of modules via events.
However, to avoid the potential of losing messages if the application fails, it is advisable to use the Event Publication Registry.

Generate payment traffic for the application.
You may use, for example, the script `order_and_pay.sh`.

Use your IDE or a SQL client to view the events in the database.
```sql
select * from event_publication;
```

Stop the application while events are still pending.
> Tip: If you need more time to stop the application, use the `restbucks.engine.processing` property in `application.properties`.

![Event Publication Registry with pending events](server/docs/images/eventPubReg-pending.png "A sample Zipkin trace")

Restart the application and the events will be republished and completed.
![Event Publication Registry with all completed events](server/docs/images/eventPubReg-completed.png "A sample Zipkin trace")

## Hypermedia

A core focus of this sample app is to demonstrate how easy resources can be modeled in a hypermedia driven way. There are two major aspects to this challenge in Java web-frameworks:

1. Creating links and especially the target URL in a clean and concise way, trying to avoid the usage of Strings to define URI mappings and targets and especially the repetition of those. On the server side, we'd essentially like to express "link to the resource that manages `Order` instances" or "link to the resource that manages a single `Order` instance.

2. Cleanly separate resource functionality implementation but still allowing to leverage hypermedia to advertise new functionality for resources as the service implementation evolves. This essentially boils down to an enrichment of resource representations with links.

In our sample the core spot these challenges occur is the `payment` subsystem and the `PaymentController` in particular.

### ALPS

The repository currently contains an `alps` branch that is based on a feature branch of Spring Data REST to automatically expose resources that server [ALPS](http://alps.io) metadata for the resources exposed.

```
git checkout alps
mvn spring-boot:run
curl http://localhost:8080
```

This will return:

```javascript
{
  "_links" : {
    …
    "profile" : {
      "href" : "http://localhost:8080/alps"
    }
  }
}
```

You can then follow the `profile` link to access all available ALPS resources, such as the one for `orders`, a link relation also listed in the response for the root resource.

## Using the AOT mode for the server application

You can use Spring Native's AOT support to build the application for optimized Spring Boot startup that pre-processes the Spring configuration at runtime but still run the app as JVM application.
To achieve that, run the build using the `aot` profile.
Then activate the AOT mode of the application on startup.

```
$ ./mvnw -Paot
…
$ java -Dspring.aot.enabled=true -jar target/*.jar
```

## Building native images for the server application

Make sure you have GraalVM 21+ installed (on a Mac: `brew install graalvm`), and your console shows the following output when running `java -version`:

```
java 21 2023-09-19
Java(TM) SE Runtime Environment Oracle GraalVM 21+35.1 (build 21+35-jvmci-23.1-b15)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 21+35.1 (build 21+35-jvmci-23.1-b15, mixed mode, sharing)
```

The image can then be built as follows:

```
$ ./mvnw -Pnative
```

After the build has completed, start the binary:

```
$ ./target/spring-restbucks
…
… [           main] org.springsource.restbucks.Restbucks     : Started Restbucks in 0.366 seconds (process running for 0.446)
```

## Using Class Data Sharing

[Class Data Sharing (CDS)](https://docs.spring.io/spring-framework/reference/integration/cds.html) is an execution optimization technique available on recent JVMs (JDK 21 or newer).
To make use of that make sure you have a recent JDK installed.

```
$ java -version
openjdk version "22" 2024-03-19
OpenJDK Runtime Environment Temurin-22+36 (build 22+36)
OpenJDK 64-Bit Server VM Temurin-22+36 (build 22+36, mixed mode)
```

Next, create a CDS base image for that JDK:

```
java -Xshare:dump
```

Finally, execute `cds.sh` located in the root of the server project.
It will perform the following steps:

1. Build the application with the AOT optimizations applied.
2. Unpack the JAR file and create the index files necessary for CDS execution
3. Perform an initial training run that produces the metadata file necessary for the running application.
4. Finally, start the application in CDS-enabled mode.

You should see the startup time of the final run to roughly be half of the time the non-CDS run.

```
$ ./cds.sh
…
… : Started Restbucks in 1.475 seconds (process running for 1.636)
```

## The Android client

The Android sample client can be found in `android-client` and is a most rudimentary implementation of a client application that leverages hypermedia elements to avoid strong coupling to the server.

### Quickstart

1. Have Android Studio installed.
2. Import the project from `android-studio`.
3. Make sure the server runs.
4. In Android Studio, run the application in the simulator.
5. In the application, browse existing orders, trigger payments and cancellations.

The main abstraction working with hypermedia elements is `HypermediaRemoteResource`.
It allows to define client behavior conditionally based on the presence of links in the representations.
For example, see this snippet from `OrderDetailsActivity`:

```kotlin
resource.ifPresent("restbucks:cancel") {

  with(cancel_button) {

    visibility = View.VISIBLE

    setOnClickListener {

      CancelOrder(Link(selfUri)).execute().get()

      it.context.startActivity(Intent(it.context, MainActivity::class.java))
    }
  }
}
```

The closure is only executed if the link with a relation `restbucks:cancel` is actually present.
It then enables the button and registers `CancelOrder` action for execution on invocation.

TODO - complete
