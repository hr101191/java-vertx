package com.hurui.serviceimpl;

import com.hurui.service.GoodbyeService;

import io.micronaut.context.annotation.Prototype;

@Prototype
public class GoodbyeServiceImpl implements GoodbyeService {

	@Override
	public String sayGoodbye() {
		return "Goodbye";
	}

}
