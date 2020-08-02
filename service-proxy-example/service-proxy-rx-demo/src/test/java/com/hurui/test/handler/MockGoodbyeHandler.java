package com.hurui.test.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public class MockGoodbyeHandler implements Handler<RoutingContext> {

	//Mocks the default OK response for the original route
	@Override
	public void handle(RoutingContext routingContext) {
		routingContext.response()
			.setChunked(true)
			.setStatusCode(200)
			.putHeader("content-type", "application/json")
			.end(new JsonObject()
					.put("message", "Goodbye")
					.encodePrettily());
	}

}
