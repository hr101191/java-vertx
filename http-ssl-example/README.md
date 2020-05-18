

### Setup

#### Creating Keystore

Command (Replace the {placeholder} with a valid value):
```
keytool -genkey -keyalg {placeholder} -keysize {placeholder} -validity {placeholder} -alias {placeholder} -keystore {placeholder} -storepass {placeholder} -ext {placeholder}
```
| Keytool Overload | Description | Sample Value |
| ----- | ----- | ----- |
| `-keyalg` | Key Algorithm | RSA |
| `-keysize` | Specifies the number of bits in the modulus during encryption | 1024, 2048 |
| `-validity` | Validity period of the issued certificate | Up to 2 years for [certificates issued after 1st March 2018](https://www.trustzone.com/ssl-certificate-validity-is-now-capped-at-a-maximum-of-2-years/) |
| `-alias` | An alias that identifys this certificate in the keystore, default will be the common name | any name of your preference |
| `-keystore` | Full path of the output keystore (.jks format) | your preferred file path |
| `-storepass` |  Password for this keystore (required) | something you will hopefully remember, god bless! |
| `-ext` |  Extensions for the certificate. Usually Subject Alternate Name (SAN) |  san:localhost\ \*Importance of SAN: Google Chrome will not recognize your certificate if your hostname is found in the list of SAN listed in the certificate. IE and Mozilla works fine.\ \*Also include the hostname(s) of other server(s) that you are hosting your service on |

In this demo, we will create two keystores. One for each service which will communicate via https.



#### Exporting Certificates/ CSR

#### Creating Truststore and importing server certificate into Truststore
![Alt text](README_IMG/output_to_copy.PNG?raw=true "output_to_copy")

Truststore in SSL is where you can configure whom you trust. 
\*Note: An actual server certificate issued by a recognized certificate authority will be created from one root cert and zero to many intermediate certificates. 
This is known as the [certificate chains](https://knowledge.digicert.com/solution/SO16297.html). For a self-signed certificate, this certificate chain is contained in 
itself. 

Enterprise Scenario: 
Your organization's contracted certificate authority is GoDaddy. You are hosting a new rest service and your rest service will call some other Apis:
1. Inhouse Apis who's server certificate is also issued by GoDaddy
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