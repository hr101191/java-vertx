package com.hurui.hazelcastconfigstoreserver;

import com.hurui.verticles.MainVerticle;

import io.vertx.core.Launcher;

public class HazelcastConfigstoreServerApplication {

	public static void main(String[] args) {		
		Launcher.executeCommand("run", MainVerticle.class.getName());
	}
}
