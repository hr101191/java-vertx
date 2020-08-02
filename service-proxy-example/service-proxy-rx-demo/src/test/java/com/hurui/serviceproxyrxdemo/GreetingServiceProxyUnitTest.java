package com.hurui.serviceproxyrxdemo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.serviceproxy.GreetingService;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;

@ExtendWith(VertxExtension.class)
public class GreetingServiceProxyUnitTest {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@BeforeAll
	static void init() {
		logger.info("Running unit tests for Greeting Service...");
	}
	
	@Test
	void helloServiceTest (Vertx vertx, VertxTestContext vertxTestContext) {
		logger.info("Test #1: GreetingService - Method: [rxHello()]");
		GreetingService greetingService = GreetingService.create();
		new ServiceBinder(vertx.getDelegate())
			.setAddress("greetingService")
			.register(GreetingService.class, greetingService);
		
		com.hurui.serviceproxy.reactivex.GreetingService greetingServiceProxy = GreetingService.createProxy(vertx.getDelegate(), "greetingService");
		greetingServiceProxy.rxHello()
			.subscribe(onSuccess -> {
				logger.info("Test passed. Response from service impl: {}", onSuccess);
				vertxTestContext.completeNow();
			}, onError -> {
				logger.error("Test failed, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
	}
	
	@Test
	void goodbyeServiceTest (Vertx vertx, VertxTestContext vertxTestContext) {
		logger.info("Test #2: GreetingService - Method: [rxGoodbye()]");
		GreetingService greetingService = GreetingService.create();
		new ServiceBinder(vertx.getDelegate())
			.setAddress("greetingService")
			.register(GreetingService.class, greetingService);
		
		com.hurui.serviceproxy.reactivex.GreetingService greetingServiceProxy = GreetingService.createProxy(vertx.getDelegate(), "greetingService");
		greetingServiceProxy.rxGoodbye()
			.subscribe(onSuccess -> {
				logger.info("Test passed. Response from service impl: {}", onSuccess);
				vertxTestContext.completeNow();
			}, onError -> {
				logger.error("Test failed, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
	}
	
}
