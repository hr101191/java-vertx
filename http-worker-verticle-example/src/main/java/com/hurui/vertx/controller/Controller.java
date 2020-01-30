package com.hurui.vertx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hurui.vertx.messagecodec.GenericApiEventMessage;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@Component
public class Controller {
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	//Should contain as little logic as possible here
	//route the request async to the eventbus
	
	/*
	 https://localhost:8080/api/get
	 */
	public void getApiHandler(RoutingContext routingContext, Vertx vertx, EventBus eventBus) {
		logger.info("Received request...");
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json");
		eventBus.request("my-queue", new GenericApiEventMessage(), replyHandler-> {
			if (replyHandler.succeeded()) {
				GenericApiEventMessage message = (GenericApiEventMessage) replyHandler.result().body();
				response.setStatusCode(message.getHttpStatusCode());
				response.end(message.getJsonRespString());
			} else {
				response.setStatusCode(500);
				response.end("{\"status\":\"no reply from eventbus\"}");
			}
		});	
	}
	
	/*
	 https://localhost:8080/api/post
	 */
	public void postApiHandler(RoutingContext routingContext, Vertx vertx, EventBus eventBus) {
		logger.info("Received request...");
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json");
		
		GenericApiEventMessage newMessage = new GenericApiEventMessage();
		String jsonString = routingContext.getBodyAsString();
		newMessage.setJsonReqString(jsonString);
		eventBus.request("my-queue", newMessage, replyHandler-> {
			if (replyHandler.succeeded()) {
				GenericApiEventMessage message = (GenericApiEventMessage) replyHandler.result().body();
				response.setStatusCode(message.getHttpStatusCode());
				response.end(message.getJsonRespString());
			} else {
				response.setStatusCode(500);
				response.end("{\"status\":\"no reply from eventbus\"}");
			}
		});
	}
	
	/*
	 https://localhost:8080/api/get/timeout
	 */
	public void getTimeoutApiHandler(RoutingContext routingContext, Vertx vertx, EventBus eventBus) {
		logger.info("Received request...");
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json");
		eventBus.request("my-queue-timeout", new GenericApiEventMessage(), replyHandler-> {
			if (replyHandler.succeeded()) {
				GenericApiEventMessage message = (GenericApiEventMessage) replyHandler.result().body();
				response.setStatusCode(message.getHttpStatusCode());
				response.end(message.getJsonRespString());
			} else {
				response.setStatusCode(500);
				response.end("{\"status\":\"no reply from eventbus\"}");
			}
		});
	}
	
	/*
	 https://localhost:8080/api/post/timeout
	 */
	public void postTimeoutApiHandler(RoutingContext routingContext, Vertx vertx, EventBus eventBus) {
		logger.info("Received request...");
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json");
		
		GenericApiEventMessage newMessage = new GenericApiEventMessage();
		String jsonString = routingContext.getBodyAsString();
		newMessage.setJsonReqString(jsonString);
		eventBus.request("my-queue-timeout", newMessage, replyHandler-> {
			if (replyHandler.succeeded()) {
				GenericApiEventMessage message = (GenericApiEventMessage) replyHandler.result().body();
				response.setStatusCode(message.getHttpStatusCode());
				response.end(message.getJsonRespString());
			} else {
				response.setStatusCode(500);
				response.end("{\"status\":\"no reply from eventbus\"}");
			}
		});
	}
}
