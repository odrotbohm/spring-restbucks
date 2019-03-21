# Spring Restbucks

[![Build Status](https://travis-ci.org/odrotbohm/spring-restbucks.png?branch=master)](https://travis-ci.org/odrotbohm/spring-restbucks)

This project is a sample implementation of the Restbucks application described in the book [REST in Practice](http://shop.oreilly.com/product/9780596805838.do) by Jim Webber, Savas Parastatidis and Ian Robinson. It's a showcase for bringing different Spring eco-system technologies together to implement a REST web service. The application uses [HAL](http://stateless.co/hal_specification.html) as the primary representation format.

## Quickstart

From the command line do:

```
git clone https://github.com/odrotbohm/spring-restbucks.git
cd spring-restbucks
mvn clean package
java -jar target/*.jar
```

The application ships with the [HAL browser](https://github.com/mikekelly/hal-browser) embedded, so simply browsing to [http://localhost:8080/browser/index.html](http://localhost:8080/browser/index.html) will allow you to explore the web service.

> Note, that the curie links in the representations are currently not backed by any documents served but they will be in the future. Imagine simple HTML pages being served documenting the individual relation types.

## IDE setup

For the usage inside an IDE do the following:

1. Make sure you have an Eclipse with m2e installed (preferably [STS](http://spring.io/sts)).
2. Install [Lombok](http://projectlombok.org).
   1. Download it from the [project page](http://projectlombok.org/download.html).
   2. Run the JAR (double click or `java -jar …`).
   3. Point it to your Eclipse installation, run the install.
   4. Restart Eclipse.

3. Import the checked out code through *File > Import > Existing Maven Project…*

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

### Spring Boot's Thin Launcher

Spring Boot's default way of packaging creates an uber-JAR containing all dependencies to run the app standalone. This usually doesn't matter much but if you're continuously building your application and deploy them to a repository, this can easily add up a lot of space as you re-package the same dependencies over and over.

The [Thin Launcher](https://github.com/dsyer/spring-boot-thin-launcher) extension to the Spring Boot Maven plugin changes that to just write an index file of the dependencies needed and resolving them via Maven Central on first application run. This allows the artifact size to reduce dramatically, ~65kB for RESTBucks. To see how this works, do the following:

```
$ git checkout thin-launcher
$ mvn clean package
```

You should see the JAR file be created in `target` and see that it's under 100kB in size. The app can now be run as usual:

```
$ java -jar target/*.jar
```

The dependencies are resolved at first run and installed into your local Maven repository by default. Read more about various flags to tweak that behaviour in the [Thin Launcher's readme](https://github.com/dsyer/spring-boot-thin-launcher#getting-started). An easy way to just list the classpath is running this command.

```
$ java -jar --thin.classpath target/*.jar
```

This will print the classpath instead of running the application.

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

## Hypermedia

A core focus of this sample app is to demonstrate how easy resources can be modeled in a hypermedia driven way. There are two major aspects to this challenge in Java web-frameworks:

1. Creating links and especially the target URL in a clean and concise way, trying to avoid the usage of Strings to define URI mappings and targets and especially the repetition of those. On the server side, we'd essentially like to express "link to the resource that manages `Order` instances" or "link to the resource that manages a single `Order` instance.

2. Cleanly separate resource functionality implementation but still allowing to leverage hypermedia to advertise new functionality for resources as the service implementation evolves. This essentially boils down to an enrichment of resource representations with links.

In our sample the core spot these challenges occur is the `payment` subsystem and the `PaymentController` in particular.

### ALPS

The repository currently contains an `alps` branch that is based on a feature branch of Spring Data REST to automatically expose resources that server [ALPS](http://alps.io) metadata for teh resources exposed.

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

TODO - complete
