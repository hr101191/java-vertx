package com.hurui.micrometermetrics.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.reactivex.Completable;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;

public class HttpServerVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public Completable rxStart() {
		return Completable.fromRunnable(() -> {
				deployHttpServer();
			});
	}
	
	private void deployHttpServer() {
		PrometheusMeterRegistry registry = (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
		Router router = Router.router(vertx);
		// Setup a route for metrics
		// publishes to port 8080, make sure this is configured in your prometheus.yml
	    router.route("/metrics").handler(ctx -> {
	    	String response = registry.scrape();
	    	logger.info("response: " + response);
	    	ctx.response().end(response);
	    });
	    router.get("/").handler(ctx -> {
	    	logger.info("handling request...");
	    	ctx.response()
	    		.setStatusCode(200)
	    		.end("Hello world");
	    });
	    vertx.createHttpServer().requestHandler(router).listen(8080);
	}
}
