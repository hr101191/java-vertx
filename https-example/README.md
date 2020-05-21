# Vertx Https Example

## 1. Description
In this demo, we are creating two rest services which will communicate with each other over https protocol.

Both servers will host the same service /api/greeting, When invoked, it will make a https call to the other server on the same endpoint

## 2. Setup

### 2.1. Creating Keystore

Command (Replace the {placeholder} with a valid value):
```
keytool -genkey -keyalg {placeholder} -keysize {placeholder} -validity {placeholder} -alias {placeholder} -keystore {placeholder} -storepass {placeholder} -ext {placeholder}
```
| Keytool Overload | Description | Sample Value |
| ----- | ----- | ----- |
| `-keyalg` | Key Algorithm | RSA |
| `-keysize` | Specifies the number of bits in the modulus during encryption | 1024, 2048 |
| `-validity` | Validity period of the issued certificate | Up to 2 years for [certificates issued after 1st March 2018](https://www.trustzone.com/ssl-certificate-validity-is-now-capped-at-a-maximum-of-2-years/) |
| `-alias` | An alias that identifes this certificate in the keystore, default will be the common name | any name of your preference |
| `-keystore` | Full path of the output keystore (.jks format) | your_preferred_file_path\\keystore_name.jks |
| `-storepass` |  Password for this keystore (required) | something you will hopefully remember, god bless! |
| `-ext` |  Extensions for the certificate. Usually Subject Alternate Name (SAN) |  SAN=dns:localhost |

\*Importance of SAN: Google Chrome will not recognize your certificate if your hostname is found in the list of SAN listed in the certificate. IE and Mozilla works fine. If you are purchasing a certificate from a certificate authority, they might require you to enter this value seperately on their portal.

\*For a cluster setup, include the dns name of all the servers hosting the service along with the dns name of your load balancer
```
SAN=dns:loadbalancer,dns:hostname1,dns:hostname2

When prompted to enter common name, provide the dns name of your load balancer:
What is your first and last name?
  [Unknown]:  loadbalancer
```

In this demo, we will create two keystores. One for each service which will communicate via https.

Generating Keystore for server A:
![Alt text](README_IMG/gen_server_a_keystore.PNG?raw=true "gen_server_a_keystore")

Generating Keystore for server B:
![Alt text](README_IMG/gen_server_b_keystore.PNG?raw=true "gen_server_b_keystore")

### 2.2. Exporting Certificates
\*Note: An actual server certificate issued by a recognized certificate authority will be created from one root cert and zero to many intermediate certificates. 
This is known as the [certificate chains](https://knowledge.digicert.com/solution/SO16297.html). When establishing a TLS conneection, client will validate the server's 
identity based on the root certificate and intermediate certificate(s) that the server certificate is generated from. Skip this section if you are using certificate issued by 
a certificate authority

For a self-signed certificate, this certificate chain is contained in 
itself and therefore we will be acting as the certificate authority ourself. Hence, this step is required if you are using self-signed certificate as clients connecting to your service 
will need your identity solely based on this certificate.

Command (Replace the {placeholder} with a valid value):
```
keytool -export -alias {placeholder} -file {placeholder} -keystore {placeholder} -storepass {placeholder}
```
| Keytool Overload | Description | Sample Value |
| ----- | ----- | ----- |
| `-alias` | An alias that identifes this certificate in the keystore, default will be the common name | An alias in this keystore |
| `-file` | Full path of the output certificate (.cer format) | your_preferred_file_path\\certificate_name.cer |
| `-keystore` | Full path of the output keystore (.jks format) | your_preferred_file_path\\keystore_name.jks |
| `-storepass` |  Password for this keystore (required) | something you will hopefully remember, god bless! |

For this demo, we will execute the following commands:

Export certificate from Server A JKS:
![Alt text](README_IMG/export_server_a_ca.PNG?raw=true "export_server_a_ca")

Export certificate from Server B JKS:
![Alt text](README_IMG/export_server_b_ca.PNG?raw=true "export_server_b_ca")

### 2.3. Creating Truststore and importing server certificate into Truststore
Truststore is where you can configure whom you trust.

Command (Replace the {placeholder} with a valid value):
```
keytool -import -alias {placeholder} -file {placeholder} -keystore {placeholder} -storepass {placeholder}
```
| Keytool Overload | Description | Sample Value |
| ----- | ----- | ----- |
| `-alias` | An alias that identifes this certificate in the keystore, default will be the common name | An alias to be given to this certificate in this truststore|
| `-file` | Full path of the output certificate (.cer format) | your_preferred_file_path\\certificate_name.cer |
| `-keystore` | Full path of the output keystore (.jks format) | your_preferred_file_path\\keystore_name.jks |
| `-storepass` |  Password for this keystore (required) | something you will hopefully remember, god bless! 

#### 2.3.1 Self-signed Certificate
For this demo, your self-signed certificate will contain the whole chain depicted above in ONE certificate. Simply import the client's self-sign certificate into your truststore

Creating truststore for server A:
![Alt text](README_IMG/server_a_truststore.PNG?raw=true "server_a_truststore")

Creating truststore for server B:
![Alt text](README_IMG/server_b_truststore.PNG?raw=true "server_b_truststore")

#### 2.3.2 Certificate issued by certificate authority
Considering the following setup used by an external client which tries to establish TLS connection with your service:
```
|-- GoDaddy Root Certificate
   |-- GoDaddy Intermediate Certificate 1
   |-- GoDaddy Intermediate Certificate 2
   |-- ...
   |-- GoDaddy Intermediate Certificate n
      |-- Client's Cert Signed by the chain of certificates above
```
You will need to import:
1. GoDaddy Root Certificate
2. All GoDaddy intermediate certificate(s)
\*Note: Client's certificate is not required. But it's ok to include (\*Think about self-signed certificate scenario)

#### 2.3.3 Enterprise Scenario
Your organization's contracted certificate authority is GoDaddy. You are hosting a new rest service and your rest service will consumed by other services:
1. Inhouse Apis who's server certificate is also issued by GoDaddy
2. External Apis hosted by vendor A who's server certificate is issued by DigiCert
3. External Apis hosted by vendor B who's server certificate is issued by Entrust

Setup (from your perspective):
```
Keystore:
|-- GoDaddy Root Certificate
   |-- GoDaddy Intermediate Certificate 1
   |-- GoDaddy Intermediate Certificate 2
   |-- ...
   |-- GoDaddy Intermediate Certificate n
      |-- Your Server Cert Signed by the chain of certificates above

Truststore:
|-- GoDaddy Root Certificate
   |-- GoDaddy Intermediate Certificate 1
   |-- GoDaddy Intermediate Certificate 2
   |-- ...
   |-- GoDaddy Intermediate Certificate n
|-- DigiCert Root
   |-- DigiCert Intermediate Certificate 1
   |-- DigiCert Intermediate Certificate 2
   |-- ...
   |-- DigiCert Intermediate Certificate n
|-- Entrust Root
   |-- Entrust Intermediate Certificate 1
   |-- Entrust Intermediate Certificate 2
   |-- ...
   |-- Entrust Intermediate Certificate n
```

Below is the setup that some people adopt, which is to use the keystore as both keystore as well as truststore. I will not discuss if this is introducing an anti-pattern here in this demo 
but i certainly agree that this is very convenient\
Setup - Alternative (from your perspective):
```
Keystore:
|-- GoDaddy Root Certificate
   |-- GoDaddy Intermediate Certificate 1
   |-- GoDaddy Intermediate Certificate 2
   |-- ...
   |-- GoDaddy Intermediate Certificate n
      |-- Your Server Cert Signed by the chain of certificates above
|-- DigiCert Root
   |-- DigiCert Intermediate Certificate 1
   |-- DigiCert Intermediate Certificate 2
   |-- ...
   |-- DigiCert Intermediate Certificate n
|-- Entrust Root
   |-- Entrust Intermediate Certificate 1
   |-- Entrust Intermediate Certificate 2
   |-- ...
   |-- Entrust Intermediate Certificate n

Truststore: Simply point truststore path programmatically to the same path as keystore.
```

## 3. Checkpoint
Here's a quick summary of what we've done so far...

Server A:
1) Created a keystore which identifies itself during a TLS connection
2) Created a truststore which included server B's certificate to verify server B's identity when server B tries to establish TLS connection with server A.

