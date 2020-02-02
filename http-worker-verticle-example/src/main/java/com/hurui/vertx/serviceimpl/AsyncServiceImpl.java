package com.hurui.vertx.serviceimpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hurui.vertx.service.AsyncService;

@Service
public class AsyncServiceImpl implements AsyncService {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	public Future<Boolean> asyncServiceJavaConcurrent(ExecutorService executor){
		logger.info("Execute method asynchronously - " + Thread.currentThread().getName());
		return executor.submit(() -> {
            Thread.sleep(1000);
            logger.info("Completed execution of method asynchronously - " + Thread.currentThread().getName());
            return true; //Sample method which always returns true
        });        
	}
}
