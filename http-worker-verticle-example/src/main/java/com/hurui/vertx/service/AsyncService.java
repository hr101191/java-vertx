package com.hurui.vertx.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface AsyncService {
	
	public Future<Boolean> asyncServiceJavaConcurrent(ExecutorService executor);
	
}
