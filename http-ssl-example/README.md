

### Setup

#### Creating Keystore

Replace the {placeholder} with actual value
keytool -genkey -keyalg {RSA} -keysize {2048} -validity {360000} -alias server -keystore server.jks -storepass 11111111 -ext san:localhost

#### Exporting Certificates

#### Creating Truststore and importing server certificate into Truststore
![Alt text](README_IMG/output_to_copy.PNG?raw=true "output_to_copy")


Scenario:
```
Keystore
|-- GoDaddy Root Certificate
   |-- GoDaddy Intermediate Certificate 1
   |-- GoDaddy Intermediate Certificate 2
   |-- ...
   |-- GoDaddy Intermediate Certificate n
      |-- Your Server Cert Signed by the chain of certificates above

TrustStore
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