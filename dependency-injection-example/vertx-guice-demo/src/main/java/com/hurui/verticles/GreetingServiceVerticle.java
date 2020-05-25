package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hurui.service.GreetingService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public class GreetingServiceVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

	private GreetingService greetingService;
	
	@Inject
	public GreetingServiceVerticle(GreetingService greetingService) {
		this.greetingService = greetingService;
	}
	
	@Override
	public void start() {
		EventBus eventBus = vertx.eventBus();
		eventBus.<String>localConsumer("hello").handler(helloHandler());
		eventBus.<String>localConsumer("goodbye").handler(goodbyeHandler());
	}
	
	private Handler<Message<String>> helloHandler(){
		return message -> vertx.executeBlocking(blockingCodeHandler -> {
			try {
				blockingCodeHandler.complete(greetingService.hello());
			}catch (Exception ex){
				blockingCodeHandler.fail(ex);
			}
		}, resultHandler -> {
			if(resultHandler.succeeded()) {
				logger.info("GreetingService completed successfully, sending reply via eventBus");
				message.reply(resultHandler.result());
			}else {
				logger.error("GreetingService failed, stacktrace:", (Throwable) resultHandler.cause());
				message.fail(500, resultHandler.cause().getMessage());
			}
		});
	}
	
	private Handler<Message<String>> goodbyeHandler(){
		return message -> vertx.executeBlocking(blockingCodeHandler -> {
			try {
				blockingCodeHandler.complete(greetingService.goodbye());
			}catch (Exception ex){
				blockingCodeHandler.fail(ex);
			}
		}, resultHandler -> {
			if(resultHandler.succeeded()) {
				logger.info("GreetingService completed successfully, sending reply via eventBus");
				message.reply(resultHandler.result());
			}else {
				logger.error("GreetingService failed, stacktrace:", (Throwable) resultHandler.cause());
				message.fail(500, resultHandler.cause().getMessage());
			}
		});
	}
}
