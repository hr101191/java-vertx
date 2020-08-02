package com.hurui.serviceproxyrxdemo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.junit5.VertxExtension;

@ExtendWith(VertxExtension.class)
class ServiceProxyRxDemoApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	@BeforeAll
	static void init() {
		
	}
	
	@Test
	void modularTest () {
		logger.info("this is a test");
	}
	
	@Test
	void fullTest() {
		
	}

}
