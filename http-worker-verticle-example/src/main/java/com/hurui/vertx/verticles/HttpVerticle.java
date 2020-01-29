package com.hurui.vertx.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.vertx.messagecodec.GenericApiEventMessage;
import com.hurui.vertx.messagecodec.GenericApiEventMessageCodec;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

public class HttpVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public void start() {
		HttpServerOptions options = new HttpServerOptions();
		options.setSsl(true); //enable SSL
		options.setKeyStoreOptions(new JksOptions() //configure keystore
				.setPath("server-keystore.jks") //points to src/resources if no qualified path is provided
				.setPassword("wibble")); 
		options.addEnabledSecureTransportProtocol("TLSv1.3");
		options.addEnabledSecureTransportProtocol("TLSv1.2");
		options.addEnabledSecureTransportProtocol("TLSv1.1");
		options.addEnabledSecureTransportProtocol("TLSv1.0");
		options.setPort(8080);
		
		//Init the eventbus and register a message codec to pass request to eventbus a.s.a.p
		EventBus eventBus = getVertx().eventBus();
		eventBus.registerDefaultCodec(GenericApiEventMessage.class, new GenericApiEventMessageCodec());
		
		//Route config
		Router router = Router.router(getVertx());
		router.route().handler(BodyHandler.create()); //To handle the response body
		router.route().handler(TimeoutHandler.create(5000)); //Timeout handler
		
		//Configure your controller to route the request to be handled by the eventbus
		//Should contain as little logic as possible here
		Route getRoute = router.route(HttpMethod.GET, "/api/get");	
		getRoute.handler(routingContext -> {
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
		});
		
		Route postRoute = router.route(HttpMethod.POST, "/api/post");	
		postRoute.handler(routingContext -> {
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
		});	
		
		Route getRouteTimeout = router.route(HttpMethod.GET, "/api/get/timeout");	
		getRouteTimeout.handler(routingContext -> {
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
		});
		
		Route postRouteTimeout = router.route(HttpMethod.POST, "/api/post/timeout");	
		postRouteTimeout.handler(routingContext -> {
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
		});	
		
		HttpServer httpServer = getVertx().createHttpServer(options);
		httpServer.requestHandler(router).listen(); //start listening
		logger.info("Http eventloop started. listening for requests...");
	}
}
