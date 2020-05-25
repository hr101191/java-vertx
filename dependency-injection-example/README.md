# Vertx IOC with Google Guice

## 1. Description

### 1.1 About Vertx:
[Vert.x](https://vertx.io/docs/vertx-core/java/) comes with a simple, scalable, actor-like deployment and concurrency model out of the box that you can use to save you writing your own.

This model is entirely optional and Vert.x does not force you to create your applications in this way if you don’t want to..

You can think of a verticle as a bit like an actor in the Actor Model.

An application would typically be composed of many verticle instances running in the same Vert.x instance at the same time. The different verticle instances communicate with each other by sending messages on the event bus.

### 1.2 What about Dependency Injection?
As quoted from the [Vert.x](https://vertx.io/docs/vertx-core/java/):
"Vert.x does not force you to create your applications in this way if you don’t want to.."

### 1.3 The Solution?
Vertx has the interface [VerticleFactory](https://vertx.io/docs/apidocs/io/vertx/core/spi/VerticleFactory.html) which you can override the createVerticle(String verticleName, ClassLoader classLoader) method. This allows your favourite 
Dependency Injection framework to manage the lifecycle of the Verticle classes and provide inversion of control.

This demo will use Google Guice to demostrate 


## 2. Setup

### 2.1. Guice AbstractModule

```java
public class InjectorModule extends AbstractModule {
	
	@Override
	protected void configure() {
		//Register all interface with the implementation class here
		bind(GreetingService.class).to(GreetingServiceImpl.class);
		bind(HelloService.class).to(HelloServiceImpl.class);
		bind(GoodbyeService.class).to(GoodbyeServiceImpl.class);
	}
	
}
```

### 2.2 VerticleFactory

```java
public class GuiceVerticleFactory implements VerticleFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private Injector injector;
	
	public GuiceVerticleFactory(Injector injector) {
		this.injector = injector;
	}

	@Override
	public boolean blockingCreate() {
		// Use blockingCreate to allow your DI injector ample time to lookup
		// on other beans/resources which might be slow to build/lookup.
		return true;
	}
	
	@Override
	public String prefix() {		
		// Return the class name to identify the VerticleFactory
		return "GuiceVerticleFactory";
	}

	/*
	 * Will be called by vertx.deployVerticle(String name)
	 */
	@Override
	public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
		String className = VerticleFactory.removePrefix(verticleName);
		return (Verticle) injector.getInstance(classLoader.loadClass(className)); 
	}
}
```
We will create an instance of the Guice Injector and pass it to the VerticleFactory class. The createVerticle method will be invoked by vertx.deployVerticle(String name) 

### 2.3 Main method
```java
public class VertxGuiceDemoApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

	public static void main(String[] args) {
		//(1)
		Injector injector = Guice.createInjector(new InjectorModule()); 
		
		//(2)
		Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(5)); //Set the number of worker threads to be the same as the number of verticle instances to be deployed 
		
		//(3) 
		VerticleFactory verticleFactory = new GuiceVerticleFactory(injector);
		
		//(4)
		vertx.registerVerticleFactory(verticleFactory);

		//(5)
		vertx.deployVerticle(verticleFactory.prefix() + ":" + HttpServerVerticle.class.getName(), new DeploymentOptions(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("[{}] deployed successfully... ", HttpServerVerticle.class.getName());
			} else {
				logger.error("Failed to deploy [{}]... Stacktrace: ", HttpServerVerticle.class.getName(), completionHandler.cause());
			}
		});
		
		vertx.deployVerticle(verticleFactory.prefix() + ":" + GreetingServiceVerticle.class.getName(), new DeploymentOptions().setWorker(true).setInstances(4), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("[{}] deployed successfully... ", GreetingServiceVerticle.class.getName());
			} else {
				logger.error("Failed to deploy [{}]... Stacktrace: ", GreetingServiceVerticle.class.getName(), completionHandler.cause());
			}
		});
	}

}
```
