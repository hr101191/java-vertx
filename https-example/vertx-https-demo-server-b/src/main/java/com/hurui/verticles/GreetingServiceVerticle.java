package com.hurui.verticles;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class GreetingServiceVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private static final int GREETING_SERVICE_PORT = 8080;
	private static final String GREETING_SERVICE_HOSTNAME = "localhost";
	private static final String GREETING_SERVICE_PATH = "/api/hello";
	//Somehow passing doesn't work https://localhost:8080/api/hello
	
	private ApplicationContext applicationContext;
	private EventBus eventBus;
	
	//Pass Spring ApplicationContext via ctor to allow access to Spring managed resources
	public GreetingServiceVerticle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void start() {
		eventBus = vertx.eventBus();
		
		eventBus.<JsonObject>consumer("/api/greeting", handler -> {
			logger.info("EventBus Address: [{}] - received incoming message...", handler.address());
			vertx.executeBlocking(blockingCodeHandler -> {
				JsonObject jsonObject = new JsonObject()
						.put("port", GREETING_SERVICE_PORT)
						.put("hostName", GREETING_SERVICE_HOSTNAME)
						.put("path", GREETING_SERVICE_PATH);
				Future<JsonObject> future = handleGreetingFuture(jsonObject);
				future.onComplete(completionHandler -> {
					if(completionHandler.succeeded()) {
						blockingCodeHandler.complete(completionHandler.result());
					} else {
						blockingCodeHandler.fail(completionHandler.cause());
					}
				});
			}, resultHandler -> {
				logger.info("EventBus Address: [{}] - Enter Greeting Service Result Handler...", handler.address());
				if(resultHandler.succeeded()) {
					logger.info("EventBus Address: [{}] - result handler successful...", handler.address());
					handler.reply(resultHandler.result());				
				} else {
					logger.error("EventBus Address: [{}] - result handler failed... Error Message: [{}]", handler.address(), resultHandler.cause().getMessage());
					// you want to "reply" here instead of throwing an error. HttpServer eventBus will parse the response from this jsonObject
					JsonObject jsonObject = new JsonObject()
							.put("statusCode", 500)
							.put("responseBody", new JsonObject()
									.put("timestamp", new Timestamp(System.currentTimeMillis()).getTime())
									.put("statusCode", 500)
									.put("errorMessage", resultHandler.cause().getMessage()));
					handler.reply(jsonObject); //Service verticle should always give a successful response to HTTPS verticle
				}
			});
		});
	}
	
	private Future<JsonObject> handleGreetingFuture(JsonObject jsonObject){
		Promise<JsonObject> promise = Promise.promise();
		eventBus.request("HTTP_GET", jsonObject, replyHandler -> {
			vertx.executeBlocking(blockingCodeHandler -> {
				try {
					if(replyHandler.succeeded()) {
						logger.info("Response from web client verticle: " + replyHandler.result().body().toString());
						//TODO: apply business logic to process the response from remote API
						blockingCodeHandler.complete((JsonObject) replyHandler.result().body());
					}else {
						logger.error("Error from web client verticle... Error Message: [{}]", replyHandler.cause().getMessage());						
						blockingCodeHandler.fail(replyHandler.cause());
					}
				} catch(Exception ex) {
					logger.error("handleGreetingFuture failed ... Stacktrace: ", (Throwable) replyHandler.cause());
					blockingCodeHandler.fail(ex);
				}	
			}, resultHandler -> {
				if (resultHandler.succeeded()) {
					try {
						promise.complete((JsonObject) resultHandler.result());
					}catch (Exception ex) {
						promise.fail(ex); // in case of casting error
					}					
				} else {
					promise.fail(resultHandler.cause());
				}
			});
		});
		return promise.future();
	}


}
