# pnet-idp-client

This library consist of 3 different parts

 1. pnet-idp-client-openidconnect: A spring Security configuration to authenticate against the PHS Identity Provider via OIDC
 2. pnet-idp-client-saml2: A Spring Security configuration to authenticate against the PHS Identity Provider via SAML 2.0
 3. pnet-idp-client-showcase: A web application that uses both of the clients to showcase all the features available when authenticating via the IDP

## Version support

The following table shows the supported version of the library and the Spring Boot Version they support.
Versions not mentioned in this table are not supported anymore

|pnet-idp-client|spring-boot|spring-security|support end|info|
|---------------|-----------|---------------|-----------|----|
|0.5.x          |2.7.x      |5.7.x          |2023-02    |This will be the last version supporting spring-security 5.7.x. spring-security 5.8.x should not have any incopatible changes. Upgrading to 0.9.x should cause no problems.|
|0.9.x          |2.7.x      |5.8.x          |2023-11    |See https://docs.spring.io/spring-security/reference/5.8/migration/index.html to prepare your application for the upcomming spring boot 3 Change. See **ReleaseNotes.md** for a list of changes|
|1.0.x          |3.0.x      |6.0.x          |2023-05    |_comming soon_|

## Run the showcase application

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
