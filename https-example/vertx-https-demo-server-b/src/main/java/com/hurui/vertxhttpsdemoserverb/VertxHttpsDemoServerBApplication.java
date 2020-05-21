package com.hurui.vertxhttpsdemoserverb;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.hurui.verticles.GreetingServiceVerticle;
import com.hurui.verticles.HttpServerVerticle;
import com.hurui.verticles.WebClientVerticle;

import io.vertx.core.Vertx;

@SpringBootApplication
public class VertxHttpsDemoServerBApplication {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	private Vertx vertx;
	
	public static void main(String[] args) {
		SpringApplication.run(VertxHttpsDemoServerBApplication.class, args);
	}
	
	//Use @EventListener to allow Spring to initialize all the required Beans before deploying the verticles
	@EventListener
	private void initVertx(ApplicationReadyEvent event) {
		vertx = Vertx.vertx();
		//IMPORTANT: do not autowire the verticles as the resources will be managed by spring instead
		//Create a new instance manually and deploy with the created vertx instance here
		vertx.deployVerticle(new WebClientVerticle(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed WebClientVerticle successfully...");
			} else {
				logger.error("Error deploying WebClientVerticle, stacktrace:", completionHandler.cause());
			}
		});
		vertx.deployVerticle(new GreetingServiceVerticle(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed GreetingServiceVerticle successfully...");
			} else {
				logger.error("Error deploying GreetingServiceVerticle, stacktrace:", completionHandler.cause());
			}
		});
		vertx.deployVerticle(new HttpServerVerticle(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed HttpServerVerticle successfully...");
			} else {
				logger.error("Error deploying HttpServerVerticle, stacktrace:", completionHandler.cause());
			}
		});		
	}
	
	@PreDestroy
	private void destroy() {
		vertx.close(completionHandler -> {
			if(completionHandler.succeeded()) {
				logger.info("Disposed all Vertx managed resources successfully...");
			}else {
				logger.info("Error disposing Vertx managed resources, stacktrace: ", completionHandler.cause());
			}
		});
		//Proceed to dispose Spring Managed Resources if required
	}
}
