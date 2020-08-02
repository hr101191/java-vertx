package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.serviceproxy.GreetingService;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class GreetingServiceVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@Override
	public Completable rxStart() {
		return Completable.fromRunnable(() -> {
					bindGreetingService();
				})
				.doOnComplete(() -> {
					logger.info("Completed initializing GreetingServiceVerticle...");
				})
				.doOnError(onError -> {
					logger.error("Failed to initialize GreetingServiceVerticle, stacktrace: ", onError);
				});
	}
	
	private void bindGreetingService() {
		GreetingService greetingService = GreetingService.create();
		new ServiceBinder(vertx.getDelegate())
			.setAddress("GreetingService") //TODO: parse this from config file instead
			.setTimeoutSeconds(60l) //TODO: parse this from config file instead
			.register(GreetingService.class, greetingService);
	}
}
