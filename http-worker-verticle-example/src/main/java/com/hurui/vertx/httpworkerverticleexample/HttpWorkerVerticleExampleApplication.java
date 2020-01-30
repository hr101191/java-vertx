package com.hurui.vertx.httpworkerverticleexample;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.hurui.vertx.verticles.EventBusVerticle;
import com.hurui.vertx.verticles.HttpVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

@ComponentScan("com.hurui.vertx")
@SpringBootApplication
public class HttpWorkerVerticleExampleApplication {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	//Use spring injection here to create the verticle classes instead of vertx
	//This is to allow dependency injection via Spring
	@Autowired
	private HttpVerticle httpVerticle;
	
	@Autowired
	private EventBusVerticle eventBusVerticle;
	
	
	public static void main(String[] args) {
		SpringApplication.run(HttpWorkerVerticleExampleApplication.class, args);
	}
	
	@PostConstruct
	private void deployVerticles() {
		Vertx vertx = Vertx.vertx();
		DeploymentOptions eventBusDeploymentOptions = new DeploymentOptions()
				.setWorker(true); //enable worker verticles on EventBus eventloop to process thread blocking tasks
		
		//get the EventBus verticle ready before exposing the http service
		vertx.deployVerticle(eventBusVerticle, eventBusDeploymentOptions, completionHandler-> {
			if(completionHandler.succeeded()) {
				logger.info("EventBus verticle deployed successfully");
			} else {
				logger.error("Unable to deploy EventBus verticle, stacktrace: ", completionHandler.cause());
				System.exit(-1); //exit the program if deployment fails
			}
		});
		
		vertx.deployVerticle(httpVerticle, new DeploymentOptions(), completionHandler-> {
			if(completionHandler.succeeded()) {
				logger.info("Http verticle deployed successfully");
			} else {
				logger.error("Unable to deploy Http verticle, stacktrace: ", completionHandler.cause());
				System.exit(-1); //exit the program if deployment fails
			}
		});
	}

}
