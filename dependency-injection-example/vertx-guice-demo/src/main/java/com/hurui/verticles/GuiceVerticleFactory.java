package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

public class GuiceVerticleFactory implements VerticleFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private Injector injector;
	
	//Pass an instance of the DI injector of your choice via the constructor
	public GuiceVerticleFactory(Injector injector) {
		this.injector = injector;
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
		return "GuiceVerticleFactory";
	}

	/*
	 * Will be called by vertx.deployVerticle(String name)
	 */
	@Override
	public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
		logger.info("Overriding createVerticle method...");
		
		//1) Get the class name
		String className = VerticleFactory.removePrefix(verticleName);
		logger.info("Creating an instance of [{}] via Guice...", className);
		
		//2) Let your DI Factory of your choice do the work of creating an instance of the verticle
		// We are using constructor injection but let's leave it for Guice to wire up the necessary dependencies for us
		// There's no need to create the dependencies manually
		return (Verticle) injector.getInstance(classLoader.loadClass(className)); 
	}

}
