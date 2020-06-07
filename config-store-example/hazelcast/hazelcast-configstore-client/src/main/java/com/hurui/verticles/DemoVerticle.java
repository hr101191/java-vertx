package com.hurui.verticles;

import io.vertx.core.AbstractVerticle;

public class DemoVerticle extends AbstractVerticle {
	@Override
	public void start() {
		System.out.println("In deployed verticle, config: " + vertx.getOrCreateContext().config());
	}
}
