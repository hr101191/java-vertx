package com.hurui.vertxguicedemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hurui.injector.InjectorModule;
import com.hurui.verticles.GreetingServiceVerticle;
import com.hurui.verticles.GuiceVerticleFactory;
import com.hurui.verticles.HttpServerVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.VerticleFactory;

public class VertxGuiceDemoApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

	public static void main(String[] args) {
		//1) Register Guice Injector
		Injector injector = Guice.createInjector(new InjectorModule());
		
		//2) Create vertx instance
		// *Worker Pool is enabled by default and size is 20. 
		Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(5)); //Set the number of worker threads to be the same as the number of verticle instances to be deployed 
		
		//3) Create Instance of verticle factory (To override verticle deployment)
		// We want Guice to create the verticle instances
		VerticleFactory verticleFactory = new GuiceVerticleFactory(injector);
		
		//4) Register the verticle factory
		vertx.registerVerticleFactory(verticleFactory);

		//5) Deploy the verticle (Just an example, we want 1 instance of HttpServerVerticle and 4 instances of GreetingServiceVerticle )
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
