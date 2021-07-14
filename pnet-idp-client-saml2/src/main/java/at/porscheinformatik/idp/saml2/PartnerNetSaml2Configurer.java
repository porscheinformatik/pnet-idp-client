/**
 * 
 */
package at.porscheinformatik.idp.saml2;

import static java.lang.String.*;
import static java.util.Objects.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationRequestFactory;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestFactory;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
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
    private AuthenticationFailureHandler failureHandler;
    private String failureUrl;
    private AuthenticationSuccessHandler successHandler;

    private Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyResolver;

    public PartnerNetSaml2Configurer(PartnerNetSaml2Provider provider)
    {
        this(provider.getEntityId());
    }

    public PartnerNetSaml2Configurer(String entityId)
    {
        this(entityId, entityId);
    }

    public PartnerNetSaml2Configurer(String entityId, String metadataUrl)
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
     * {@link #failureHandler(AuthenticationFailureHandler) if any.
     * 
     * @param failureUrl the new failureUrl to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer failureUrl(String failureUrl)
    {
        this.failureUrl = failureUrl;
        this.failureHandler = null;

        return this;
    }

    /**
     * Set the {@link AuthenticationFailureHandler} to use on authentication failure. This will override the registered
     * {@link #failureUrl(String)} if any.
     * 
     * @param failureHandler the new failure handler to use
     * @return the builder for a fluent api
     */
    public PartnerNetSaml2Configurer failureHandler(AuthenticationFailureHandler failureHandler)
    {
        this.failureHandler = failureHandler;
        this.failureUrl = null;

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

    @Override
    public void init(HttpSecurity builder) throws Exception
    {
        Saml2CredentialsManager credManager = getCredentialsManager();
        RelyingPartyRegistrationRepository relyingPartyRegistrationRepository =
            getRelyingPartyRegistrationRepository(credManager);
        relyingPartyResolver = new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);

        builder.authenticationProvider(buildAuthenticationProvider());

        builder.setSharedObject(Saml2AuthenticationRequestFactory.class, buildRequestFactory());
        customizeRequestContextResolver(builder, relyingPartyResolver);

        builder.saml2Login(saml2Login -> {
            saml2Login.relyingPartyRegistrationRepository(relyingPartyRegistrationRepository);
            saml2Login
                .authenticationConverter(new HttpRequestContextAwareSaml2AuthenticationConverter(relyingPartyResolver));

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
        });

        builder
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, DEFAULT_ENTITY_ID_PATH.replace("{registrationId}", "*"))
            .permitAll();
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

    private void customizeRequestContextResolver(HttpSecurity builder,
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyResolver)
    {
        ApplicationContext appContext =
            requireNonNull(builder.getSharedObject(ApplicationContext.class), "No application context configured");

        PartnerNetSaml2AuthenticationRequestContextResolver resolver =
            appContext.getBean(PartnerNetSaml2AuthenticationRequestContextResolver.class);
        resolver.setRelyingPartyRegistrationResolver(relyingPartyResolver);
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
            new ReloadingRelyingPartyRegistrationRepository(DEFAULT_REGISTRATION_ID, this.entityId, this.metadataUrl,
                credManager, clientFactory, DEFAULT_LOGIN_PROCESSING_URL, DEFAULT_ENTITY_ID_PATH);

        if (failOnStartup)
        {
            requireNonNull(repository.findByRegistrationId(DEFAULT_REGISTRATION_ID),
                format("No RelyingPartyRegistration for metadata %s found", this.metadataUrl));
        }

        return repository;
    }

    private Saml2AuthenticationRequestFactory buildRequestFactory()
    {
        OpenSamlAuthenticationRequestFactory factory = new OpenSamlAuthenticationRequestFactory();
        factory.setAuthenticationRequestContextConverter(new PartnerNetSaml2AuthenticationRequestContextConverter());

        return factory;
    }

}
