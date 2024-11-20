/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.credential.impl.CollectionCredentialResolver;
import org.opensaml.xmlsec.encryption.support.ChainingEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.LocalKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

/**
 * @author Daniel Furtlehner
 */
public class DecryptAssertionsMessageHandler extends AbstractSuccessResponseMessageHandler {

    @Override
    protected void doInvoke(Response response, MessageContext messageContext) throws MessageHandlerException {
        List<EncryptedAssertion> encryptedAssertions = response.getEncryptedAssertions();

        if (encryptedAssertions.isEmpty()) {
            return;
        }

        List<Assertion> assertionsToAdd = new ArrayList<>();
        RelyingPartyRegistration relyingPartyRegistration = getAuthenticationToken(
            messageContext
        ).getRelyingPartyRegistration();
        Collection<Saml2X509Credential> credentials = relyingPartyRegistration.getDecryptionX509Credentials();
        String entityId = relyingPartyRegistration.getEntityId();

        Decrypter decrypter = buildDecrypter(entityId, credentials);

        for (EncryptedAssertion encryptedAssertion : encryptedAssertions) {
            try {
                assertionsToAdd.add(decrypter.decrypt(encryptedAssertion));
            } catch (DecryptionException e) {
                throw new MessageHandlerException("Error decrypting EncryptedAssertions", e);
            }
        }

        response.getAssertions().addAll(assertionsToAdd);
    }

    private Decrypter buildDecrypter(String entityId, Collection<Saml2X509Credential> credentials) {
        List<Credential> samlCredentials = credentials
            .stream()
            .map(key -> CredentialSupport.getSimpleCredential(key.getCertificate(), key.getPrivateKey()))
            .collect(Collectors.toList());

        CollectionCredentialResolver localCredResolver = new CollectionCredentialResolver(samlCredentials);

        List<KeyInfoProvider> kiProviders = new ArrayList<>();
        kiProviders.add(new RSAKeyValueProvider());
        kiProviders.add(new InlineX509DataProvider());

        KeyInfoCredentialResolver keyResolver = new LocalKeyInfoCredentialResolver(kiProviders, localCredResolver);

        List<EncryptedKeyResolver> encryptedKeyResolvers = new ArrayList<>();
        encryptedKeyResolvers.add(new InlineEncryptedKeyResolver());
        encryptedKeyResolvers.add(new EncryptedElementTypeEncryptedKeyResolver());
        encryptedKeyResolvers.add(new SimpleRetrievalMethodEncryptedKeyResolver());

        ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver(
            encryptedKeyResolvers,
            entityId
        );

        return new Decrypter(null, keyResolver, encryptedKeyResolver);
    }
}
