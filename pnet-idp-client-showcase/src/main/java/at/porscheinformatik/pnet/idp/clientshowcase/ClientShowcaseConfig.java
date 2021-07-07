package at.porscheinformatik.pnet.idp.clientshowcase;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientShowcaseConfig
{

    @Bean
    public TomcatContextCustomizer contextCustomizer()
    {
        return (context) -> {
            // We have to set the same site attribute, otherwise saml authentication does not work because of missing session cookies
            Rfc6265CookieProcessor processor = new Rfc6265CookieProcessor();
            processor.setSameSiteCookies("None");

            context.setCookieProcessor(processor);
        };
    }
}
