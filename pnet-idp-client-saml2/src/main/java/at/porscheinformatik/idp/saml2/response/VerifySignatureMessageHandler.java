/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureValidationConfiguration;
import org.opensaml.xmlsec.SignatureValidationParameters;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.SignatureValidationParametersCriterion;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.security.saml2.core.Saml2X509Credential;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

/**
 * @author Daniel Furtlehner
 */
public class VerifySignatureMessageHandler extends AbstractSimpleMessageHandler
{
    private static final SAMLSignatureProfileValidator SIGNATURE_PROFILE_VALIDATOR =
        new SAMLSignatureProfileValidator();

    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException
    {
        Response response = getResponse(messageContext);

        // Successful responses MUST be signed. Error responses MAY be signed.
        boolean signatureRequired = Objects.equals(response.getStatus().getStatusCode().getValue(), StatusCode.SUCCESS);
        boolean isSigned = response.isSigned();

        if (signatureRequired && !isSigned)
        {
            throw new MessageHandlerException("Response must be signed but no signature present");
        }

        if (isSigned)
        {
            String idpEntityId = response.getIssuer().getValue();
            Collection<Saml2X509Credential> credentials = getAuthenticationToken(messageContext)
                .getRelyingPartyRegistration()
                .getAssertingPartyDetails()
                .getVerificationX509Credentials();

            if (credentials.isEmpty())
            {
                throw new MessageHandlerException(
                    String.format("No verification credentials found for %s", idpEntityId));
            }

            try
            {
                verifySignature(response, credentials);
            }
            catch (SignatureException e)
            {
                throw new MessageHandlerException("Error validating signature", e);
            }
        }
    }

    /**
     * Verifies the signature of a signable saml object.
     *
     * @param signable to validate
     * @param credentials a list of credentials. We should go for a seamless key migration. So a entity might add
     *            multiple keys into the metadata and sign with one of them. When the key is not used anymore it signs
     *            with the new valid key. When checking with all keys we should account for this. The first one in the
     *            list should be the one that is normally used. So no performance problems.
     * @throws SignatureException when the signature is not valid
     */
    private void verifySignature(SignableSAMLObject signable, Collection<Saml2X509Credential> credentials)
        throws SignatureException
    {
        SIGNATURE_PROFILE_VALIDATOR.validate(signable.getSignature());

        SignatureTrustEngine trustEngine = buildTrustEngine(credentials);

        CriteriaSet trustBasisCriteria = new CriteriaSet();
        trustBasisCriteria.add(new SignatureValidationParametersCriterion(buildSignatureValidationParameters()));

        try
        {
            if (!trustEngine.validate(signable.getSignature(), trustBasisCriteria))
            {
                throw new SignatureException("Failed to validate signature with all available certificates.");
            }
        }
        catch (SecurityException e)
        {
            throw new SignatureException("Failed to validate signature with all available certificates.", e);
        }
    }

    @Nonnull
    private SignatureTrustEngine buildTrustEngine(Collection<Saml2X509Credential> credentials)
    {
        List<Credential> samlCredentials = credentials //
            .stream()
            .map(key -> new BasicX509Credential(key.getCertificate()))
            .collect(Collectors.toList());

        CredentialResolver resolver = new StaticCredentialResolver(samlCredentials);
        KeyInfoCredentialResolver keyInfoResolver = new StaticKeyInfoCredentialResolver(samlCredentials);

        return new ExplicitKeySignatureTrustEngine(resolver, keyInfoResolver);
    }

    @Nonnull
    private SignatureValidationParameters buildSignatureValidationParameters()
    {
        SignatureValidationConfiguration validationConfiguration =
            SecurityConfigurationSupport.getGlobalSignatureValidationConfiguration();

        SignatureValidationParameters params = new SignatureValidationParameters();
        params.setExcludedAlgorithms(validationConfiguration.getExcludedAlgorithms());
        params.setIncludedAlgorithms(validationConfiguration.getIncludedAlgorithms());

        return params;
    }

}
