package com.hurui.serviceimpl;

import com.hurui.service.HelloService;

import io.micronaut.context.annotation.Prototype;

@Prototype
public class HelloServiceImpl implements HelloService {

	@Override
	public String SayHello() {
		return "Hello!";
	}

}
