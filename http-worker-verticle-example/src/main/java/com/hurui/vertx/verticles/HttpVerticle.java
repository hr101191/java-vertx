package com.hurui.vertx.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hurui.vertx.controller.Controller;
import com.hurui.vertx.messagecodec.GenericApiEventMessage;
import com.hurui.vertx.messagecodec.GenericApiEventMessageCodec;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

@Service
public class HttpVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Autowired
	private Controller controller;
	
	@Override
	public void start() {
		HttpServerOptions options = new HttpServerOptions();
		options.setSsl(true); //enable SSL
		options.setKeyStoreOptions(new JksOptions() //configure keystore
				.setPath("server-keystore-new.jks") //points to src/resources if no qualified path is provided
				.setPassword("11111111")); 
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
		
		//Configure your route the handler method in the controller class
		router.route(HttpMethod.GET, "/api/get").handler(routingContext -> controller.getApiHandler(routingContext, getVertx(), eventBus));
		router.route(HttpMethod.POST, "/api/post").handler(routingContext -> controller.postApiHandler(routingContext, vertx, eventBus));
		router.route(HttpMethod.GET, "/api/get/timeout").handler(routingContext -> controller.getTimeoutApiHandler(routingContext, vertx, eventBus));
		router.route(HttpMethod.POST, "/api/post/timeout").handler(routingContext -> controller.postTimeoutApiHandler(routingContext, vertx, eventBus));
		router.route(HttpMethod.GET, "/api/get/javaconcurrentfuture").handler(routingContext -> controller.getApiJavaFutureHandler(routingContext, vertx, eventBus));
		
		HttpServer httpServer = getVertx().createHttpServer(options);
		httpServer.requestHandler(router).listen(); //start listening
		logger.info("Http eventloop started. listening for requests...");
	}
	
	

	
}
