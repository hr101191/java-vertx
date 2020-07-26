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
		logger.info("Deploying Verticles...");
		loadPrometheusOptions();
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

	private void loadPrometheusOptions() {
		// Deploy with embedded server: prometheus metrics will be automatically exposed,
	    // independently from any other HTTP server defined
	    MicrometerMetricsOptions micrometerMetricsOptions = new MicrometerMetricsOptions()
		    		.setPrometheusOptions(new VertxPrometheusOptions()
		    		//.setStartEmbeddedServer(true)
		    		//.setEmbeddedServerOptions(new HttpServerOptions().setPort(9090))
		    		.setEnabled(true)
	    		)
	    		.setEnabled(true);		 
	    
	    /* (non-Javadoc)
	     * Create a new instance of Vertx and set the VertxOptions. 
	     * Then use this instance of Vertx to override the existing Vertx from this verticle and deploy the other verticles
	     * 
	     * You will see the warning below in the log because Vertx.vertx() always creates a new instance of Vertx:
	     * WARNING: You're already on a Vert.x context, are you sure you want to create a new Vertx instance? 
	     */
	     vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(micrometerMetricsOptions));
	}
	
	private Single<String> deployHttpServerVerticle() {
		return vertx.rxDeployVerticle(new HttpServerVerticle(), new DeploymentOptions())
				.doOnSuccess(onSuccess -> {
					logger.info("Deploying Http Server Verticle successfully... DeploymentId: [{}]", onSuccess);
				});
	}
	
}