/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;
import static java.lang.String.*;
import static java.util.Objects.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.Filter;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.saml2.Saml2LoginConfigurer;
import org.springframework.security.core.GrantedAuthority;
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

import at.porscheinformatik.idp.saml2.DefaultSaml2CredentialsManager.Saml2CredentialsConfig;
import at.porscheinformatik.idp.saml2.Saml2ResponseParserBase.Saml2Data;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetSaml2Configurer extends AbstractHttpConfigurer<PartnerNetSaml2Configurer, HttpSecurity>
{
    /**
     * @param http the http security to configure
     * @param provider the authentication provider to use.
     * @param useAuthorizeHttpRequests Spring implemented a new, easier way to do access control
     *            {@link HttpSecurity#authorizeHttpRequests()}. Now two methods
     *            {@link HttpSecurity#authorizeHttpRequests()} and {@link HttpSecurity#authorizeRequests()} exist. They
     *            can not be mixed in a single {@link HttpSecurity} configuration. If this flag is true,
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used. Otherwise
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used.
     * @return the configurer for further customization
     * @throws Exception when an exception occurs while configuring
     */
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, PartnerNetSaml2Provider provider) throws Exception
    {
        return apply(http, provider.getEntityId(), provider.getEntityId(), true);
    }

    /**
     * @param http the http security to configure
     * @param entityId the entity id of the identity provider to use
     * @param useAuthorizeHttpRequests Spring implemented a new, easier way to do access control
     *            {@link HttpSecurity#authorizeHttpRequests()}. Now two methods
     *            {@link HttpSecurity#authorizeHttpRequests()} and {@link HttpSecurity#authorizeRequests()} exist. They
     *            can not be mixed in a single {@link HttpSecurity} configuration. If this flag is true,
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used. Otherwise
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used.
     * @return the configurer for further customization
     * @throws Exception on occasion
     */
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, String entityId) throws Exception
    {
        return apply(http, entityId, entityId, true);
    }

    /**
     * @param http the http security to configure
     * @param entityId the entity id of the identity provider to use
     * @param metadataUrl the URL pointing to the identity providers metadata
     * @param useAuthorizeHttpRequests Spring implemented a new, easier way to do access control
     *            {@link HttpSecurity#authorizeHttpRequests()}. Now two methods
     *            {@link HttpSecurity#authorizeHttpRequests()} and {@link HttpSecurity#authorizeRequests()} exist. They
     *            can not be mixed in a single {@link HttpSecurity} configuration. If this flag is true,
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used. Otherwise
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used.
     * @return the configurer for further customization
     * @throws Exception when an exception occurs while configuring
     */
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, String entityId, String metadataUrl)
        throws Exception
    {
        return apply(http, entityId, metadataUrl, true);
    }

    /**
     * @param http the http security to configure
     * @param provider the authentication provider to use.
     * @param useAuthorizeHttpRequests Spring implemented a new, easier way to do access control
     *            {@link HttpSecurity#authorizeHttpRequests()}. Now two methods
     *            {@link HttpSecurity#authorizeHttpRequests()} and {@link HttpSecurity#authorizeRequests()} exist. They
     *            can not be mixed in a single {@link HttpSecurity} configuration. If this flag is true,
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used. Otherwise
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used.
     * @return the configurer for further customization
     * @throws Exception when an exception occurs while configuring
     * @deprecated will be removed in 1.0.0. Migrate your applications to new request matchers.
     *             https://docs.spring.io/spring-security/reference/5.8/migration/servlet/config.html#use-new-requestmatchers
     */
    @Deprecated
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, PartnerNetSaml2Provider provider,
        boolean useAuthorizeHttpRequests) throws Exception
    {
        return apply(http, provider.getEntityId(), useAuthorizeHttpRequests);
    }

    /**
     * @param http the http security to configure
     * @param entityId the entity id of the identity provider to use
     * @param useAuthorizeHttpRequests Spring implemented a new, easier way to do access control
     *            {@link HttpSecurity#authorizeHttpRequests()}. Now two methods
     *            {@link HttpSecurity#authorizeHttpRequests()} and {@link HttpSecurity#authorizeRequests()} exist. They
     *            can not be mixed in a single {@link HttpSecurity} configuration. If this flag is true,
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used. Otherwise
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used.
     * @return the configurer for further customization
     * @throws Exception when an exception occurs while configuring * @deprecated will be removed in 1.0.0. Migrate your
     *             applications to new request matchers.
     *             https://docs.spring.io/spring-security/reference/5.8/migration/servlet/config.html#use-new-requestmatchers
     */
    @Deprecated
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, String entityId, boolean useAuthorizeHttpRequests)
        throws Exception
    {
        return apply(http, entityId, entityId, useAuthorizeHttpRequests);
    }

    /**
     * @param http the http security to configure
     * @param entityId the entity id of the identity provider to use
     * @param metadataUrl the URL pointing to the identity providers metadata
     * @param useAuthorizeHttpRequests Spring implemented a new, easier way to do access control
     *            {@link HttpSecurity#authorizeHttpRequests()}. Now two methods
     *            {@link HttpSecurity#authorizeHttpRequests()} and {@link HttpSecurity#authorizeRequests()} exist. They
     *            can not be mixed in a single {@link HttpSecurity} configuration. If this flag is true,
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used. Otherwise
     *            {@link HttpSecurity#authorizeHttpRequests()} will be used.
     * @return the configurer for further customization
     * @throws Exception when an exception occurs while configuring
     * @deprecated will be removed in 1.0.0. Migrate your applications to new request matchers.
     *             https://docs.spring.io/spring-security/reference/5.8/migration/servlet/config.html#use-new-requestmatchers
     */
    @Deprecated
    public static PartnerNetSaml2Configurer apply(HttpSecurity http, String entityId, String metadataUrl,
        boolean useAuthorizeHttpRequests) throws Exception
    {
        if (useAuthorizeHttpRequests)
        {
            http //
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, DEFAULT_ENTITY_ID_PATH)
                .permitAll();
        }
        else
        {
            http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, DEFAULT_ENTITY_ID_PATH.replace("{registrationId}", "*"))
                .permitAll();
        }

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
    private BiFunction<PartnerNetSaml2AuthenticationPrincipal, Saml2Data, Collection<? extends GrantedAuthority>> authoritiesMapper;
    private Consumer<AuthnRequestContext> authnRequestCustomizer;
    private AuthenticationFailureHandler failureHandler;
    private String failureUrl;
    private AuthenticationSuccessHandler successHandler;

    private RelyingPartyRegistrationResolver relyingPartyResolver;
    private Customizer<Saml2LoginConfigurer<HttpSecurity>> customizer = saml2Login -> {
        // Noop customizer. Users can override this to add custom configurations
    };

    private PartnerNetSaml2Configurer(String entityId, String metadataUrl)
    {
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
    public PartnerNetSaml2Configurer failOnStartup()
    {
        failOnStartup = true;

        return this;
    }

    /**
     * @param credentialConfigs static list of credentials to use for authentication
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer credentials(Saml2CredentialsConfig... credentialConfigs)
    {
        return credentials(() -> Arrays.asList(credentialConfigs));
    }

    /**
     * @param supplier the supplier that will be called periodically to load the most up to date set of credentials
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer credentials(Supplier<List<Saml2CredentialsConfig>> supplier)
    {
        credentialsManager = new DefaultSaml2CredentialsManager(supplier);

        return this;
    }

    /**
     * Override the default client factory to be used for loading SAML metadata
     *
     * @param clientFactory the client factory to use
     * @return the builder for a fluent api
     * @see HttpClientFactory#defaultClient()
     */
    public PartnerNetSaml2Configurer clientFactory(HttpClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;

        return this;
    }

    /**
     * Override the default response processor
     *
     * @param responseProcessor the response processor to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer responseProcessor(Saml2ResponseProcessor responseProcessor)
    {
        this.responseProcessor = responseProcessor;

        return this;
    }

    /**
     * Override the default response parser
     *
     * @param responseParser the response parser to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer responseParser(Saml2ResponseParser responseParser)
    {
        this.responseParser = responseParser;

        return this;
    }

    /**
     * Override the default authorities mapper
     *
     * @param authoritiesMapper the new authoritiesmapper to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer authoritiesMapper(
        BiFunction<PartnerNetSaml2AuthenticationPrincipal, Saml2Data, Collection<? extends GrantedAuthority>> authoritiesMapper)
    {
        this.authoritiesMapper = authoritiesMapper;

        return this;
    }

    /**
     * Set the URL to redirect to on authentication failure. This will override the registered
     * {@link #failureHandler(AuthenticationFailureHandler)} if any.
     *
     * @param failureUrl the new failureUrl to use
     * @return the builder for a fluent api
     * @deprecated use the {@link #customizer(Customizer)} method instead
     */
    @Deprecated
    public PartnerNetSaml2Configurer failureUrl(String failureUrl)
    {
        this.failureUrl = failureUrl;
        failureHandler = null;

        return this;
    }

    /**
     * Set the {@link AuthenticationFailureHandler} to use on authentication failure. This will override the registered
     * {@link #failureUrl(String)} if any.
     *
     * @param failureHandler the new failure handler to use
     * @return the builder for a fluent api
     * @deprecated use the {@link #customizer(Customizer)} method instead
     */
    @Deprecated
    public PartnerNetSaml2Configurer failureHandler(AuthenticationFailureHandler failureHandler)
    {
        this.failureHandler = failureHandler;
        failureUrl = null;

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
    public PartnerNetSaml2Configurer successHandler(AuthenticationSuccessHandler successHandler)
    {
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
    public PartnerNetSaml2Configurer customizer(Customizer<Saml2LoginConfigurer<HttpSecurity>> customizer)
    {
        this.customizer = Objects.requireNonNull(customizer, "Customizer must not be null");

        return this;
    }

    /**
     * Adds a customizer that allows you to further customize the {@link AuthnRequestContext}. This is necessary, if
     * values like maxSessionAge, the tenant or the nistLevel aren't provided by request parameters.
     *
     * @param authnRequestCustomizer
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer authnRequestCustomizer(Consumer<AuthnRequestContext> authnRequestCustomizer)
    {
        this.authnRequestCustomizer = authnRequestCustomizer;

        return this;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception
    {
        Saml2CredentialsManager credManager = getCredentialsManager();
        RelyingPartyRegistrationRepository relyingPartyRegistrationRepository =
            getRelyingPartyRegistrationRepository(credManager);
        relyingPartyResolver = new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);

        builder.authenticationProvider(buildAuthenticationProvider());

        builder.saml2Login(saml2Login -> {
            saml2Login.relyingPartyRegistrationRepository(relyingPartyRegistrationRepository);
            saml2Login.authenticationDetailsSource(new HttpRequestContextAwareSaml2AuthenticationDetailsSource());
            saml2Login.authenticationRequestResolver(buildRequestResolver(relyingPartyResolver));

            saml2Login.loginProcessingUrl(DEFAULT_LOGIN_PROCESSING_URL);
            saml2Login.successHandler(getSuccessHandler());

            if (failureHandler != null)
            {
                saml2Login.failureHandler(failureHandler);
            }
            else if (failureUrl != null)
            {
                saml2Login.failureUrl(failureUrl);
            }

            customizer.customize(saml2Login);
        });
    }

    private AuthenticationSuccessHandler getSuccessHandler()
    {
        if (successHandler != null)
        {
            return successHandler;
        }

        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setRedirectStrategy(new Saml2UrlSanitizingRedirectStrategy());

        return handler;
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception
    {
        builder.addFilterBefore(buildMetadataFilter(), Saml2WebSsoAuthenticationFilter.class);
        builder.saml2Login().authenticationManager(builder.getSharedObject(AuthenticationManager.class));
    }

    private Filter buildMetadataFilter()
    {
        Saml2MetadataResolver metadataResolver = new PartnerNetSaml2MetadataResolver();
        return new Saml2ServiceProviderMetadataFilter(DEFAULT_ENTITY_ID_PATH, relyingPartyResolver, metadataResolver);
    }

    private AuthenticationProvider buildAuthenticationProvider()
    {
        Saml2ResponseProcessor responseProcessor = getResponseProcessor();
        Saml2ResponseParser parser = getResponseParser();

        return postProcess(new PartnerNetSamlAuthenticationProvider(responseProcessor, parser));
    }

    private Saml2ResponseParser getResponseParser()
    {
        if (responseParser == null)
        {
            return new PartnerNetSaml2ResponseParser(getAuthoritiesMapper());
        }

        return responseParser;
    }

    private BiFunction<PartnerNetSaml2AuthenticationPrincipal, Saml2Data, Collection<? extends GrantedAuthority>> getAuthoritiesMapper()
    {
        if (authoritiesMapper == null)
        {
            return new DefaultPartnerNetAuthoritiesMapper();
        }

        return authoritiesMapper;
    }

    public Consumer<AuthnRequestContext> getAuthnRequestCustomizer()
    {
        if (authnRequestCustomizer == null)
        {
            return new PartnerNetSaml2AuthnRequestCustomizer();
        }

        return authnRequestCustomizer;
    }

    private Saml2ResponseProcessor getResponseProcessor()
    {
        if (responseProcessor == null)
        {
            return Saml2ResponseProcessor.withDefaultHandlers();
        }

        return responseProcessor;
    }

    private Saml2CredentialsManager getCredentialsManager()
    {
        return postProcess(requireNonNull(credentialsManager, "No credentials configured"));
    }

    private RelyingPartyRegistrationRepository getRelyingPartyRegistrationRepository(
        Saml2CredentialsManager credManager)
    {
        ReloadingRelyingPartyRegistrationRepository repository =
            new ReloadingRelyingPartyRegistrationRepository(DEFAULT_REGISTRATION_ID, entityId, metadataUrl, credManager,
                clientFactory, DEFAULT_LOGIN_PROCESSING_URL, DEFAULT_ENTITY_ID_PATH);

        if (failOnStartup)
        {
            requireNonNull(repository.findByRegistrationId(DEFAULT_REGISTRATION_ID),
                format("No RelyingPartyRegistration for metadata %s found", metadataUrl));
        }

        return repository;
    }

    private Saml2AuthenticationRequestResolver buildRequestResolver(
        RelyingPartyRegistrationResolver relyingPartyRegistrationResolver)
    {
        OpenSaml4AuthenticationRequestResolver resolver =
            new OpenSaml4AuthenticationRequestResolver(relyingPartyRegistrationResolver);

        resolver.setAuthnRequestCustomizer(getAuthnRequestCustomizer());
        resolver
            .setRelayStateResolver(request -> Saml2Utils //
                .getRelayState(request)
                .map(relayState -> String.format(AUTO_GENERATED_RELAY_STATE_FORMAT, UUID.randomUUID(), relayState)) // pre-append a random string
                .orElseGet(() -> String.format(AUTO_GENERATED_RELAY_STATE_FORMAT, UUID.randomUUID(), ""))); // default to the auto generated UUID;

        return resolver;
    }
}
