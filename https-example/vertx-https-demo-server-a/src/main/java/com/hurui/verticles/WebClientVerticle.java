package com.hurui.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class WebClientVerticle extends AbstractVerticle {
	
	@Override
	public void start() {
		//TODO: Externalize the configuration
		WebClient client = WebClient.create(getVertx(), new WebClientOptions()
				.setSsl(true)
				.setTrustAll(false)
				.setKeyStoreOptions(new JksOptions() //configure keystore
						.setPath("server-a-keystore.jks") //points to src/resources if no qualified path is provided
						.setPassword("11111111")
						)
				.setTrustStoreOptions(new JksOptions() //configure truststore
						.setPath("server-a-truststore.jks") //points to src/resources if no qualified path is provided
						.setPassword("11111111")
						)
				);
	}
}
