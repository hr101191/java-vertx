package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

@Component
@Scope("prototype") //Prototype scope is needed if you want to deploy multiple instances of this verticle
public class HttpServerVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private EventBus eventBus;
	
	@Override
	public void start() {
		//TODO: Externalize the configuration
		HttpServerOptions options = new HttpServerOptions()
				.setSsl(true) //enable SSL
				.setKeyStoreOptions(new JksOptions() //configure keystore
					.setPath("server-b-keystore.jks") //points to src/resources if no qualified path is provided
					.setPassword("11111111")
				)
				.setTrustStoreOptions(new JksOptions() //configure truststore
					.setPath("server-b-truststore.jks") //points to src/resources if no qualified path is provided
					.setPassword("11111111")
				)
				.addEnabledSecureTransportProtocol("TLSv1.3")
				.addEnabledSecureTransportProtocol("TLSv1.2")
				.addEnabledSecureTransportProtocol("TLSv1.1")
				.addEnabledSecureTransportProtocol("TLSv1.0")
				.setPort(8081);
		
		eventBus = getVertx().eventBus();
		
		Router router = Router.router(getVertx());
		router.route().handler(BodyHandler.create()); //To handle the response body
		router.route().handler(TimeoutHandler.create(5000)); //Timeout handler
		
		//Configure your routes and pass the RoutingContext to the respective Service Verticle
		router.route(HttpMethod.GET, "/api/greeting").handler(this::handleGet);
		router.route(HttpMethod.GET, "/api/greeting/:message").handler(this::handleGet);
		router.route(HttpMethod.GET, "/api/hello").handler(this::handleGet);
		
		HttpServer httpServer = getVertx().createHttpServer(options);
		httpServer.requestHandler(router);
		
		httpServer.listen();
	}
	
	private void handleGet(RoutingContext routingContext) {
		try {			
			switch(routingContext.currentRoute().getPath()) {
				case "/api/greeting": 
					logger.info("Received request... Route: [/api/greeting]");	
					eventBus.request("/api/greeting", null, replyHandler -> {
						if(replyHandler.succeeded()) {
							JsonObject jsonObject = (JsonObject) replyHandler.result().body();
							logger.info("Ending response... Route: [/api/greeting] | Success | Status Code: {} | Response Body: {}", jsonObject.getInteger("statusCode"), jsonObject.getJsonObject("responseBody"));
							routingContext.response().setChunked(true).putHeader("content-type", "application/json").setStatusCode(jsonObject.getInteger("statusCode")).write(jsonObject.getJsonObject("responseBody").encodePrettily()).end();						
						} else {
							logger.error("Error... Route: [/api/greeting] | stacktrace: ", replyHandler.cause());								
							routingContext.response().setStatusCode(500).end();
						}
					});
					break;
				case "/api/greeting/:message": //TODO: complete this implementation
					eventBus.request("/api/greeting:message", routingContext.pathParam("message"), replyHandler -> {
						if(replyHandler.succeeded()) {
							
						} else {
							
						}
					});
					break;
				case "/api/hello": //simple endpoint for server A to call
					routingContext.response().putHeader("content-type", "application/json").setStatusCode(200).end("{\"message\":\"hello from server B\"}");
					break;
				default:
					logger.error("No valid handler has been set for path: {}", routingContext.currentRoute().getPath());
					routingContext.response().setStatusCode(422).end();
					break;				
			}
		} catch(Exception ex) {
			logger.error("Get Handler failed! Stacktrace: ", ex);
		}	
	}
}
