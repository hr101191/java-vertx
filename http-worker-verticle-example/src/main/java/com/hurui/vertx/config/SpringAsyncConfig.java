package com.hurui.vertx.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAsyncConfig {
	@Bean
	public ExecutorService asyncSingleThreadExecutor(){
		ExecutorService executor = Executors.newSingleThreadExecutor();
		return executor;
	}
	
	@Bean
	public ExecutorService asyncMultiThreadExecutor(){
		ExecutorService executor = Executors.newFixedThreadPool(5);
		return executor;
	}
}
