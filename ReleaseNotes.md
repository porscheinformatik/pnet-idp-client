# Release Notes

## 0.9.0

 - Upgraded to Spring Security 5.8.x that supports new Spring Security 6.0 features and is still fully Spring Boot 2.7 compatible.
 - Fixed NPE in the Metadata Filter when a illegal registration id was given
 - Added missing properties to `at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser` and `at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationPrincipal`

### Breaking Changes

 - Set the OpenSAML Baseline to 4.x. In the unlikely case, that your application directly uses OpenSAML, you have to upgrade to 4.3.x or greater.
 - In the unlikely case you used `at.porscheinformatik.idp.saml2.XmlUtils.getXmlValue(XMLObject)` directly, it will return a `java.time.Instant` intead of a `org.jodatime.DateTime` for XSDateTime objects.
 - In case you use the getters in the `at.porscheinformatik.idp.saml2.Saml2Utils` directly, they return Optionals now. The easiest thing to do will be to use `Saml2Utils.getXXX().orElse(null);` to keep the same behaviour as before