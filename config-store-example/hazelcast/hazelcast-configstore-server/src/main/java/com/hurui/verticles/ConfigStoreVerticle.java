package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ConfigStoreVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private JsonObject jsonConfig;
	
	@Override
	public void start() {
		EventBus eventBus = vertx.eventBus();
		
		ConfigStoreOptions directoryConfigStoreOptions = new ConfigStoreOptions()
				.setType("directory")
			    .setConfig(new JsonObject().put("path", "src/main/resources/config") //TODO: change this path to command line args instead of classpath
			    		.put("filesets", new JsonArray()
			    				.add(new JsonObject().put("pattern", "*json"))
			    				));
	
		ConfigRetrieverOptions options = new ConfigRetrieverOptions()
				//.setScanPeriod(2000)
				.addStore(directoryConfigStoreOptions);
		
		ConfigRetriever configRetriever = ConfigRetriever.create(getVertx(), options);
		// Fetch the config from the eventbus config store on startup
		configRetriever.getConfig(asyncResult -> {
			if(asyncResult.succeeded()) {
				logger.info("Initialized json config: " + asyncResult.result());
				jsonConfig = asyncResult.result();
			} else {
				logger.info("Failed to initialize json config... stacktrace:  ", asyncResult.cause());
			}
		});
		

		configRetriever.listen(listener -> {
			logger.info("Existing config: " + listener.getPreviousConfiguration());
			logger.info("New Config: " + listener.getNewConfiguration());
			logger.info("Publishing the new config to all subscribers listening to [app-config] queue in the cluster...");
			eventBus.publish("app-config", listener.getNewConfiguration());
		});
		
		eventBus.consumer("initial-config-queue", handler -> {
			logger.info("Received request from consumer, sending json config: " + jsonConfig);
			handler.reply(jsonConfig);
		});
	}
}
