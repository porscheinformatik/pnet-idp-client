package at.porscheinformatik.idp.saml2;

import org.apache.http.client.HttpClient;
import org.springframework.security.saml2.Saml2Exception;

import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder;

@FunctionalInterface
public interface HttpClientFactory
{

    /**
     * The default client factory builds a client with 5 seconds connection timeout and 10 seconds socket timeout.
     * 
     * @return the default http client
     */
    static HttpClientFactory defaultClient()
    {
        return new HttpClientFactory()
        {
            @Override
            public HttpClient newClient() throws Saml2Exception
            {
                try
                {
                    HttpClientBuilder clientBuilder = new HttpClientBuilder();

                    clientBuilder.setConnectionTimeout(5 * 1000);
                    clientBuilder.setSocketTimeout(10 * 1000);

                    return clientBuilder.buildClient();
                }
                catch (Exception e)
                {
                    throw new Saml2Exception("Error building HttpClient", e);
                }
            }
        };
    }

    /**
     * @return the client instance to use
     * @throws Saml2Exception if something goes wrong building the client
     */
    HttpClient newClient() throws Saml2Exception;

}
