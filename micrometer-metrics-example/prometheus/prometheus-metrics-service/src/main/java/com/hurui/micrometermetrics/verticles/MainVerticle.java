package com.hurui.micrometermetrics.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public Completable rxStart() {
		logger.info("Json Config from context: " +  vertx.getOrCreateContext().config().toString());
		logger.info("Deploying Verticles...");
		return deployVerticles()
				.doOnComplete(() -> {
					logger.info("Successfully deployed all verticles...");
				})
				.doOnError(onError -> {
					logger.error("Failed to deploy verticles, stacktrace: ", onError);
				});
	}
	
	private Completable deployVerticles() {
		return Completable.fromSingle(deployHttpServerVerticle())
				.doOnSubscribe(onSubscribe -> {
					logger.info("Deploying Http Server Verticle...");
				})					
				.doOnError(onError -> {
					logger.error("Failed to deploy Http Server Verticle...");
				});
	}
	
	private Single<String> deployHttpServerVerticle() {
		return vertx.rxDeployVerticle(new HttpServerVerticle(), new DeploymentOptions())
				.doOnSuccess(onSuccess -> {
					logger.info("Deploying Http Server Verticle successfully... DeploymentId: [{}]", onSuccess);
				});
	}
	
}