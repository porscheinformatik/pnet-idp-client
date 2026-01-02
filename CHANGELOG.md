# Changelog

## [1.4.0]

-   Refactored some toString methods
-   Updated to Spring Boot 3.5.9 and others
-   Removed deprecated methods and fields
-   Added a CHANGELOG.md to document changes and updates in the project.

## [1.3.1]

-   Add BDO-ID to OIDC and SAML2 principals

## [1.3.0]

-   Update Spring version

## [1.2.5]

-   Add authentication provider PHS_ENTRA_ID_CF

## [1.2.4]

-   Add tax number for persons in Italy

## [1.2.3]

-   Add preselect_tenant param to UI
-   Reformat using prettier

## [1.2.2]

-   Add login_hint to SAML2 Request
-   Add prompt to SAML2 Request

## [1.2.1]

-   Add Auth Provider to auth principles
-   Add prompt and login_hint to parameters
-   Fix description of prompt=none
-   Fix invalid check for prompt parameter
-   Verify max_age_mfa in showcase
-   Display login_hint in showcase
-   Renew certificate for local development
-   Ignore .vscode
-   Rename assembly for showcase

## [1.2.0]

-   Fix SonarQube Issues (multiple commits)

## [1.2.0-RC1]

-   Update Spring Boot to 3.4.0
-   Fix test after Spring upgrade

## [1.1.2]

-   Fix URLs for Partner.Net DEV environment

## [1.1.1]

-   Fix NPE in PartnerNetUserType

## [1.1.0]

-   Add MaxAgeMfa param to SAML 2
-   Add max_age_mfa param to OIDC
-   Add missing types to PartnerNetUserType
-   Using Prettier to format source
-   Refactor showcase applications to include all optional params
-   Upgrade spring boot to 3.3.5
-   Fix deprecation warnings in ClientShowcaseSecurityConfig
-   Fix USER_INFO_USER_TYPE claim name
-   Upgrade to Spring Boot 3.3.2
-   Fix parsing for UserType
-   Add responsible user information
-   Fix a lot of warnings
-   Add UserType to authentication data

## [1.0.2]

-   Add git commit hash label to deployments and pods (#8)

## [1.0.1]

-   Switch from OC DeploymentConfig to K8s Deployment (#7)

## [1.0.0]

Initial release with full OpenID Connect and SAML 2.0 support for Partner.Net Identity Provider.

-   OpenID Connect authentication support
-   SAML 2.0 authentication support
-   Support for max_age parameter
-   Support for max_age_mfa parameter
-   Support for authentication context classes
-   Support for tenant selection
-   Support for custom state handling
-   ForceAuthn flag support
-   Relay state CSRF protection
-   Metadata filter for SAML
-   Authorities mapper interface
-   Showcase application with examples
-   Upgraded to Spring Boot 3.x
-   Upgraded to Java 17
-   Migrated from WebSecurityConfigurerAdapter to SecurityFilterChain
