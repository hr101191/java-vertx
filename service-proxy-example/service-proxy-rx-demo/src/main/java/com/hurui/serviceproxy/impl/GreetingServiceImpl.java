package com.hurui.serviceproxy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.serviceproxy.GreetingService;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class GreetingServiceImpl implements GreetingService {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public GreetingService hello(Handler<AsyncResult<JsonObject>> resultHandler) {
		generateHelloMessage()
			.doOnSubscribe(onSubscribe -> {
				logger.info("");
			})
			.subscribe(onSuccess -> {
				resultHandler.handle(Future.succeededFuture(onSuccess));
			}, onError -> {
				resultHandler.handle(Future.failedFuture(onError));
			});
		return this;
	}

	@Override
	public GreetingService goodbye(Handler<AsyncResult<JsonObject>> resultHandler) {
		generateGoodbyeMessage()
			.doOnSubscribe(onSubscribe -> {
				logger.info("");
			})
			.subscribe(onSuccess -> {
				resultHandler.handle(Future.succeededFuture(onSuccess));
			}, onError -> {
				resultHandler.handle(Future.failedFuture(onError));
			});
		return this;
	}

	private Single<JsonObject> generateHelloMessage() {
		return Single.just(new JsonObject()
				.put("message", "Hello"));
	}
	
	private Single<JsonObject> generateGoodbyeMessage() {
		return Single.just(new JsonObject()
				.put("message", "Goodbye"));
	}
}
