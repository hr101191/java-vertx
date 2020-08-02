package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public Completable rxStart() {
		JsonObject config = vertx.getOrCreateContext().config();
		return deployVerticles(config);
	}
	
	private Completable deployVerticles(JsonObject config) {
		return Completable.fromSingle(deployGreetingServiceVerticle(config))
				.andThen(Completable.fromSingle(deployHttpVerticle(config)))
				.doOnComplete(() -> {
					logger.info("Deployed all verticles successfully");
				})
				.doOnError(onError -> {
					logger.error("One or more verticle(s) failed to start properly, stacktrace: ", onError);
				});
	}
	
	private Single<String> deployHttpVerticle(JsonObject config) {
		return vertx.rxDeployVerticle(new HttpVerticle(), new DeploymentOptions().setConfig(config))
				.doOnSubscribe(onSubscribe -> {
					logger.info("Deploying http verticle...");
				})
				.doOnSuccess(onSuccess -> {
					logger.info("Successfully deployed http verticle, deployment ID: [{}]", onSuccess);
				})
				.doOnError(onError -> {
					logger.error("Failed to deploy http verticle, stacktrace: ", onError);
				});
	}
	
	private Single<String> deployGreetingServiceVerticle(JsonObject config) {
		return vertx.rxDeployVerticle(new GreetingServiceVerticle(), new DeploymentOptions().setConfig(config))
				.doOnSubscribe(onSubscribe -> {
					logger.info("Deploying greeting service verticle...");
				})
				.doOnSuccess(onSuccess -> {
					logger.info("Successfully deployed greeting service, deployment ID: [{}]", onSuccess);
				})
				.doOnError(onError -> {
					logger.info("Failed to deploy greeting service, stacktrace: ", onError);
				});
	}
}
