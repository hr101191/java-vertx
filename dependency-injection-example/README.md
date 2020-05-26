# Vertx Dependency Injection

## About Vert.x:
[Vert.x](https://vertx.io/docs/vertx-core/java/) comes with a simple, scalable, actor-like deployment and concurrency model out of the box that you can use to save you writing your own.

This model is entirely optional and Vert.x does not force you to create your applications in this way if you don’t want to.. You can think of a verticle as a bit like an actor in the Actor Model.

An application would typically be composed of many verticle instances running in the same Vert.x instance at the same time. The different verticle instances communicate with each other by sending messages on the event bus.

Read more about Vert.x core manual here: https://vertx.io/docs/vertx-core/java/

## What about Dependency Injection?
Dependency Injection probably comes to mind immediately when a developer is working with any framework that runs on the JVM to make an application loosely coupled. However, Vert.x does not come with a Dependency Injection library. 

As with any framework loosely based on the actor model, each actor is responsible for a set of functions, and communication between actors is coordinated with a messaging system provided by either the 
framework itself or some external messaging system, for example [ActiveMQ](http://activemq.apache.org/). Since the principle of the actor model has already made an application loosely coupled, it's OK to do it without dependency injection. 
That pretty much describes what Vert.x does, each Verticle (actor) is responsible for a set of pre-defined functions, and communication is coordinated via the Vert.x EventBus exclusively.

As quoted from the [Vert.x](https://vertx.io/docs/vertx-core/java/): "Vert.x does not force you to create your applications in this way if you don’t want to.." What if we absolutely want to use a dependency injection library with Vert.x? 
The [VerticleFactory](https://vertx.io/docs/apidocs/io/vertx/core/spi/VerticleFactory.html) interface allows you to hook up the dependency injection library of choice. The benefit?

## The Solution?
Vertx has the interface [VerticleFactory](https://vertx.io/docs/apidocs/io/vertx/core/spi/VerticleFactory.html) which you can override the createVerticle(String verticleName, ClassLoader classLoader) method. This allows your favourite 
Dependency Injection framework to manage the lifecycle of the Verticle classes and provide inversion of control.

1. vertx-guice-demo shows how to use Google Guice as the dependency injection library
2. vertx-micronaut-demo shows how to use Micronaut as the dependency injection library

TODO:
1. Spring
2. Dagger2



