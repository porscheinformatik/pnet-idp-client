/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;
import static java.lang.String.*;
import static java.util.Objects.*;

import jakarta.servlet.Filter;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.saml2.Saml2LoginConfigurer;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver.AuthnRequestContext;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetSaml2Configurer extends AbstractHttpConfigurer<PartnerNetSaml2Configurer, HttpSecurity> {

    /**
     * @param http the http security to configure
     * @param provider the authentication provider to use.
     * @return the configurer for further customization
     * @throws Exception when an exception occurs while configuring
     */
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, PartnerNetSaml2Provider provider)
        throws Exception {
        return apply(http, provider.getEntityId(), provider.getEntityId());
    }

    /**
     * @param http the http security to configure
     * @param entityId the entity id of the identity provider to use
     * @return the configurer for further customization
     * @throws Exception on occasion
     */
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, String entityId) throws Exception {
        return apply(http, entityId, entityId);
    }

    /**
     * @param http the http security to configure
     * @param entityId the entity id of the identity provider to use
     * @param metadataUrl the URL pointing to the identity providers metadata
     * @return the configurer for further customization
     * @throws Exception when an exception occurs while configuring
     */
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, String entityId, String metadataUrl)
        throws Exception {
        http //
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET, DEFAULT_ENTITY_ID_PATH)
            .permitAll();

        return http.apply(new PartnerNetSaml2Configurer(entityId, metadataUrl));
    }

    private static final String DEFAULT_REGISTRATION_ID = "pnet";
    private static final String DEFAULT_LOGIN_PROCESSING_URL = "/saml2/sso/post/{registrationId}";
    private static final String DEFAULT_ENTITY_ID_PATH = "/saml2/{registrationId}";

    private final String entityId;
    private final String metadataUrl;

    private boolean failOnStartup;
    private HttpClientFactory clientFactory = HttpClientFactory.defaultClient();
    private Saml2CredentialsManager credentialsManager;
    private Saml2ResponseProcessor responseProcessor;
    private Saml2ResponseParser responseParser;
    private PartnerNetSaml2AuthoritiesMapper authoritiesMapper;
    private Consumer<AuthnRequestContext> authnRequestCustomizer;
    private AuthenticationFailureHandler failureHandler;
    private String failureUrl;
    private AuthenticationSuccessHandler successHandler;

    private RelyingPartyRegistrationResolver relyingPartyResolver;
    private Customizer<Saml2LoginConfigurer<HttpSecurity>> customizer = saml2Login -> {
        // Noop customizer. Users can override this to add custom configurations
    };

    private PartnerNetSaml2Configurer(String entityId, String metadataUrl) {
        super();
        this.entityId = entityId;
        this.metadataUrl = metadataUrl;
    }

    /**
     * When called, that application will fail to start, when the metadata of the provider could not be loaded.
     * Otherwise it will gracefully start, and try to load the metadata until the metadata could be fetched.
     *
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer failOnStartup() {
        failOnStartup = true;

        return this;
    }

    /**
     * Set the credentials manager to use for loading the credentials.
     *
     * @param credentialsManager the credentials manager to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer credentials(Saml2CredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;

        return this;
    }

    /**
     * Override the default client factory to be used for loading SAML metadata
     *
     * @param clientFactory the client factory to use
     * @return the builder for a fluent api
     * @see HttpClientFactory#defaultClient()
     */
    public PartnerNetSaml2Configurer clientFactory(HttpClientFactory clientFactory) {
        this.clientFactory = clientFactory;

        return this;
    }

    /**
     * Override the default response processor
     *
     * @param responseProcessor the response processor to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer responseProcessor(Saml2ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;

        return this;
    }

    /**
     * Override the default response parser.
     *
     * @param responseParser the response parser to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer responseParser(Saml2ResponseParser responseParser) {
        this.responseParser = responseParser;

        return this;
    }

    /**
     * Override the default authorities mapper. It will only be used if the default {@link #responseParser} is used,
     * otherwise this value will be ignored.
     *
     * @param authoritiesMapper the new authorities mapper to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer authoritiesMapper(PartnerNetSaml2AuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;

        return this;
    }

    /**
     * Override the default {@link AuthenticationSuccessHandler} with a custom implementation. The default handler is
     * based on the {@link SavedRequestAwareAuthenticationSuccessHandler} and sanitizes the redirectUrl to strip off all
     * SAML Processing related query parameters.
     *
     * @param successHandler the {@link AuthenticationSuccessHandler} to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer successHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;

        return this;
    }

    /**
     * Add a customizer that allows you to further customize the Spring Securities {@link Saml2LoginConfigurer}. This is
     * equivalent to calling {@link HttpSecurity#saml2Login(Customizer)} with the advantage of having the default
     * Partner.Net configuration applied. This customizer is called at the very end of the Partner.Net specific
     * configuration. So you can override configurations applied by the Partner.Net configurer.
     *
     * @param customizer the customizer to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer customizer(Customizer<Saml2LoginConfigurer<HttpSecurity>> customizer) {
        this.customizer = Objects.requireNonNull(customizer, "Customizer must not be null");

        return this;
    }

    /**
     * Adds a customizer that allows you to further customize the {@link AuthnRequestContext}. This is necessary, if
     * values like maxSessionAge, the tenant or the nistLevel aren't provided by request parameters.
     *
     * @param authnRequestCustomizer the request customizer
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer authnRequestCustomizer(Consumer<AuthnRequestContext> authnRequestCustomizer) {
        this.authnRequestCustomizer = authnRequestCustomizer;

        return this;
    }

    public PartnerNetSaml2Configurer failureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;

        return this;
    }

    public PartnerNetSaml2Configurer failureUrl(String failureUrl) {
        this.failureUrl = failureUrl;

        return this;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {
        Saml2CredentialsManager credManager = getCredentialsManager();
        RelyingPartyRegistrationRepository relyingPartyRegistrationRepository = getRelyingPartyRegistrationRepository(
            credManager
        );
        relyingPartyResolver = new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);

        builder.authenticationProvider(buildAuthenticationProvider());

        builder.saml2Login(saml2Login -> {
            saml2Login.relyingPartyRegistrationRepository(relyingPartyRegistrationRepository);
            saml2Login.authenticationDetailsSource(new HttpRequestContextAwareSaml2AuthenticationDetailsSource());
            saml2Login.authenticationRequestResolver(buildRequestResolver(relyingPartyResolver));

            saml2Login.loginProcessingUrl(DEFAULT_LOGIN_PROCESSING_URL);
            saml2Login.successHandler(getSuccessHandler());

            if (failureHandler != null) {
                saml2Login.failureHandler(failureHandler);
            } else if (failureUrl != null) {
                saml2Login.failureUrl(failureUrl);
            }

            customizer.customize(saml2Login);
        });
    }

    private AuthenticationSuccessHandler getSuccessHandler() {
        if (successHandler != null) {
            return successHandler;
        }

        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setRedirectStrategy(new Saml2UrlSanitizingRedirectStrategy());

        return handler;
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        builder.addFilterBefore(buildMetadataFilter(), Saml2WebSsoAuthenticationFilter.class);

        builder.saml2Login(c -> c.authenticationManager(builder.getSharedObject(AuthenticationManager.class)));
    }

    private Filter buildMetadataFilter() {
        Saml2MetadataResolver metadataResolver = new PartnerNetSaml2MetadataResolver();
        return new Saml2ServiceProviderMetadataFilter(DEFAULT_ENTITY_ID_PATH, relyingPartyResolver, metadataResolver);
    }

    private AuthenticationProvider buildAuthenticationProvider() {
        Saml2ResponseProcessor currentResponseProcessor = getResponseProcessor();
        Saml2ResponseParser parser = getResponseParser();

        return postProcess(new PartnerNetSamlAuthenticationProvider(currentResponseProcessor, parser));
    }

    private Saml2ResponseParser getResponseParser() {
        return requireNonNullElseGet(responseParser, () -> new PartnerNetSaml2ResponseParser(getAuthoritiesMapper()));
    }

    private PartnerNetSaml2AuthoritiesMapper getAuthoritiesMapper() {
        return requireNonNullElseGet(authoritiesMapper, PartnerNetSaml2AuthoritiesMapper::defaultInstance);
    }

    public Consumer<AuthnRequestContext> getAuthnRequestCustomizer() {
        return requireNonNullElseGet(authnRequestCustomizer, PartnerNetSaml2AuthnRequestCustomizer::new);
    }

    private Saml2ResponseProcessor getResponseProcessor() {
        return requireNonNullElseGet(responseProcessor, Saml2ResponseProcessor::withDefaultHandlers);
    }

    private Saml2CredentialsManager getCredentialsManager() {
        return requireNonNull(credentialsManager, "No credentials configured");
    }

    private RelyingPartyRegistrationRepository getRelyingPartyRegistrationRepository(
        Saml2CredentialsManager credManager
    ) {
        ReloadingRelyingPartyRegistrationRepository repository = new ReloadingRelyingPartyRegistrationRepository(
            DEFAULT_REGISTRATION_ID,
            entityId,
            metadataUrl,
            credManager,
            clientFactory,
            DEFAULT_LOGIN_PROCESSING_URL,
            DEFAULT_ENTITY_ID_PATH
        );

        if (failOnStartup) {
            requireNonNull(
                repository.findByRegistrationId(DEFAULT_REGISTRATION_ID),
                format("No RelyingPartyRegistration for metadata %s found", metadataUrl)
            );
        }

        return repository;
    }

    private Saml2AuthenticationRequestResolver buildRequestResolver(
        RelyingPartyRegistrationResolver relyingPartyRegistrationResolver
    ) {
        OpenSaml4AuthenticationRequestResolver resolver = new OpenSaml4AuthenticationRequestResolver(
            relyingPartyRegistrationResolver
        );

        resolver.setAuthnRequestCustomizer(getAuthnRequestCustomizer());
        resolver.setRelayStateResolver(request ->
            Saml2Utils.getRelayState(request) //
                .map(relayState -> String.format(AUTO_GENERATED_RELAY_STATE_FORMAT, UUID.randomUUID(), relayState)) // pre-append a random string
                .orElseGet(() -> String.format(AUTO_GENERATED_RELAY_STATE_FORMAT, UUID.randomUUID(), ""))
        ); // default to the auto generated UUID;

        return resolver;
    }
}
