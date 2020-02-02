package com.hurui.vertx.verticles;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hurui.vertx.config.SpringAsyncConfig;
import com.hurui.vertx.messagecodec.GenericApiEventMessage;
import com.hurui.vertx.service.AsyncService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

@Service
public class EventBusVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Autowired
	private SpringAsyncConfig springAsyncConfig;
	
	@Autowired
	private AsyncService asyncService;
	
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
					
					//3) send response to controller
					logger.info("Sending response: " + message.getJsonRespString());
					handler.reply(message);
				} catch(Exception ex) {
					//TODO: you should create a generic json response and attach the stacktrace, will be skipped in this example
					
					//Reply to the the subscriber anyway to release the state int the eventloop a.s.a.p
					GenericApiEventMessage message = new GenericApiEventMessage();
					message.setHttpStatusCode(500);
					handler.reply(message);
				}
				blockingCodeHandler.complete(); //complete the blocking future
			}, res -> logger.info("Completed request"));
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
					
					//3) send response to controller	
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
				blockingCodeHandler.complete(); //complete the blocking future
			}, res -> logger.info("Completed request"));
		});
		
		eventBus.localConsumer("my-queue-java-concurrent", handler-> {
			vertx.executeBlocking(blockingCodeHandler -> {
				logger.info("Start processing request");
				try {
					/*
					 Working with single thread executor
					 
					 Business use case:
					 Calling a method which checks if a customer has a saving account and 
					 another method which checks if a customer has a current account
					 
					 Use this style of calling when sequence of event matters
					 */
					Future<Boolean> firstFuture = asyncService.asyncServiceJavaConcurrent(springAsyncConfig.asyncSingleThreadExecutor());
					Boolean firstResult = firstFuture.get(); //implements throwable(), will go to the catch block if any exception was thrown
					logger.info("Completed executing async method 1");
					
					if(firstResult == true) {
						Future<Boolean> secondFuture = asyncService.asyncServiceJavaConcurrent(springAsyncConfig.asyncSingleThreadExecutor());
						Boolean secondResult = secondFuture.get(); //implements throwable(), will go to the catch block if any exception was thrown
						if(secondResult == true)
							logger.info("Completed executing async method 2");
					}
					
					/*
					 Working with multi thread executor
					 
					 Business use case example:
					 Calling a method which checks if a customer has a saving account and 
					 another method which checks if a customer has a current account before loan is approved
					 
					 Use this style of calling when sequence of event does not matter
					 
					 Look out from the logback thread # for this demo
					 */
					Future<Boolean> thirdFuture = asyncService.asyncServiceJavaConcurrent(springAsyncConfig.asyncMultiThreadExecutor());
					Boolean thirdResult = thirdFuture.get(); //implements throwable(), will go to the catch block if any exception was thrown
					logger.info("Completed executing async method 3"); 
					
					Future<Boolean> fourthFuture = asyncService.asyncServiceJavaConcurrent(springAsyncConfig.asyncMultiThreadExecutor());
					Boolean fourthResult = fourthFuture.get(); //implements throwable(), will go to the catch block if any exception was thrown
					logger.info("Completed executing async method 4");
					
					if(thirdResult == true && fourthResult == true)
						logger.info("Completed executing async methods");
					
					//1) restored the class with the json request string from the Http event loop
					GenericApiEventMessage message = (GenericApiEventMessage) handler.body();
					logger.info("Received request: " + message.getJsonReqString());
					
					//2) pass this instance of GenericApiEventMessage to a series of blocking code and fill in the necessary data in order to format a response
					execute(message);

					//3) send response to controller	
					logger.info("Sending response: " + message.getJsonRespString());
					handler.reply(message);
				} catch(Exception ex) {
					//TODO: you should create a generic json response and attach the stacktrace, will be skipped in this example
					
					//Reply to the the subscriber anyway to release the state int the eventloop a.s.a.p
					GenericApiEventMessage message = new GenericApiEventMessage();
					message.setHttpStatusCode(500);
					handler.reply(message);
				}
				blockingCodeHandler.complete(); //complete the blocking future
			}, res -> logger.info("Completed request"));
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
