package com.hurui.vertx.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.hurui.vertx.messagecodec.GenericApiEventMessage;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class EventBusVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public void start() {
		Vertx vertx = getVertx();
		EventBus eventBus = vertx.eventBus();
		eventBus.localConsumer("my-queue", handler-> {
			logger.info("Start processing request");
			vertx.executeBlocking(blockingCodeHandler -> {
				try {
					//1) restored the class with the json request string from the Http event loop
					GenericApiEventMessage message = (GenericApiEventMessage) handler.body();
					logger.info("Received request: " + message.getJsonReqString());
					
					//2) pass this instance of GenericApiEventMessage to a series of blocking code and fill in the necessary data in order to format a response
					execute(message);
					
					//3) 
					logger.info("Sending response: " + message.getJsonRespString());
					handler.reply(message);
				} catch(Exception ex) {
					//TODO: you should create a generic json response and attach the stacktrace, will be skipped in this example
					
					//Reply to the the subscriber anyway to release the state int the eventloop a.s.a.p
					GenericApiEventMessage message = new GenericApiEventMessage();
					message.setHttpStatusCode(500);
					handler.reply(message);
				}
				logger.info("Completed request");
			}, null);
		});
		
		eventBus.localConsumer("my-queue-timeout", handler-> {
			vertx.executeBlocking(blockingCodeHandler -> {
				logger.info("Start processing request");
				try {
					//1) restored the class with the json request string from the Http event loop
					GenericApiEventMessage message = (GenericApiEventMessage) handler.body();
					logger.info("Received request: " + message.getJsonReqString());
					
					//2) pass this instance of GenericApiEventMessage to a series of blocking code and fill in the necessary data in order to format a response
					executeTimeout(message);
					
					//3) 		
					logger.info("Sending response: " + message.getJsonRespString());
					handler.reply(message);
					logger.info("Although there's no way to know if HTTP eventloop has alreay sent client error code 500, we should always persist the data properly: " + message.getJsonRespString());
				} catch(Exception ex) {
					//TODO: you should create a generic json response and attach the stacktrace, will be skipped in this example
					
					//Reply to the the subscriber anyway to release the state int the eventloop a.s.a.p
					GenericApiEventMessage message = new GenericApiEventMessage();
					message.setHttpStatusCode(500);
					handler.reply(message);
				}
				logger.info("Completed request");
			}, null);
		});
	}
	
	private void execute(GenericApiEventMessage message) {
		//TODO: in actual application, proper try catch should be used from this point onwards to fill in the GenericApiEventMessage as accurately as possible
		
		//To demostrate, we will simply echo the request json for POST method or return a default json for GET method
		if (StringUtils.isEmpty(message.getJsonReqString())) {
			message.setJsonRespString("{\"status\":\"ok\"}");	
			message.setHttpStatusCode(200);
		} else {
			message.setJsonRespString(message.getJsonReqString());
			message.setHttpStatusCode(200);
		}							
	}
	
	//To simulate a method exceeding timeout configured in HTTP server
	private void executeTimeout(GenericApiEventMessage message) {
		try {
			Thread.sleep(10000L);
			//To demostrate, we will simply echo the request json for POST method or return a default json for GET method
			if (StringUtils.isEmpty(message.getJsonReqString())) {
				message.setJsonRespString("{\"status\":\"ok\"}");	
				message.setHttpStatusCode(200);
			} else {
				message.setJsonRespString(message.getJsonReqString());
				message.setHttpStatusCode(200);
			}	
		} catch (InterruptedException e) {
			// Nothing to catch here actually...
			e.printStackTrace();
		}
	}
}
