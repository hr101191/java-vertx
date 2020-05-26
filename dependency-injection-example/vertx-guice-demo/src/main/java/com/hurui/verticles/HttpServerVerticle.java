package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

public class HttpServerVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	private EventBus eventBus;
	
	@Override
	public void start() {
		eventBus = vertx.eventBus();
		
		HttpServerOptions options = new HttpServerOptions()
				.setSsl(false)
				.setPort(8080);
		
		Router router = Router.router(getVertx());
		router.route().handler(BodyHandler.create()); //To handle the response body
		router.route().handler(TimeoutHandler.create(5000)); //Timeout handler
		
		HttpServer httpServer = vertx.createHttpServer(options);
		httpServer.requestHandler(router);
		
		router.route(HttpMethod.GET, "/api/hello").handler(this::helloHandler);
		router.route(HttpMethod.GET, "/api/goodbye").handler(this::goodbyeHandler);
		
		httpServer.listen(listenHandler -> {
			if(listenHandler.succeeded()) {
				logger.info("HttpServer started successfully... Listening on port: [{}]", listenHandler.result().actualPort());
			}else {
				logger.error("Error starting HttpServer... Stacktrace: ", (Throwable) listenHandler.cause());
			}
		});
	}
	
	private void helloHandler(RoutingContext routingContext) {
		logger.info("Handling request... Route: [/api/hello]");
		eventBus.<String>request("hello", null, replyHandler -> {
			if(replyHandler.succeeded()) {
				try {
					routingContext.response().setChunked(true).putHeader("content-type", "text/plain").setStatusCode(200).write(replyHandler.result().body()).end();
					logger.info("Ended Http response successfully... Route: [/api/hello]");
				}catch(Exception ex) {
					logger.error("Error... Route: [/api/hello] | stacktrace: ", ex);	
					routingContext.response().setStatusCode(500).end();
				}
			}else {
				logger.error("Error... Route: [/api/hello] | stacktrace: ", (Throwable) replyHandler.cause());	
				routingContext.response().setStatusCode(500).end();
			}
		});
	}
	
	private void goodbyeHandler(RoutingContext routingContext) {
		logger.info("Handling request... Route: [/api/goodbye]");
		eventBus.<String>request("goodbye", null, replyHandler -> {
			if(replyHandler.succeeded()) {
				try {
					routingContext.response().setChunked(true).putHeader("content-type", "text/plain").setStatusCode(200).write(replyHandler.result().body()).end();
					logger.info("Ended Http response successfully... Route: [/api/goodbye]");
				}catch(Exception ex) {
					logger.error("Error... Route: [/api/goodbye] | stacktrace: ", ex);	
					routingContext.response().setStatusCode(500).end();
				}
			}else {
				logger.error("Error... Route: [/api/goodbye] | stacktrace: ", (Throwable) replyHandler.cause());	
				routingContext.response().setStatusCode(500).end();
			}
		});
	}

}
