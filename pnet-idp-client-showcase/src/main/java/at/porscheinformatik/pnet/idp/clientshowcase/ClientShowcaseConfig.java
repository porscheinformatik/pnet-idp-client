package at.porscheinformatik.pnet.idp.clientshowcase;

import at.porscheinformatik.pnet.idp.clientshowcase.session.SessionWrapperFilter;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientShowcaseConfig {

    @Bean
    public TomcatContextCustomizer contextCustomizer() {
        return context -> {
            // We have to set the same site attribute, otherwise saml authentication does not work because of missing session cookies
            Rfc6265CookieProcessor processor = new Rfc6265CookieProcessor();
            processor.setSameSiteCookies("None");

            context.setCookieProcessor(processor);
        };
    }

    /**
     * <b>So not use this filter in a production environment. Use spring-session if you want to share the session
     * between instances of your application.</b>
     *
     * <p>
     * This filter is here to mimic the way spring-session handles sessions. Session attributes will be serialized and
     * deserialized. Once there was a Problem in the SAML Implementation of Spring, where non serializable objects where
     * added to the session. To find such Problems early on when upgrading, we serialize and deserialize everything that
     * is added to the session.
     * </p>
     *
     * @return the filter
     */
    @Bean
    public SessionWrapperFilter sessionWrapperFilter() {
        return new SessionWrapperFilter();
    }
}
