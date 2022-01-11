# pnet-idp-client

This library consist of 3 different parts

 1. pnet-idp-client-openidconnect: A spring Security configuration to authenticate against the PHS Identity Provider via OIDC
 2. pnet-idp-client-saml2: A Spring Security configuration to authenticate against the PHS Identity Provider via SAML 2.0
 3. pnet-idp-client-showcase: A web application that uses both of the clients to showcase all the features available when authenticating via the IDP
 
# Run the showcase application

To run the showcase application you have to build it first

`mvn clean install`

Then go to the `target` folder and run

```
java -Dspring.profiles.active=<environment>\
 -Dpoi.saml2.credentials[0].location=file:/path/to/keystore\
 -Dpoi.saml2.credentials[0].type=PKCS12\
 -Dpoi.saml2.credentials[0].password=<keystore_password>\
 -Dpoi.saml2.credentials[0].privateAlias=<private_key_alias>\
 -Dpoi.saml2.credentials[0].publicAlias=<public_key_alias>\
 -Dpoi.saml2.credentials[0].usage=DECRYPTION\
 -Doidc.client.id=<client_id>\
 -Doidc.client.secret=<client_secret>\
 -jar pnet-idp-client-showcase-<version>.jar
```

environment: one of `local`, `dev`, `qa`, `prod`. Depending on what IDP you want to use

The keystore needs to contain a certificate that is also configured in the IDP environment you are using.

# Version compatibility

|pnet-idp-client|spring-security|
|---------------|---------------|
|0.0.x          |5.4.x          |
|0.1.x          |5.6.x          |
