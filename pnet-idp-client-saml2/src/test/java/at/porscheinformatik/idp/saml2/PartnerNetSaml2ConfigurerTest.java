package at.porscheinformatik.idp.saml2;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.servlet.Filter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationRequestFilter;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetSaml2ConfigurerTest
{
    private static final String IDP_ENTITY_ID = "https://identity.com/identity/saml2";

    static
    {
        Saml2Initializer.initialize();
    }

    @Test
    public void requestFilterIsConfigured() throws Exception
    {
        HttpSecurity http = buildHttpSecurity();
        PartnerNetSaml2Configurer.apply(http, IDP_ENTITY_ID, false).credentials(Saml2TestUtils::defaultCredentials);

        DefaultSecurityFilterChain filterChain = http.build();

        Saml2WebSsoAuthenticationRequestFilter filter =
            assertFilter(filterChain, Saml2WebSsoAuthenticationRequestFilter.class);

        assertFieldValue(filter, "authenticationRequestResolver", Saml2AuthenticationRequestResolver.class);
    }

    @Test
    public void authenticationFilterIsConfigured() throws Exception
    {
        HttpSecurity http = buildHttpSecurity();
        PartnerNetSaml2Configurer
            .apply(http, IDP_ENTITY_ID, false) //
            .credentials(Saml2TestUtils::defaultCredentials);

        DefaultSecurityFilterChain filterChain = http.build();

        Saml2WebSsoAuthenticationFilter filter = assertFilter(filterChain, Saml2WebSsoAuthenticationFilter.class);
        assertFieldValue(filter, "authenticationDetailsSource",
            HttpRequestContextAwareSaml2AuthenticationDetailsSource.class);
    }

    @Test
    public void metadataFilterIsConfigured() throws Exception
    {
        HttpSecurity http = buildHttpSecurity();
        PartnerNetSaml2Configurer
            .apply(http, IDP_ENTITY_ID, false) //
            .credentials(Saml2TestUtils::defaultCredentials);

        DefaultSecurityFilterChain filterChain = http.build();

        Saml2ServiceProviderMetadataFilter filter = assertFilter(filterChain, Saml2ServiceProviderMetadataFilter.class);
        AntPathRequestMatcher matcher = assertFieldValue(filter, "requestMatcher", AntPathRequestMatcher.class);
        assertThat(matcher, equalTo(new AntPathRequestMatcher("/saml2/{registrationId}", "GET")));
    }

    @Test
    public void authenticationProviderIsConfigured() throws Exception
    {
        HttpSecurity http = buildHttpSecurity();
        PartnerNetSaml2Configurer
            .apply(http, IDP_ENTITY_ID, false) //
            .credentials(Saml2TestUtils::defaultCredentials);

        DefaultSecurityFilterChain filterChain = http.build();

        Saml2WebSsoAuthenticationFilter filter = assertFilter(filterChain, Saml2WebSsoAuthenticationFilter.class);
        ProviderManager manager = assertFieldValue(filter, "authenticationManager", ProviderManager.class);

        manager
            .getProviders()
            .stream()
            .filter(provider -> PartnerNetSamlAuthenticationProvider.class.isAssignableFrom(provider.getClass()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No PartnerNet Authentication Provider configured"));
    }

    @Test
    public void missingCredentialsThrowsException() throws Exception
    {
        HttpSecurity http = buildHttpSecurity();
        PartnerNetSaml2Configurer.apply(http, IDP_ENTITY_ID, false);

        NullPointerException e = assertThrows(NullPointerException.class, () -> http.build());
        assertThat(e.getMessage(), equalTo("No credentials configured"));
    }

    @SuppressWarnings("unchecked")
    private <T> T assertFieldValue(Object o, String fieldName, Class<T> expectedClass)
    {
        FieldValueCallback callback = new FieldValueCallback(o, fieldName);
        ReflectionUtils.doWithFields(o.getClass(), callback);

        Object value = callback.value;

        assertThat(value, Matchers.instanceOf(expectedClass));

        return (T) value;
    }

    @SuppressWarnings("unchecked")
    private <FilterT extends Filter> FilterT findFirstFilter(List<Filter> filters, Class<FilterT> expectedType)
    {
        return filters
            .stream()
            .filter(filter -> filter.getClass().isAssignableFrom(expectedType))
            .map(filter -> (FilterT) filter)
            .findAny()
            .orElse(null);
    }

    private HttpSecurity buildHttpSecurity()
    {
        ObjectPostProcessor<Object> objectPostProcessor = new NoopPostProcessor();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        AuthenticationManagerBuilder authenticationBuilder = new AuthenticationManagerBuilder(objectPostProcessor);
        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(ApplicationContext.class, applicationContext);

        return new HttpSecurity(objectPostProcessor, authenticationBuilder, sharedObjects);
    }

    private <T extends Filter> T assertFilter(DefaultSecurityFilterChain filterChain, Class<T> filterClass)
    {
        T filter = findFirstFilter(filterChain.getFilters(), filterClass);

        assertThat(filter, notNullValue());

        return filter;
    }

    private static final class NoopPostProcessor implements ObjectPostProcessor<Object>
    {

        @Override
        public <O> O postProcess(O object)
        {
            return object;
        }

    }

    private static class FieldValueCallback implements FieldCallback
    {
        private final Object o;
        private final String fieldName;

        private Object value;

        FieldValueCallback(Object o, String fieldName)
        {
            super();

            this.o = o;
            this.fieldName = fieldName;
        }

        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException
        {
            if (Objects.equals(field.getName(), fieldName))
            {
                if (!field.canAccess(o))
                {
                    field.setAccessible(true);
                }

                value = field.get(o);
            }
        }

    }
}
