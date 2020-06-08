package com.hurui.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	private JsonObject localConfig = new JsonObject();
	private JsonObject jsonConfig = new JsonObject();
	private String demoVerticleDeploymentId = null;
	
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
				logger.error("Failed to created an instance of Infinispan Cluster Manager, stacktrace: " , handler.cause());
				logger.info("Creating a new instance of Infinispan Cluster Manager using the default settings...");
				vertxOptions.setClusterManager(new InfinispanClusterManager());
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
					
					Future<String> redeployFuture = undeployVerticle(demoVerticleDeploymentId)
							.compose(mapper -> deployVerticle(new DemoVerticle(), new DeploymentOptions().setConfig(jsonConfig)));
					redeployFuture.onComplete(handler -> {
						if(handler.succeeded()) {
							logger.info("Successfully redeployed Demo Verticle");
							demoVerticleDeploymentId = handler.result(); //update the deploymentID
						} else {
							logger.error("Failed to undeploy Demo Verticle, stackTrace: ", handler.cause());
						}
					});
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
				ClusterManager clusterManager = new InfinispanClusterManager();
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
	
	private Future<String> deployVerticle(Verticle verticle, DeploymentOptions deploymentOptions) {
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
	
	private Future<Void> undeployVerticle(String deploymentId) {
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
}
