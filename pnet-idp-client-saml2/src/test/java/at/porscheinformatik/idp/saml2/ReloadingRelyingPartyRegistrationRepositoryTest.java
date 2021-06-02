package at.porscheinformatik.idp.saml2;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration.AssertingPartyDetails;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;

class ReloadingRelyingPartyRegistrationRepositoryTest
{
    private static final String METADATA_CERTIFICATE_BYTES =
        "MIIFrTCCA5WgAwIBAgIEUM6dXDANBgkqhkiG9w0BAQsFADCBhjELMAkGA1UEBhMCQVQxETAPBgNVBAgTCFNhbHpidXJnMREwDwYDVQQHEwhTYWx6YnVyZzEYMBYGA1UEChMPUG9yc2NoZSBIb2xkaW5nMRswGQYDVQQLExJQb3JzY2hlIEluZm9ybWF0aWsxGjAYBgNVBAMTEVBhcnRuZXIuTmV0IFNBTUwyMB4XDTE4MTEwNTEzMTUwN1oXDTIxMTAyMDEzMTUwN1owgYYxCzAJBgNVBAYTAkFUMREwDwYDVQQIEwhTYWx6YnVyZzERMA8GA1UEBxMIU2FsemJ1cmcxGDAWBgNVBAoTD1BvcnNjaGUgSG9sZGluZzEbMBkGA1UECxMSUG9yc2NoZSBJbmZvcm1hdGlrMRowGAYDVQQDExFQYXJ0bmVyLk5ldCBTQU1MMjCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAI1KjXSZmiV14H8c/jUaUzSjuCY5AzmM4Cw7RcUohSi5YNHn+dZuViPztbCm1nLjyHmPmkGt/nBW0Ghw3XwGt1JREynuvmy4TcxhoQX9c77XY+SoYZMf/aEQFdFy5YhDcGHDo5RRZB/6vnkgSuCrvrUriL87UcuXtOuq2f9xz4kQjBtPhdjl9d3CRr8mF5t3LuIBmVsJixr/r7snCJHVX/DYTJYhy97GKr1iuZwzXS83FAOGyEhh5PgLY82kaC5/iftdkxTsSQdCZT5bkjKkfECCeoVUY+LR5Gi0/odzHgAO71Gw36OWPt9dOdCzPynvVHuMd1ZuVotQUYGv/HMZG+6cw0l7h7m9R3Lxe0VPsxlzpLmKYYvV+haP1iE8yI/CESH4X1mfyHyyZ2nRrxTVbXaRjExeq/G4ldfuqiaKsbhoQ3WjJQ1EjgMbNZM+gKN+QD+udJyGJeGBp+fWSKFcvDecYrDuYZn7KsirhIxB6yWoRj8O1FVDi45YL6R8F9XePsx+4Z16QkoHA5CkbFgidDJwhwQ9zYd5kUtPTL3ZE0NgTZk8CRbB1gcPO+Q2uj5B9oIdy9WFshaO66z7/jcW2p15jfhK7p1vO/JNUKkVMpZkO52ukl3B/J0l7MEbLeZfw7MGRQbMDxjbvL1LDeXN5q5mviYAgnklp2B2o3gS41pNAgMBAAGjITAfMB0GA1UdDgQWBBQ2XPoaTIQn4PS7fNWlY+YLfkCBOzANBgkqhkiG9w0BAQsFAAOCAgEAJZqjZ8HclxkuqrtVvL/esrPgFxd5awAc0jXpckJgFMOMZ3y6G7isREY/QlfyCjVvx+5EB29dfPbhAc7835IPzgsfCbZGLqNIC45CDDDeZjocPR3OJ7njGa3piywgI1lLv8dS1MpQWVrjxuh8mB1pZ8cGf5OFxo5GNrNJFmwAsRhWcTsJ2/9EHgmp7MlOnrDmhQfx0H2dHTY90F7spX0Q20M3PXqmi7sc8eQ1U1uFg4vnkY+dS7NeMuTZ9R70qnjWfhJXK4yGg65fvGfl5mD/MgJlF2qyr5aQiE3Tr74NYswBGg7ZrpWGGmEi1dx+SaQYKE5b3bOReuHLnjbSAUP5FNfUSokKJwL2FE8+41p3sTrQPGdcI+xneyA+qaPEJTR54zobPnvjllaCfxXBJc98bzdH4OyL3a50p5TZrDVdhi+oWkdVMdJmI3E93z+Ta6I88++2GS2yxwxCxr9TSX1MFHohmR7Tn+0Rz4y9xXOPISZ/lukre9eyWL3I4Qv6Iwru8bdIkiFTIQyfWVXXA3zKKRWJFJWSiTE+sV779XqY66RFNH460gogTXjzeREeH8/MStbmm4jAN//ULL2izuarGAjj0YT8m2KoyCKyqJMHojo9fQm8VBlJe+83vf2wwgsSNax9yFG2wmYfyUYInk+0+krU0B3xPutOx6d0vItfHPQ=";

