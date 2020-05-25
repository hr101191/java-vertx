package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.context.ApplicationContext;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

public class MicronautVerticleFactory implements VerticleFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private ApplicationContext applicationContext;
	
	//Pass an instance of the DI injector of your choice via the constructor
	public MicronautVerticleFactory(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public boolean blockingCreate() {
		// Use blockingCreate to allow your DI injector ample time to lookup
		// on other beans/resources which might be slow to build/lookup.
		return true;
	}
	
	@Override
	public String prefix() {		
		// Return the class name to identify the VerticleFactory
		return "MicronautVerticleFactory";
	}

	/*
	 * Will be called by vertx.deployVerticle(String name)
	 */
	@Override
	public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
		logger.info("Overriding createVerticle method...");
		
		//1) Get the class name
		String className = VerticleFactory.removePrefix(verticleName);
		logger.info("Creating an instance of [{}] via Micronaut...", className);
		
		//2) Let your DI Factory of your choice do the work of creating an instance of the verticle
		// We are using constructor injection but let's leave it for Micronaut to wire up the necessary dependencies for us
		// There's no need to create the dependencies manually
		return (Verticle) applicationContext.getBean(classLoader.loadClass(className));
	}
	
}
