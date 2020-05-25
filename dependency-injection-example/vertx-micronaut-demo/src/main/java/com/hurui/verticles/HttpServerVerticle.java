package com.hurui.verticles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.service.GreetingService;

import io.micronaut.context.annotation.Prototype;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

@Prototype
public class HttpServerVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private GreetingService greetingService;
	
	@Inject
	public HttpServerVerticle(GreetingService greetingService) {
		this.greetingService = greetingService;
	}
	
    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
    }
	
	@Override
	public void start() {
		HttpServerOptions options = new HttpServerOptions()
				.setSsl(false)
				.setPort(8080);
		
		Router router = Router.router(getVertx());
		router.route().handler(BodyHandler.create()); //To handle the response body
		router.route().handler(TimeoutHandler.create(5000)); //Timeout handler
		
		HttpServer httpServer = vertx.createHttpServer(options);
		httpServer.requestHandler(router);
		
		logger.info(greetingService.Hello());
		
		httpServer.listen(listenHandler -> {
			if(listenHandler.succeeded()) {
				logger.info("HttpServer started successfully... Listening on port: [{}]", listenHandler.result().actualPort());
			}else {
				logger.error("Error starting HttpServer... Stacktrace: ", (Throwable) listenHandler.cause());
			}
		});
	}

}
