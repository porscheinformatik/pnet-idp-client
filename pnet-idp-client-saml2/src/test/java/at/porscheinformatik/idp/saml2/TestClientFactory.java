package at.porscheinformatik.idp.saml2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.util.StreamUtils;

public class TestClientFactory implements HttpClientFactory {

    private final String metadata;
    private final String metadataUrl;

    public TestClientFactory(String metadataFile, String metadataUrl) throws IOException {
        super();
        metadata = loadMetadata(metadataFile);
        this.metadataUrl = metadataUrl;
    }

    private String loadMetadata(String metadataFile) throws IOException {
        try (InputStream metadataStream = getClass().getResourceAsStream(metadataFile)) {
            String newMetadata = StreamUtils.copyToString(metadataStream, Charset.forName("UTF-8"));

            String validUntil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(6).format(DateTimeFormatter.ISO_DATE_TIME);
            newMetadata = newMetadata.replace("${validUntil}", validUntil);

            return newMetadata;
        }
    }

    @Override
    public HttpClient newClient() throws Saml2Exception {
        return new StaticHttpClient();
    }

    private class StaticHttpClient extends CloseableHttpClient {

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context)
            throws IOException {
            String method = request.getMethod();

            if (!"GET".equals(method)) {
                throw new IllegalArgumentException("Only get requests are supported.");
            }

            String uri = request.getRequestUri();

            if (!Objects.equals(metadataUrl, uri)) {
                throw new IOException(
                    String.format("Uri %s could not be loaded. EntityDescriptor was %s", uri, metadataUrl)
                );
            }

            BasicClassicHttpResponse response = new BasicClassicHttpResponse(HttpStatus.SC_OK, "OK");

            response.setEntity(new StringEntity(metadata, ContentType.create("application/samlmetadata+xml", "UTF-8")));

            return CloseableHttpResponse.adapt(response);
        }

        @Override
        public void close() throws IOException {
            // Nothing to close here
        }

        @Override
        public void close(CloseMode closeMode) {
            // Nothing to close here
        }
    }
}
