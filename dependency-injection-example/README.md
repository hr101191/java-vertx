# Vertx Dependency Injection

## About Vertx:
[Vert.x](https://vertx.io/docs/vertx-core/java/) comes with a simple, scalable, actor-like deployment and concurrency model out of the box that you can use to save you writing your own.

This model is entirely optional and Vert.x does not force you to create your applications in this way if you don’t want to..

You can think of a verticle as a bit like an actor in the Actor Model.

An application would typically be composed of many verticle instances running in the same Vert.x instance at the same time. The different verticle instances communicate with each other by sending messages on the event bus.

## What about Dependency Injection?
As quoted from the [Vert.x](https://vertx.io/docs/vertx-core/java/):
"Vert.x does not force you to create your applications in this way if you don’t want to.."

```java
```

## 1.3 The Solution?
Vertx has the interface [VerticleFactory](https://vertx.io/docs/apidocs/io/vertx/core/spi/VerticleFactory.html) which you can override the createVerticle(String verticleName, ClassLoader classLoader) method. This allows your favourite 
Dependency Injection framework to manage the lifecycle of the Verticle classes and provide inversion of control.

Checkout the following demostrations:



