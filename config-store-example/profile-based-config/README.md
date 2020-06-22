# Profile Based Config Example

## 1. Description
In this demo, we are replicating the look and feel of the Springboot profile based configuration with Vert.x shell

Both servers will host the same service /api/greeting, When invoked, it will make a https call to the other server on the same endpoint

## 2. Command Line

*Only -conf

Program Arguments:
```
-conf "{\"\activeProfile" : \"prod\"}"
```

Program Arguments (running as a fat-jar):
```
```

Program Arguments (running as a fat-jar):
```
```

## 3. Code Discussion
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
