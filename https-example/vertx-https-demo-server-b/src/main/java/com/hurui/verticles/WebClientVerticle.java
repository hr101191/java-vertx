package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class WebClientVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());	
	private ApplicationContext applicationContext;
	private EventBus eventBus;	
	private WebClient webClient;
	
	public WebClientVerticle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void start() {
		//TODO: Externalize the configuration
		webClient = WebClient.create(vertx, new WebClientOptions()
				.setSsl(true)
				.setTrustAll(false)
				.setKeyStoreOptions(new JksOptions() //configure keystore
						.setPath("server-b-keystore.jks") //points to src/resources if no qualified path is provided
						.setPassword("11111111")
						)
				.setTrustStoreOptions(new JksOptions() //configure truststore
						.setPath("server-b-truststore.jks") //points to src/resources if no qualified path is provided
						.setPassword("11111111")
						)
				);
			
		eventBus = vertx.eventBus();
		
		eventBus.<JsonObject>localConsumer("HTTP_GET").handler(handler -> {
			vertx.executeBlocking(blockingCodeHandler -> {
				try {
					logger.info("EventBus Address: [{}] - received incoming message...", handler.address());
					JsonObject jsonObject = (JsonObject) handler.body();
					int port = jsonObject.getInteger("port");
					String hostName = jsonObject.getString("hostName");
					String path = jsonObject.getString("path");
					Future<JsonObject> future = handleGetRequest(port, hostName, path);
					future.onComplete(completionHandler -> {
						if(completionHandler.succeeded()) {
							blockingCodeHandler.complete(completionHandler.result());
						} else {
							blockingCodeHandler.fail(completionHandler.cause());
						}
					});
				} catch(Exception ex) {
					blockingCodeHandler.fail(ex);
				}
			}, resultHandler -> {
				if(resultHandler.succeeded()) {
					logger.info("EventBus Address: [{}] - sending successful response...", handler.address());
					JsonObject jsonObject = new JsonObject()
							.put("isSuccess", Boolean.TRUE)
							.put("response", resultHandler.result());
					handler.reply(jsonObject);
				}else {
					logger.error("EventBus Address: [{}] - sending failure response...", handler.address());
					JsonObject jsonObject = new JsonObject()
							.put("isSuccess", Boolean.FALSE)
							.put("statusCode", 500)
							.put("errorMessage", resultHandler.cause().getMessage());
					handler.reply(jsonObject);					
				}
			});
		});
	}
	
	//TODO: put this in WebClientServiceImpl Class and Expose this via interface
	private Future<JsonObject> handleGetRequest(int port, String hostName, String path){
		Promise<JsonObject> promise = Promise.promise();
		try {
			logger.info("Sending [HTTP.GET] request... Port: {} | HostName: {} | Path: {}", port, hostName, path);
			webClient.get(port, hostName, path)	
				.send(handler ->{
					if(handler.succeeded()) {
						HttpResponse<Buffer> response = handler.result();
						logger.info("Status Code {} | Response: {}", response.statusCode(), response.bodyAsString());
						promise.complete(response.bodyAsJsonObject());
					} else {
						logger.error("Error calling remote endpoint, stacktrace: ", (Throwable) handler.cause());						
						promise.fail(handler.cause());
					}
			});
		} catch(Exception ex) {
			logger.error("Error calling remote endpoint, stacktrace: [{}]", ex);
			promise.fail(ex);
		}
		return promise.future();
	}

}
