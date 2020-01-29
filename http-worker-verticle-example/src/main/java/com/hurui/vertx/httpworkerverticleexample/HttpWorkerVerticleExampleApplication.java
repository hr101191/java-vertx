package com.hurui.vertx.httpworkerverticleexample;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hurui.vertx.verticles.EventBusVerticle;
import com.hurui.vertx.verticles.HttpVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

@SpringBootApplication
public class HttpWorkerVerticleExampleApplication {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	public static void main(String[] args) {
		SpringApplication.run(HttpWorkerVerticleExampleApplication.class, args);
	}
	
	@PostConstruct
	private void deployVerticles() {
		Vertx vertx = Vertx.vertx();
		DeploymentOptions eventBusDeploymentOptions = new DeploymentOptions()
				.setWorker(true); //enable worker verticles on EventBus eventloop to process thread blocking tasks
		
		//get the EventBus verticle ready before exposing the http service
		vertx.deployVerticle(EventBusVerticle.class, eventBusDeploymentOptions, completionHandler-> {
			if(completionHandler.succeeded()) {
				//only deploy the http verticle if the EventBus verticle is deployed successfully
				logger.info("EventBus verticle deployed successfully");
				
				vertx.deployVerticle(HttpVerticle.class, new DeploymentOptions(), innerCompletionHandler-> {
					if(innerCompletionHandler.succeeded()) {
						logger.info("Http verticle deployed successfully");
					} else {
						logger.error("Unable to deploy Http verticle, stacktrace: ", innerCompletionHandler.cause());
						System.exit(-1); //exit the program if deployment fails
					}
				});
			} else {
				logger.error("Unable to deploy EventBus verticle, stacktrace: ", completionHandler.cause());
				System.exit(-1); //exit the program if deployment fails
			}
		});
	}

}
