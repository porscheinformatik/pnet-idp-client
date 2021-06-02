package at.porscheinformatik.idp.saml2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.util.StreamUtils;

public class TestClientFactory implements HttpClientFactory
{
    private final String metadata;
    private final String metadataUrl;

    public TestClientFactory(String metadataFile, String metadataUrl) throws IOException
    {
        super();

        this.metadata = loadMetadata(metadataFile);
        this.metadataUrl = metadataUrl;
    }

    private String loadMetadata(String metadataFile) throws IOException
    {
        try (InputStream metadataStream = getClass().getResourceAsStream(metadataFile))
        {
            String newMetadata = StreamUtils.copyToString(metadataStream, Charset.forName("UTF-8"));

            String validUntil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(6).format(DateTimeFormatter.ISO_DATE_TIME);
            newMetadata = newMetadata.replace("${validUntil}", validUntil);

            return newMetadata;
        }
    }

    @Override
    public HttpClient newClient() throws Saml2Exception
    {
        return new StaticHttpClient();
    }

    private class StaticHttpClient extends CloseableHttpClient
    {
        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
            throws IOException, ClientProtocolException
        {
            String method = request.getRequestLine().getMethod();

            if (!"GET".equals(method))
            {
                throw new IllegalArgumentException("Only get requests are supported.");
            }

            String uri = request.getRequestLine().getUri();

            if (!Objects.equals(metadataUrl, uri))
            {
                throw new IOException(
                    String.format("Uri %s could not be loaded. EntityDescriptor was %s", uri, metadataUrl));
            }

            return new TestHttpResponse(metadata);
        }

        @Override
        public void close() throws IOException
        {
            // Nothing to close here
        }

        @SuppressWarnings("deprecation")
        @Override
        public HttpParams getParams()
        {
            throw new UnsupportedOperationException("Implement when needed");
        }

        @SuppressWarnings("deprecation")
        @Override
        public ClientConnectionManager getConnectionManager()
        {
            throw new UnsupportedOperationException("Implement when needed");
        }

    }

    private static class TestHttpResponse extends BasicHttpResponse implements CloseableHttpResponse
    {
        public TestHttpResponse(String content)
        {
            super(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");

            setEntity(new StringEntity(content, ContentType.create("application/samlmetadata+xml", "UTF-8")));
        }

        @Override
        public void close() throws IOException
        {
            // Nothing to close
        }

    }
}
