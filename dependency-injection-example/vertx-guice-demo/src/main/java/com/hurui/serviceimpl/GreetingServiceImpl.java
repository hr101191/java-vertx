package com.hurui.serviceimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hurui.service.GoodbyeService;
import com.hurui.service.GreetingService;
import com.hurui.service.HelloService;

public class GreetingServiceImpl implements GreetingService {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private HelloService helloService;
	private GoodbyeService goodbyeService;
	
	@Inject
	public GreetingServiceImpl(HelloService helloService, GoodbyeService goodbyeService) {
		this.helloService = helloService;
		this.goodbyeService = goodbyeService;
		logger.info("Instance created... logging at this constructor to show that an instance of GreetingServiceImpl is created for each verticle. "
				+ "Default scope for Guice is equivalent to Spring PROTOTYPE");
	}
	
	@Override
	public String hello() {
		return helloService.sayHello();
	}

	@Override
	public String goodbye() {
		return goodbyeService.sayGoodbye();
	}
}