    static
    {
        OpenSamlInitializationService.initialize();
    }

    @Test
    void testFindByWrongRegistrationId() throws Exception
    {
        ReloadingRelyingPartyRegistrationRepository repository =
            buildRepository("https://qa-identity.auto-partner.net/identity/saml2",
                "https://qa-identity.auto-partner.net/identity/saml2", "saml2_metadata.xml");

        RelyingPartyRegistration registration = repository.findByRegistrationId("pnet");

        assertThat(registration, nullValue());
    }

    @Test
    void testFindByCorrectRegistrationId() throws Exception
    {
        ReloadingRelyingPartyRegistrationRepository repository =
            buildRepository("https://qa-identity.auto-partner.net/identity/saml2",
                "https://qa-identity.auto-partner.net/identity/saml2", "saml2_metadata.xml");

        RelyingPartyRegistration registration = repository.findByRegistrationId("rpId");

        assertThat(registration, notNullValue());
        assertThat(registration.getAssertionConsumerServiceBinding(), equalTo(Saml2MessageBinding.POST));
        assertThat(registration.getAssertionConsumerServiceLocation(),
            equalTo("{baseUrl}/saml2/sso/post/{registrationId}"));
        assertThat(registration.getEntityId(), equalTo("{baseUrl}/saml2/{registrationId}"));
        assertThat(registration.getRegistrationId(), equalTo("rpId"));

        Collection<Saml2X509Credential> decryptionCredentials = registration.getDecryptionX509Credentials();
        String dCDescription = Objects.toString(decryptionCredentials);
        assertThat(decryptionCredentials, notNullValue());
        assertThat(dCDescription, decryptionCredentials.size(), equalTo(1));
        Saml2X509Credential credential = decryptionCredentials.iterator().next();
        assertThat(dCDescription, credential.getPrivateKey(), notNullValue());
        assertThat(dCDescription, credential.getCertificate(), notNullValue());

        AssertingPartyDetails idpDetails = registration.getAssertingPartyDetails();
        assertThat(idpDetails.getEntityId(), equalTo("https://qa-identity.auto-partner.net/identity/saml2"));
        assertThat(idpDetails.getSingleSignOnServiceBinding(), equalTo(Saml2MessageBinding.REDIRECT));
        assertThat(idpDetails.getSingleSignOnServiceLocation(),
            equalTo("https://qa-identity.auto-partner.net/identity/saml2/authorize/redirect"));
        assertThat(idpDetails.getWantAuthnRequestsSigned(), equalTo(false));

        Collection<Saml2X509Credential> verificationCredentials = idpDetails.getVerificationX509Credentials();
        String vCDescription = Objects.toString(verificationCredentials);

        assertThat(verificationCredentials, notNullValue());
        assertThat(vCDescription, verificationCredentials.size(), equalTo(1));
        assertThat(vCDescription, firstCertificateAsString(verificationCredentials),
            equalTo(METADATA_CERTIFICATE_BYTES));
    }

    private String firstCertificateAsString(Collection<Saml2X509Credential> credentials)
        throws CertificateEncodingException
    {
        Saml2X509Credential credential = credentials.iterator().next();

        return Base64.getEncoder().encodeToString(credential.getCertificate().getEncoded());
    }

    private ReloadingRelyingPartyRegistrationRepository buildRepository(String entityId, String metadataUrl,
        String metadataFile) throws Exception
    {
        Saml2CredentialsManager credentialsManager = Saml2TestUtils.defaultCredentialsManager();
        return new ReloadingRelyingPartyRegistrationRepository("rpId", entityId, metadataUrl, credentialsManager,
            new TestClientFactory(metadataFile, metadataUrl), "/saml2/sso/post/{registrationId}");
    }

}
