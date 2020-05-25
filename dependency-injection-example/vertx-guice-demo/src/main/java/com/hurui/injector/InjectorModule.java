package com.hurui.injector;

import com.google.inject.AbstractModule;
import com.hurui.service.GoodbyeService;
import com.hurui.service.GreetingService;
import com.hurui.service.HelloService;
import com.hurui.serviceimpl.GoodbyeServiceImpl;
import com.hurui.serviceimpl.GreetingServiceImpl;
import com.hurui.serviceimpl.HelloServiceImpl;

public class InjectorModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(GreetingService.class).to(GreetingServiceImpl.class);
		bind(HelloService.class).to(HelloServiceImpl.class);
		bind(GoodbyeService.class).to(GoodbyeServiceImpl.class);
	}
	
}
