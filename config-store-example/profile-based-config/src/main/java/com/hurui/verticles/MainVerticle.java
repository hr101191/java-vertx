package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	private JsonObject localConfig = new JsonObject();
	
	@Override
	public void start() {
		ConfigStoreOptions fileConfigStoreOptions = new ConfigStoreOptions();
		
		/* (non-Javadoc)
		 * Check if there is -conf overload passed via command line.
		 */
		if(vertx.getOrCreateContext().config().isEmpty()) {
			//load the default config for local env
			logger.warn("-conf parameter is not passed via command line! Loading application-local-config from classpath...");	
			fileConfigStoreOptions.setType("file")
				.setConfig(new JsonObject()
				.put("path", "src/main/resources/application-local-config.json"));
		} else {
			// Get json config passed via command line
			JsonObject commandLineConfig = vertx.getOrCreateContext().config();
			
			// If the key "activeProfile" is found, implement custom logic to load the profile-based json from classpath
			if(commandLineConfig.containsKey("activeProfile")) {
				String profile = commandLineConfig.getString("activeProfile");
				if (profile.equalsIgnoreCase("prod")) {
					logger.info("Active profile: [{}]", profile);	
					fileConfigStoreOptions.setType("file")
						.setConfig(new JsonObject()
						.put("path", "src/main/resources/application-prod-config.json"));
				} else if (profile.equalsIgnoreCase("uat")) {
					logger.info("Active profile: [{}]", profile);
					fileConfigStoreOptions.setType("file")
						.setConfig(new JsonObject()
						.put("path", "src/main/resources/application-uat-config.json"));
				} else {
					logger.warn("Invalid profile: [{}] Loading application-local-config from classpath...", profile);
					fileConfigStoreOptions.setType("file")
						.setConfig(new JsonObject()
						.put("path", "src/main/resources/application-local-config.json"));
				}
			} 	
		}
		
		ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
				.setScanPeriod(2000000)
				.addStore(fileConfigStoreOptions);
		
		ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
		
		// Serve a page to test retrieving port from config
	    Router router = Router.router(vertx);

	    router.route().handler(routingContext -> {
	    	routingContext.response().putHeader("content-type", "text/html").end("Hello World!");
	    });
		
		configRetriever.getConfig(completionHandler -> {
			if(completionHandler.succeeded()) {
				localConfig = completionHandler.result(); //Do something with the default config if required
				logger.info("Retrieved config successfully: " + localConfig);
			    vertx.createHttpServer().requestHandler(router).listen(localConfig.getInteger("httpPort"));
			    logger.info("Deployed http server, port: [{}]", localConfig.getInteger("httpPort"));
			} else {
				logger.error("Failed to read json config from src/main/resources, stacktrace: ", completionHandler.cause());
				logger.warn("Empty JsonObject used, some components may not load properly!");
			}
		});
	}
	
}
