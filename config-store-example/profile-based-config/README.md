# Profile Based Config Example

## 1. Description
In this demo, we are replicating the look and feel of the Springboot profile based configuration with Vert.x shell

## 2. Command Line

*Only -conf parameter is discussed here. 

Program Arguments:
```
-conf "{\"\activeProfile" : \"prod\"}"
```

Get json config from command line:
```java
public class MainVerticle extends AbstractVerticle {
	
	@Override
	public void start() {
		JsonObject commandLineConfig = vertx.getOrCreateContext().config();
		//Implement custom logic to process value from json
	}
}
```
## 3. Code Discussion

(1) Check if -conf parameter is passed from command line. Load default config from classpath is -conf is not passed:
```java
ConfigStoreOptions configStoreOptions = new ConfigStoreOptions();

if(vertx.getOrCreateContext().config().isEmpty()) {
	logger.warn("-conf parameter is not passed via command line! Loading application-local-config from classpath...");	
	configStoreOptions.setType("file")
		.setConfig(new JsonObject()
		.put("path", "src/main/resources/application-local-config.json"));
} else {
	//see code snippet at (2)
}
```

(2) Implement custom logic to load the profile based config from classpath
```java
ConfigStoreOptions configStoreOptions = new ConfigStoreOptions();

JsonObject commandLineConfig = vertx.getOrCreateContext().config();

if(commandLineConfig.containsKey("activeProfile")) {
	String profile = commandLineConfig.getString("activeProfile");
	if (profile.equalsIgnoreCase("prod")) {
		logger.info("Active profile: [{}]", profile);	
		configStoreOptions.setType("file")
			.setConfig(new JsonObject()
			.put("path", "src/main/resources/application-prod-config.json"));
	} else if (profile.equalsIgnoreCase("uat")) {
		logger.info("Active profile: [{}]", profile);
		configStoreOptions.setType("file")
			.setConfig(new JsonObject()
			.put("path", "src/main/resources/application-uat-config.json"));
	} else {
		logger.warn("Invalid profile: [{}] Loading application-local-config from classpath...", profile);
		configStoreOptions.setType("file")
			.setConfig(new JsonObject()
			.put("path", "src/main/resources/application-local-config.json"));
	}
}
```

You may also pass in additional parameter(s) in the json string from command line as required by your business logic:

Example:
Program Arguments:
```
-conf "{\"\activeProfile" : \"prod\", \"instanceRef\" : 1}"
```

Get json config from command line:
```java
public class MainVerticle extends AbstractVerticle {
	
	@Override
	public void start() {
		JsonObject commandLineConfig = vertx.getOrCreateContext().config();
		//Implement custom logic to process value from json
		if(commandLineConfig.containsKey("instanceRef")){
			//Add your own code here
		}
	}
}
```

Full Code Snippet:
```java
public class MainVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	private JsonObject localConfig = new JsonObject();
	
	@Override
	public void start() {
		ConfigStoreOptions configStoreOptions = new ConfigStoreOptions();
		
		if(vertx.getOrCreateContext().config().isEmpty()) {
			logger.warn("-conf parameter is not passed via command line! Loading application-local-config from classpath...");	
			configStoreOptions.setType("file")
				.setConfig(new JsonObject()
				.put("path", "src/main/resources/application-local-config.json"));
		} else {
			JsonObject commandLineConfig = vertx.getOrCreateContext().config();
			
			if(commandLineConfig.containsKey("activeProfile")) {
				String profile = commandLineConfig.getString("activeProfile");
				if (profile.equalsIgnoreCase("prod")) {
					logger.info("Active profile: [{}]", profile);	
					configStoreOptions.setType("file")
						.setConfig(new JsonObject()
						.put("path", "src/main/resources/application-prod-config.json"));
				} else if (profile.equalsIgnoreCase("uat")) {
					logger.info("Active profile: [{}]", profile);
					configStoreOptions.setType("file")
						.setConfig(new JsonObject()
						.put("path", "src/main/resources/application-uat-config.json"));
				} else {
					logger.warn("Invalid profile: [{}] Loading application-local-config from classpath...", profile);
					configStoreOptions.setType("file")
						.setConfig(new JsonObject()
						.put("path", "src/main/resources/application-local-config.json"));
				}
			} else {
				logger.info("Loading command line -conf parameter as resource... Command line Config: {}", commandLineConfig);
				configStoreOptions.setType("json")
					.setConfig(commandLineConfig);
			}
		}
		
		//Omitted other business logic
	}
}
```
