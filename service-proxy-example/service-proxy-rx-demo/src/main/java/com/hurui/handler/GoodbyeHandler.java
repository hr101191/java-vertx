package com.hurui.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.serviceproxy.GreetingService;

import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

public class GoodbyeHandler implements Handler<RoutingContext> {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private com.hurui.serviceproxy.reactivex.GreetingService greetingServiceProxy;

	@Override
	public void handle(RoutingContext routingContext) {
		logger.info("Route: [{}] - Handling request... Request Body: {}", routingContext.currentRoute().getPath(), routingContext.getBodyAsJson());
		
		// You need to create this service proxy each time the handler is invoked
		greetingServiceProxy = GreetingService.createProxy(routingContext.vertx().getDelegate(), "GreetingService");
		
		greetingServiceProxy.rxGoodbye()
			.subscribe(onSuccess -> {
				logger.info("Route: [{}] - Request completed successfully, ending request with HttpStatus 200, response:\n {}", routingContext.currentRoute().getPath(), onSuccess.encode());
				routingContext.response()
					.setChunked(true)
					.setStatusCode(200)
					.putHeader("content-type", "application/json")
					.end(onSuccess.encodePrettily());
			}, onError -> {
				logger.warn("Error thrown from service proxy, route: [{}]", routingContext.currentRoute().getPath());
				logger.error("Error thrown from service proxy, stacktrace:", onError);
				routingContext.response()
					.setStatusCode(500)
					.end();
			});		
	}
}
