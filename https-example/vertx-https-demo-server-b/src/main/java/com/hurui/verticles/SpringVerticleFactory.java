package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

@Component
public class SpringVerticleFactory implements VerticleFactory, ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private ApplicationContext applicationContext;
	  
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public boolean blockingCreate() {
	    // Usually verticle instantiation is fast but since our verticles are Spring Beans,
	    // they might depend on other beans/resources which are slow to build/lookup.
	    return true;
	}

	@Override
	public String prefix() {
	    return "SpringVerticleFactory";
	}

	@Override
	public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
		logger.info("Overriding createVerticle method...");
	    String className = VerticleFactory.removePrefix(verticleName);
	    logger.info("Creating an instance of [{}] via Spring Context...", className);
	    return (Verticle) applicationContext.getBean(Class.forName(className));
	}



}
