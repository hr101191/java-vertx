package com.hurui.test.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.test.handler.MockGoodbyeHandler;
import com.hurui.test.handler.MockHelloHandler;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class MockHttpVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public Completable rxStart() {
		return Completable.fromSingle(deployMockHttpServer());
	}
	
	private Single<HttpServer> deployMockHttpServer() {
		HttpServer httpServer = vertx.createHttpServer();
		Router router = Router.router(vertx);		
		router.get("/api/mock/hello").handler(new MockHelloHandler()); //You can rewrite the Handler class to simulate certain behaviour specifically.
		router.get("/api/mock/goodbye").handler(new MockGoodbyeHandler());
		router.route().handler(BodyHandler.create());
		return httpServer.requestHandler(router)
				.rxListen(11111)
				.doOnSubscribe(onSubscribe -> {
					logger.info("Creating mock Http Server...");
				})
				.doOnSuccess(onSuccess -> {
					logger.info("Successfully mock created Http Server...");
				})
				.doOnError(onError -> {
					logger.error("Error creating mock Http Server, stacktrace: ", onError);
				});
	}
}
