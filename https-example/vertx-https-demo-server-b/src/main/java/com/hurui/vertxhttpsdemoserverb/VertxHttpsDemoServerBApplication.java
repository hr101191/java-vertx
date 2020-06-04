package com.hurui.vertxhttpsdemoserverb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.hurui.verticles.GreetingServiceVerticle;
import com.hurui.verticles.HttpServerVerticle;
import com.hurui.verticles.SpringVerticleFactory;
import com.hurui.verticles.WebClientVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;

@Configuration
@ComponentScan("com.hurui")
public class VertxHttpsDemoServerBApplication {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

	@SuppressWarnings("resource")
	public static void main(String[] args) {

		Vertx vertx = Vertx.vertx();
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(VertxHttpsDemoServerBApplication.class);
				
		VerticleFactory verticleFactory = applicationContext.getBean(SpringVerticleFactory.class);

		vertx.registerVerticleFactory(verticleFactory);
		
		vertx.deployVerticle(verticleFactory.prefix() + ":" + WebClientVerticle.class.getName(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed WebClientVerticle successfully...");
			} else {
				logger.error("Error deploying WebClientVerticle, stacktrace: ", completionHandler.cause());
			}
		});
		vertx.deployVerticle(verticleFactory.prefix() + ":" + GreetingServiceVerticle.class.getName(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed GreetingServiceVerticle successfully...");
			} else {
				logger.error("Error deploying GreetingServiceVerticle, stacktrace: ", completionHandler.cause());
			}
		});
		vertx.deployVerticle(verticleFactory.prefix() + ":" + HttpServerVerticle.class.getName(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed HttpServerVerticle successfully...");
			} else {
				logger.error("Error deploying HttpServerVerticle, stacktrace: ", completionHandler.cause());
			}
		});	
	}
	
}
