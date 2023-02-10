# Release Notes

## 1.0.0

This release makes the pnet-idp-client Spring Boot 3 compatible. It will drop support for Releases prior to Spring Boot 3.0.0.

### Breaking Changes

- Upgrade to Spring Boot 3. See https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide for a guide on how to migrate.
- Set the java baseline to Java 17
- Remove deprecated `at.porscheinformatik.idp.saml2.PartnerNetSaml2Configurer.apply(...)` methods. Use the methods without the boolean flag.
- `at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser.getAdditionalLocales()` returns a list of locales instead of a list of strings. If you need the old behaviour, call Locale::toLanguageTag on each entry in the list.
## 0.9.0

- Upgraded to Spring Security 5.8.x that supports new Spring Security 6.0 features and is still fully Spring Boot 2.7 compatible.
- Fixed NPE in the Metadata Filter when a illegal registration id was given
- Added missing properties to `at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser` and `at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationPrincipal`

### Breaking Changes

- Set the OpenSAML Baseline to 4.x. In the unlikely case, that your application directly uses OpenSAML, you have to upgrade to 4.3.x or greater.
- In the unlikely case you used `at.porscheinformatik.idp.saml2.XmlUtils.getXmlValue(XMLObject)` directly, it will return a `java.time.Instant` intead of a `org.jodatime.DateTime` for XSDateTime objects.
- In case you use the getters in the `at.porscheinformatik.idp.saml2.Saml2Utils` directly, they return Optionals now. The easiest thing to do will be to use `Saml2Utils.getXXX().orElse(null);` to keep the same behaviour as before
