package com.hurui.serviceimpl;

import com.hurui.service.HelloService;

public class HelloServiceImpl implements HelloService {

	@Override
	public String sayHello() {
		return "Hello!";
	}

}
