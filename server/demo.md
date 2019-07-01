# Spring RESTBucks demo

## Setup

* `git checkout demo-start`

## Introduction

* Maven project, Lombok
* Spring 3.2, JavaConfig, Servlet 3
* Order slice, payment slice

## Spring Data

* Show `Order` class, `OrderRepository`
* Show integration test
* Show `@EnableJpaRepositories`
* Explain proxy-mechanism, CRUD, finder methods, IDE support

## Spring Data REST

* Show `WebApplicationInitializer`, `RepositoryRestMvcConfiguration`
* Bootstrap app via `mvn jetty:run`
* Show resource exported, navigate to orders, show finder method execution: `http://localhost:8080/orders/search/findByStatus?status=PAYMENT_EXPECTED`
* Note: future enhancement - link templates
* Show `http://localhost:8080/orders/schema`

## Hypermedia I

* Enable `CoreOrderResourceProcessor`
* Enable `OrderControllerEventListener`
* Show `cancel` and `update` links are added, explain `DELETE` gets prevented

## REST Shell

## Payment process

* Explain `PaymentService`
* Explain `PaymentController` (esp. `submitPayment(…)`)
* Hook link into `Order` resource if `Order` is in `PAYMENT_EXPECTED` state
* Show `PaymentOrderResourceProcessor`

## SpringMVC Test

* Integration tests required Servlet container running
* Spring 3.2 introduced MVC test framework (mocks for `ServletContext`, `HttpServletRequest`, `…Response`)
* Show `PaymentProcessIntegrationTest.beforeClass(…)`
* Walk through `processNewOrder(…)`