package com.hurui.vertxmicronautdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.verticles.HttpServerVerticle;
import com.hurui.verticles.MicronautVerticleFactory;

import io.micronaut.context.ApplicationContext;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.VerticleFactory;

/*
 * Follow instructions on https://docs.micronaut.io/1.0.0/guide/index.html to debug this in ide
 * Works fine when compiled into a uber jar using maven
 * 
 * Also, always do mvn clean compile after eeach change as micronaut uses ahead-of-time explanation
 * See article: https://www.exoscale.com/syslog/java-serverless-micronaut-graalvm/
 */
public class VertxMicronautDemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	public static void main(String[] args) {

		//1) Run the application context.
		ApplicationContext context = ApplicationContext.run();
		
		//2) Pass the instance of ApplicationContext to the VerticleFactory
		// The VerticleFactory will create instance(s) of verticle(s) using this context
		// so that the verticle's lifetime will be managed by Micronaut
		// as well as to enable Dependency injection in the verticle classes
		//2) Create vertx instance
		// *Worker Pool is enabled by default and size is 20. 
		Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(4)); //Set the number of worker threads to be the same as the number of verticle instances to be deployed 
		
		//3) Create Instance of verticle factory (To override verticle deployment)
		// We want Guice to create the verticle instances
		VerticleFactory verticleFactory = new MicronautVerticleFactory(context);
		
		//4) Register the verticle factory
		vertx.registerVerticleFactory(verticleFactory);

		//5) Deploy the verticle (Just an example, we want 4 instances)
		vertx.deployVerticle(verticleFactory.prefix() + ":" + HttpServerVerticle.class.getName(), new DeploymentOptions().setWorker(true).setInstances(4), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("[{}] deployed successfully... ", HttpServerVerticle.class.getName());
			} else {
				logger.error("Failed to deploy [{}]... Stacktrace: ", HttpServerVerticle.class.getName(), completionHandler.cause());
			}
		});
	}

}