Server B:
1) Created a keystore which identifies itself during a TLS connection
2) Created a truststore which included server B's certificate to verify server B's identity when server B tries to establish TLS connection with server A.

## 4. Code Discussion
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

Vertx HttpServer:
```java
public class HttpServerVerticle extends AbstractVerticle {

	//Omitted
	
	@Override
	public void start() {
		HttpServerOptions options = new HttpServerOptions()
				.setSsl(true) //enable SSL
				.setKeyStoreOptions(new JksOptions() //configure keystore
					.setPath("server-b-keystore.jks") //points to src/resources if no qualified path is provided
					.setPassword("11111111")
				)
				.setTrustStoreOptions(new JksOptions() //configure truststore
					.setPath("server-b-truststore.jks") //points to src/resources if no qualified path is provided
					.setPassword("11111111")
				)
				.addEnabledSecureTransportProtocol("TLSv1.3")
				.addEnabledSecureTransportProtocol("TLSv1.2")
				.addEnabledSecureTransportProtocol("TLSv1.1")
				.addEnabledSecureTransportProtocol("TLSv1.0")
				.setPort(8081);
		//Omitted
	}
	//Omitted
}
```

Other points to note:

\* Even though project used SpringBoot, noticed that the AbstractVerticle classes are not marked with @Component? This is to ensure that the lifecycle of the Verticles are managed by Vertx instead. See code snippet below on how to deploy the verticles:
```java
	//Use @EventListener to ensure that all the required Spring Beans are loaded before starting the Verticles
	@EventListener
	private void dostuff(ApplicationReadyEvent event) {
		//1) Create an instance of Vertx
		Vertx vertx = Vertx.vertx();
		
		//2) Deploy the verticles using this instance of Vertx
		vertx.deployVerticle(new WebClientVerticle(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed WebClientVerticle successfully...");
			} else {
				logger.error("Error deploying WebClientVerticle, stacktrace:", completionHandler.cause());
			}
		});
		vertx.deployVerticle(new GreetingServiceVerticle(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed GreetingServiceVerticle successfully...");
			} else {
				logger.error("Error deploying GreetingServiceVerticle, stacktrace:", completionHandler.cause());
			}
		});
		vertx.deployVerticle(new HttpServerVerticle(), completionHandler -> {
			if (completionHandler.succeeded()) {
				logger.info("Deployed HttpServerVerticle successfully...");
			} else {
				logger.error("Error deploying HttpServerVerticle, stacktrace:", completionHandler.cause());
			}
		});		
	}
```

\* Use @PreDestroy to dispose any Vertx resources
```java
	@PreDestroy
	private void destroy() {
		vertx.close(completionHandler -> {
			if(completionHandler.succeeded()) {
				logger.info("Disposed all Vertx managed resources successfully...");
			}else {
				logger.info("Error disposing Vertx managed resources, stacktrace: ", completionHandler.cause());
			}
		});
		//Proceed to dispose Spring Managed Resources if required
	}
```
## 5. Test