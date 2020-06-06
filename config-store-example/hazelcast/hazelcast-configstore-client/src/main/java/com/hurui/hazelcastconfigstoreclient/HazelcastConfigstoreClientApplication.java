package com.hurui.hazelcastconfigstoreclient;

import com.hurui.verticles.MainVerticle;

import io.vertx.core.Launcher;

public class HazelcastConfigstoreClientApplication {
	
	public static void main(String[] args) {
		Launcher.executeCommand("run", MainVerticle.class.getName());
	}
}
