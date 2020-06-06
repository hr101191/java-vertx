package com.hurui.verticles;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class DemoVerticle extends AbstractVerticle {
	@Override
	public void start() {
		System.out.println("CONFIGGGGGGGGGG: " + vertx.getOrCreateContext().config());
	}
}
