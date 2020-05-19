package com.hurui.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

public class HttpServerVerticle extends AbstractVerticle {

	@Override
	public void start() {
		//TODO: Externalize the configuration
		HttpServerOptions options = new HttpServerOptions()
				.setSsl(true) //enable SSL
				.setKeyStoreOptions(new JksOptions() //configure keystore
					.setPath("server-a-keystore.jks") //points to src/resources if no qualified path is provided
					.setPassword("11111111")
				)
				.setTrustStoreOptions(new JksOptions() //configure truststore
					.setPath("server-a-truststore.jks") //points to src/resources if no qualified path is provided
					.setPassword("11111111")
				)
				.addEnabledSecureTransportProtocol("TLSv1.3")
				.addEnabledSecureTransportProtocol("TLSv1.2")
				.addEnabledSecureTransportProtocol("TLSv1.1")
				.addEnabledSecureTransportProtocol("TLSv1.0")
				.setPort(8080);
		
		EventBus eventBus = getVertx().eventBus();
		
		Router router = Router.router(getVertx());
		router.route().handler(BodyHandler.create()); //To handle the response body
		router.route().handler(TimeoutHandler.create(5000)); //Timeout handler
		
		//Configure your routes and pass the RoutingContext to the respective Service Verticle
		router.route(HttpMethod.GET, "/api/greeting").handler(routingContext -> {
			eventBus.send("GreetingServiceVerticle", routingContext);
		});
		
		HttpServer httpServer = getVertx().createHttpServer(options);
		httpServer.requestHandler(router);
		httpServer.listen();
	}
}
