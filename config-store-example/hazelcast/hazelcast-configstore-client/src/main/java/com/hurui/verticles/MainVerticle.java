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
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private JsonObject jsonConfig;
	private ConfigRetriever retriever;
	private String demoVerticleDeploymentId;
	
	@Override
	public void start() {
		logger.info("Initializing Main Verticle");		

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
				
				ConfigStoreOptions eventBusConfigStoreOptions = new ConfigStoreOptions()
						.setType("event-bus")
						.setConfig(new JsonObject()
								.put("address", "app-config")
								);
				ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
						  .addStore(eventBusConfigStoreOptions);
				
				ConfigRetriever configRetriever = ConfigRetriever.create(resultHandler.result(), configRetrieverOptions);
				
				// configRetriever.getConfig is not working... It does not retrieve the config from the event-bus store
				// instead, use eventbus to retrieve initial config
				resultHandler.result().eventBus().request("initial-config-queue", "", replyHandler -> {
					if(replyHandler.succeeded()) {
						jsonConfig = (JsonObject) replyHandler.result().body();
						logger.info("Fetched json config from event-bus: " + jsonConfig);
						//deploy verticle here after obtaining the config
						//you may use local vertx here as clustered vertx will slow down performance
						vertx.deployVerticle(new DemoVerticle(), new DeploymentOptions().setConfig(jsonConfig), completionHandler -> {
							if(completionHandler.succeeded()) {
								logger.info("Deployed verticle successfully... Deployment ID: [{}]", completionHandler.result());
								demoVerticleDeploymentId = completionHandler.result();					
							} else {
								logger.error("Failed tp deploy verticle ... stacktrace: ", completionHandler.cause());
							}					
						});
					}
				});
				
				// Listen to changes published by node which reads from file via event-bus
				configRetriever.listen(listener -> {	
					logger.info("Incoming updates from event-bus...");
					logger.info("Previous json config from event-bus: " + listener.getPreviousConfiguration());
					logger.info("New json config from event-bus: " + listener.getNewConfiguration());
					jsonConfig = listener.getNewConfiguration();
					//TODO: redeploy the verticles to reflect the new config changes
					logger.info("TODO: redeploy the verticle");
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
	
	private Future<String> deployVerticle(Vertx vertx, Verticle verticle, DeploymentOptions deploymentOptions) {
		Promise<String> promise = Promise.promise();
		vertx.deployVerticle(verticle, deploymentOptions, completionHandler -> {
			if(completionHandler.succeeded()) {
				promise.complete(completionHandler.result());
			} else {
				promise.fail(completionHandler.cause());
			}
		});
		return promise.future();
	}
	
	private Future<Void> undeployVerticle(Vertx vertx, String deploymentId) {
		Promise<Void> promise = Promise.promise();
		vertx.undeploy(deploymentId, completionHandler -> {
			if(completionHandler.succeeded()) {
				promise.complete(completionHandler.result());				
			} else {
				promise.fail(completionHandler.cause());
			}
		});
		return promise.future();
	}
	
	private Future<Void> configChangeEvent() {
		Promise<Void> promise = Promise.promise();
		return promise.future();
	}
}
