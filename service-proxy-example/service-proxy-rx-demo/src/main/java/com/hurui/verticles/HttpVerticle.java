package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.handler.GoodbyeHandler;
import com.hurui.handler.HelloHandler;

import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class HttpVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	private static int CONSTANT_BFFER_SIZE_INT = 10;
	
	@Override
	public Completable rxStart() {
		HttpServerOptions httpServerOptions = new HttpServerOptions()
				.setPort(8080);
		return Completable.fromSingle(deployHttpServer(httpServerOptions));		
	}

	private Single<HttpServer> deployHttpServer(HttpServerOptions httpServerOptions) {
		Router router = Router.router(vertx);		
		router.get("/api/hello").handler(new HelloHandler());
		router.get("/api/goodbye").handler(new GoodbyeHandler());
		router.route().handler(BodyHandler.create());
		HttpServer httpServer = vertx.createHttpServer(httpServerOptions);
		httpServer.requestStream()
				.toFlowable()
				.map(HttpServerRequest::pause)
				.onBackpressureBuffer(CONSTANT_BFFER_SIZE_INT, //TODO: Set this from config (Default value is 128)
								() -> {
									logger.error("BUFFER OVERFLOW! Dropping latest request...");
								}, 
								BackpressureOverflowStrategy.DROP_LATEST)
				.onBackpressureDrop(onDrop -> onDrop.response().setStatusCode(503).end())
				.observeOn(RxHelper.scheduler(vertx))
				.subscribe(onNext -> {
					router.handle(onNext.resume());
				}, onError -> {
					logger.error("Error stream HttpServerRequest, stacktrace: ", onError);
				});
		return httpServer.rxListen(8080)
				.doOnSubscribe(onSubscribe -> {
					logger.info("Creating Http Server...");
				})
				.doOnSuccess(onSuccess -> {
					logger.info("Successfully created Http Server...");
				})
				.doOnError(onError -> {
					logger.error("Error creating Http Server, stacktrace: ", onError);
				});
	}
}
