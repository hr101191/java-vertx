package com.hurui.serviceimpl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.service.GoodbyeService;
import com.hurui.service.GreetingService;
import com.hurui.service.HelloService;

import io.micronaut.context.annotation.Prototype;

@Prototype
public class GreetingServiceImpl implements GreetingService {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private HelloService helloService;
	private GoodbyeService goodbyeService;
	
	@Inject
	public GreetingServiceImpl(HelloService helloService, GoodbyeService goodbyeService) {
		this.helloService = helloService;
		this.goodbyeService = goodbyeService;
		logger.info("Instance created... logging at this constructor to show that an instance of GreetingServiceImpl is created for each verticle. This is achieved with @Prototype annotation");
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
