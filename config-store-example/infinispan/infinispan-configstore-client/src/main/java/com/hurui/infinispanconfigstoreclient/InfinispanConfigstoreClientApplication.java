package com.hurui.infinispanconfigstoreclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.verticles.MainVerticle;

import io.vertx.core.Launcher;

public class InfinispanConfigstoreClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	public static void main(String[] args) {
		logger.info("Initializing Vertx Command Launcher...");
		String[] newArgs = new String[args.length + 1];
	    newArgs[0] = MainVerticle.class.getName();
	    System.arraycopy(args, 0, newArgs, 1, args.length);
	    
	    logger.info("Executing the program with the following command line arguements:");	    
	    for(String str : newArgs) {
	    	logger.info("args: " + str);
	    }
	    logger.info("Deploying Main Verticle...");
		Launcher.executeCommand("run", newArgs);
	}

}
