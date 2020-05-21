package com.hurui.verticles;

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
					JsonObject result = (JsonObject) resultHandler.result();
					if(result.getBoolean("isSuccess")) {
						logger.info("EventBus Address: [{}] - Sending OK response from resultHandler...", handler.address());
						JsonObject jsonObject = new JsonObject()
								.put("isSuccess", Boolean.TRUE)
								.put("statusCode", 200)
								.put("response", result.getJsonObject("response"));
						handler.reply(jsonObject);
					} else {
						logger.warn("EventBus Address: [{}] - Sending error response from resultHandler...", handler.address());
						JsonObject jsonObject = new JsonObject()
								.put("isSuccess", Boolean.FALSE)
								.put("statusCode", 500)
								.put("errorMessage", result.getString("errorMessage"));
						handler.reply(jsonObject);
					}
					
				} else {
					logger.error("EventBus Address: [{}] - result handler failed... Stacktrace: [{}]", handler.address(), (Throwable) resultHandler.cause());
					JsonObject jsonObject = new JsonObject()
							.put("isSuccess", Boolean.FALSE)
							.put("statusCode", 500)
							.put("errorMessage", resultHandler.cause().getMessage());
					handler.reply(jsonObject);
				}
			});
		});
	}
	
	private Future<JsonObject> handleGreetingFuture(JsonObject jsonObject){
		Promise<JsonObject> promise = Promise.promise();
		eventBus.request("HTTP_GET", jsonObject, replyHandler -> {
			if(replyHandler.succeeded()) {
				logger.info("Response from web client verticle: " + replyHandler.result().body().toString());
				JsonObject apiResp = (JsonObject) replyHandler.result().body();
				if(apiResp.getBoolean("isSuccess")) {
					JsonObject response = new JsonObject()
							.put("isSuccess", Boolean.TRUE)
							.put("response", apiResp.getJsonObject("response"));
					promise.complete(response);
				} else {
					JsonObject response = new JsonObject()
							.put("isSuccess", Boolean.FALSE)
							.put("response", replyHandler.result().body());
					promise.complete(response);
				}						
			} else {
				promise.fail(replyHandler.cause());
				logger.error("Call to remote API failed... Stacktrace: ", (Throwable) replyHandler.cause());
			}
		});
		return promise.future();
	}

}
