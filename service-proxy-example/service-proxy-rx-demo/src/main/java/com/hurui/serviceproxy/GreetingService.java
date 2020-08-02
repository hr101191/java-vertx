package com.hurui.serviceproxy;

import com.hurui.serviceproxy.impl.GreetingServiceImpl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface GreetingService {

	@GenIgnore
	static GreetingService create() {
		return new GreetingServiceImpl();
	}
	
	@GenIgnore
	static com.hurui.serviceproxy.reactivex.GreetingService createProxy(Vertx vertx, String address) {
		return new com.hurui.serviceproxy.reactivex.GreetingService(new GreetingServiceVertxEBProxy(vertx, address));
	}
	
	@GenIgnore
	static com.hurui.serviceproxy.reactivex.GreetingService createProxyWithCustomDeliveryOption(Vertx vertx, String address, DeliveryOptions deliveryOptions) {
		return new com.hurui.serviceproxy.reactivex.GreetingService(new GreetingServiceVertxEBProxy(vertx, address, deliveryOptions));
	}
	
	@Fluent
	GreetingService hello(Handler<AsyncResult<JsonObject>> resultHandler);
	
	@Fluent
	GreetingService goodbye(Handler<AsyncResult<JsonObject>> resultHandler);
}
