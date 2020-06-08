package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	private JsonObject localConfig = new JsonObject();
	
	@Override
	public void start() {
		logger.info("Initializing Main Verticle");
		
		//get the initial config
		if(vertx.getOrCreateContext().config().isEmpty()) {
			//build config store from src/main/resources and read the default config
			logger.info("No --conf overload detected from CLI, loading default-config.json from src/main/resoruces");
			ConfigStoreOptions fileConfigStoreOptions = new ConfigStoreOptions()
					.setType("file")
					.setOptional(true)
					.setConfig(new JsonObject()
							.put("path", "src/main/resources/default-config.json"));
			
			ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
					.setScanPeriod(2000000)
					.addStore(fileConfigStoreOptions);
			
			ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
			
			configRetriever.getConfig(completionHandler -> {
				if(completionHandler.succeeded()) {
					localConfig = completionHandler.result(); //Do something with the default config if required
					logger.info("Default config loaded successfully: " + localConfig);
				} else {
					logger.error("Failed to read default-config.json from src/main/resoruces, stacktrace: ", completionHandler.cause());
					logger.warn("Empty JsonObject used, some components may not load properly!");
				}
			});
		} else {
			//react to the --conf overload from command line and set the config based on the overload path
			localConfig = vertx.getOrCreateContext().config(); //Note that no error message will be thrown here, please ensure input is correct!!
			logger.info("Initial configuration from external context: " + localConfig);
		}		
		
		VertxOptions vertxOptions = new VertxOptions();
		Future<ClusterManager> clusterManagerFuture = buildClusterManager();
		clusterManagerFuture.onComplete(handler -> {
			if(handler.succeeded()) {
				vertxOptions.setClusterManager(handler.result());
			} else {
				logger.error("Failed to created an instance of Hazelcast Cluster Manager, stacktrace: " , handler.cause());
				logger.info("Creating a new instance of Hazelcast Cluster Manager using the default settings...");
				vertxOptions.setClusterManager(new HazelcastClusterManager());
			}
		});
		
		Vertx.clusteredVertx(vertxOptions, resultHandler -> {
			if(resultHandler.succeeded()) {
				logger.info("Obtained an instance of clustered Vertx successfully");				
				//Ensure that you use the clustered Vertx instance to deploy the ConfigStore Verticle so that any config change will be published to subscribing nodes
				resultHandler.result().deployVerticle(new ConfigStoreVerticle(), new DeploymentOptions(), completionHandler -> {
					if(completionHandler.succeeded()) {
						logger.info("Deployed verticle successfully...");
					} else {
						logger.error("Failed to deploy verticle ... stacktrace: ", completionHandler.cause());
					}
				});
			} else {
				logger.error("Failed to obtained an instance of clustered Vertx, stacktrace: " , resultHandler.cause());
				logger.warn("Program will terminate now! Please check the cluster configurations...");
				System.exit(-1);
			}
		});

	}

	private Future<ClusterManager> buildClusterManager() {
		Promise<ClusterManager> promise = Promise.promise();
		vertx.<ClusterManager>executeBlocking(blockingCodeHandler -> {
			try {
				Config hazelcastConfig = new Config();
				hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost").setEnabled(true);
				hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
				hazelcastConfig.getNetworkConfig().setPort(8888).setPortAutoIncrement(true);
				//ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);
				ClusterManager clusterManager = new HazelcastClusterManager();
				promise.complete(clusterManager);
			} catch(Exception ex) {
				promise.fail(ex);
			}			
		}, resultHandler -> {
			if(resultHandler.succeeded()) {
				promise.complete(resultHandler.result());
			} else {
				promise.fail(resultHandler.cause());
			}
		});
		
		return promise.future();
	}

}
