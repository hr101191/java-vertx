

### Setup

#### Creating Keystore

Replace the {placeholder} with actual value
keytool -genkey -keyalg {RSA} -keysize {2048} -validity {360000} -alias server -keystore server.jks -storepass 11111111 -ext san:localhost

**Importance of SAN: Google chrome will not recognize your certificate if your hostname is found in the list of SAN listed in the certificate. IE and Mozilla works fine.

#### Exporting Certificates

#### Creating Truststore and importing server certificate into Truststore
![Alt text](README_IMG/output_to_copy.PNG?raw=true "output_to_copy")


Enterprise Scenario: 
You are hosting a new rest service and your rest service will call some other Apis:
1. Inhouse Apis who's server certificate is issued by DigiCert
2. External Apis hosted by vendor A who's server certificate is issued by DigiCert
3. External Apis hosted by vendor B who's server certificate is issued by Entrust

Setup:
```
Keystore:
|-- GoDaddy Root Certificate
   |-- GoDaddy Intermediate Certificate 1
   |-- GoDaddy Intermediate Certificate 2
   |-- ...
   |-- GoDaddy Intermediate Certificate n
      |-- Your Server Cert Signed by the chain of certificates above

TrustStore:
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