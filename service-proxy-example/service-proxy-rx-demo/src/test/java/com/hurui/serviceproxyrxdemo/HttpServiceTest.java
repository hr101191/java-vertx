package com.hurui.serviceproxyrxdemo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.test.verticles.MockHttpVerticle;
import com.hurui.verticles.GreetingServiceVerticle;
import com.hurui.verticles.HttpVerticle;

import io.vertx.core.Verticle;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

@ExtendWith(VertxExtension.class)
class HttpServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	private static List<String> deploymentIdList = new ArrayList<>();
	
	@BeforeAll
	@Timeout(120000)
	static void init(Vertx vertx, VertxTestContext vertxTestContext) {	
		//pause the thread to complete deployment before executing test case
		Checkpoint deploymentCompletedCheckpoint = vertxTestContext.checkpoint(3); //Set # of passes as 3 because we are deploying 3 verticles here

		logger.info("Running unit tests for endpoints hosted in HttpVerticle");
		//1) Deploy the regular verticles as requried
		Verticle httpVerticle = new HttpVerticle();
		Verticle greetingServiceVerticle = new GreetingServiceVerticle();
		//2) It is easy to mock test verticles that returns a fixed response for testing purpose (This is a simple example)
		// scenario: if your verticles are interacting with a remote api and Http.OK response is not guaranteed
		Verticle mockHttpVerticle = new MockHttpVerticle();
		vertx.rxDeployVerticle(httpVerticle)
			.subscribe(onSuccess -> {
				deploymentIdList.add(onSuccess);
				deploymentCompletedCheckpoint.flag();
			}, onError -> {
				logger.error("Fail to deploy verticle, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
		vertx.rxDeployVerticle(greetingServiceVerticle)
			.subscribe(onSuccess -> {
				deploymentIdList.add(onSuccess);
				deploymentCompletedCheckpoint.flag();
			}, onError -> {
				logger.error("Fail to deploy verticle, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
		vertx.rxDeployVerticle(mockHttpVerticle)
			.subscribe(onSuccess -> {
				deploymentIdList.add(onSuccess);
				deploymentCompletedCheckpoint.flag();
			}, onError -> {
				logger.error("Fail to deploy verticle, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});	
	}
	
	@Order(1) 
	@Test
	@Timeout(120000)
	void testHelloEndpoint (Vertx vertx, VertxTestContext vertxTestContext) {	
		logger.info("Test #1 - Route: http://localhost:8080/api/hello");
		Checkpoint checkpoint1 = vertxTestContext.checkpoint(); //assert that status code = 200
		Checkpoint checkpoint2 = vertxTestContext.checkpoint(); //assert thta message = "Hello"
		WebClient webClient = WebClient.create(vertx);
		webClient.getAbs("http://localhost:8080/api/hello")
			.ssl(false)
			.rxSend()
			.subscribe(onSuccess -> {	
				logger.info("Successfully received a response from remote api. Response body: {}", onSuccess.bodyAsJsonObject());
				assertThat(onSuccess.statusCode()).isEqualTo(200);
				checkpoint1.flag();
				logger.info("Checkpoint 1 passed...");
				assertThat(onSuccess.bodyAsJsonObject().getString("message")).isEqualTo("Hello");
				checkpoint2.flag();
				logger.info("Checkpoint 2 passed...");
				logger.info("Test #1 passed...");
				vertxTestContext.completeNow();
			}, onError -> {
				logger.error("Failed to receive a response from remote api, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
	}
	
	@Order(2) 
	@Test
	@Timeout(120000)
	void testGoodbyeEndpoint (Vertx vertx, VertxTestContext vertxTestContext) {
		logger.info("Test #2 - Route: http://localhost:8080/api/goodbye");
		Checkpoint checkpoint1 = vertxTestContext.checkpoint(); //assert that status code = 200
		Checkpoint checkpoint2 = vertxTestContext.checkpoint(); //assert thta message = "Hello"
		WebClient webClient = WebClient.create(vertx);
		webClient.getAbs("http://localhost:8080/api/goodbye")
			.ssl(false)
			.rxSend()
			.subscribe(onSuccess -> {	
				logger.info("Successfully received a response from remote api. Response body: {}", onSuccess.bodyAsJsonObject());
				assertThat(onSuccess.statusCode()).isEqualTo(200);
				checkpoint1.flag();
				logger.info("Test #2 Checkpoint 1 passed...");
				assertThat(onSuccess.bodyAsJsonObject().getString("message")).isEqualTo("Goodbye");
				checkpoint2.flag();
				logger.info("Test #2 Checkpoint 2 passed...");
				logger.info("Test #2 passed...");
				vertxTestContext.completeNow();
			}, onError -> {
				logger.error("Failed to receive a response from remote api, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
	}
	
	@Order(3) 
	@Test
	@Timeout(120000)
	void testMockHelloEndpoint (Vertx vertx, VertxTestContext vertxTestContext) {
		logger.info("Test #3 - Route: http://localhost:11111/api/hello");
		Checkpoint checkpoint1 = vertxTestContext.checkpoint(); //assert that status code = 200
		Checkpoint checkpoint2 = vertxTestContext.checkpoint(); //assert thta message = "Hello"
		WebClient webClient = WebClient.create(vertx);
		webClient.getAbs("http://localhost:11111/api/mock/hello")
			.ssl(false)
			.rxSend()
			.subscribe(onSuccess -> {	
				logger.info("Successfully received a response from remote api. Response body: {}", onSuccess.bodyAsJsonObject());
				assertThat(onSuccess.statusCode()).isEqualTo(200);
				checkpoint1.flag();
				logger.info("Test #3 Checkpoint 1 passed...");
				assertThat(onSuccess.bodyAsJsonObject().getString("message")).isEqualTo("Hello");
				checkpoint2.flag();
				logger.info("Test #3 Checkpoint 2 passed...");
				logger.info("Test #3 passed...");
				vertxTestContext.completeNow();
			}, onError -> {
				logger.error("Failed to receive a response from remote api, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
	}
	
	@Order(4)
	@Test
	@Timeout(120000)
	void testMockGoodbyeEndpoint (Vertx vertx, VertxTestContext vertxTestContext) {
		logger.info("Test #4 - Route: http://localhost:11111/api/mock/goodbye");
		Checkpoint checkpoint1 = vertxTestContext.checkpoint(); //assert that status code = 200
		Checkpoint checkpoint2 = vertxTestContext.checkpoint(); //assert thta message = "Hello"
		WebClient webClient = WebClient.create(vertx);
		webClient.getAbs("http://localhost:11111/api/mock/goodbye")
			.ssl(false)
			.rxSend()
			.subscribe(onSuccess -> {	
				logger.info("Successfully received a response from remote api. Response body: {}", onSuccess.bodyAsJsonObject());
				assertThat(onSuccess.statusCode()).isEqualTo(200);
				checkpoint1.flag();
				logger.info("Test #2 Checkpoint 1 passed...");
				assertThat(onSuccess.bodyAsJsonObject().getString("message")).isEqualTo("Goodbye");
				checkpoint2.flag();
				logger.info("Test #2 Checkpoint 2 passed...");
				logger.info("Test #2 passed...");
				vertxTestContext.completeNow();
			}, onError -> {
				logger.error("Failed to receive a response from remote api, stacktrace: ", onError);
				vertxTestContext.failNow(onError);
			});
	}

	@AfterAll
	@Timeout(120000)
	static void tearDown(Vertx vertx, VertxTestContext vertxTestContext) {
		//Set # of passes as 3 because we are undeploying 3 verticles here
		Checkpoint deploymentCompletedCheckpoint = vertxTestContext.checkpoint(3); 
		for(String deploymentId : deploymentIdList) {
			vertx.rxUndeploy(deploymentId).subscribe(() -> {
				logger.info("Undeploy verticle successfully... Deployment ID: [{}]", deploymentId);
				deploymentCompletedCheckpoint.flag(); //flag once as checkpoint is past successfully
			}, onError -> {
				logger.warn("Failed to undeploy verticle.. Deployment ID: [{}]", deploymentId);
				logger.error("stacktrace:", onError);
				vertxTestContext.failNow(onError);
			});
		}
		vertx.rxClose()
			.doOnComplete(() -> {
				logger.info("Successfully closed vertx instance");
			})
			.doOnError(onError -> {
				logger.error("Failed to close vertx instance, stacktrace:", onError);
				vertxTestContext.failNow(onError);
			});
	}
}
