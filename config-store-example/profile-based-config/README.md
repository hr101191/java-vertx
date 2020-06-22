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
Vertx WebClient:
```java
public class WebClientVerticle extends AbstractVerticle {
	
	//Omitted
	
	@Override
	public void start() {
		webClient = WebClient.create(vertx, new WebClientOptions()
				.setSsl(true)
				.setTrustAll(false)
				.setKeyStoreOptions(new JksOptions() //configure keystore
						.setPath("server-b-keystore.jks") //points to src/resources if no qualified path is provided
						.setPassword("11111111")
						)
				.setTrustStoreOptions(new JksOptions() //configure truststore
						.setPath("server-b-truststore.jks") //points to src/resources if no qualified path is provided
						.setPassword("11111111")
						)
				);				
		//Omitted
	}
	//Omitted
}
```
