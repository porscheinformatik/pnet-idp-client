/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static java.util.Objects.*;
import static org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport.*;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.springframework.security.saml2.core.Saml2X509Credential;

import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.security.impl.SecureRandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

/**
 * @author Daniel Furtlehner
 */
public final class Saml2ObjectUtils
{
    //ID is required. Specification says between 128 and 160 bit are perfect
    private static final IdentifierGenerationStrategy ID_GENERATOR = new SecureRandomIdentifierGenerationStrategy(20);

    private Saml2ObjectUtils()
    {
        super();
    }

    /**
     * @return a random indentifier for saml messages
     */
    public static String generateId()
    {
        return ID_GENERATOR.generateIdentifier();
    }

    /**
     * Creates a {@link Response} with the issueInstant set to the actual date and the given issuer set.
     *
     * @param issuer - Aufrufer
     * @param destination - destination on application side
     * @param inResponseTo - Id des SAML Requests
     * @return SAML Response
     */
    public static Response response(@Nonnull String issuer, @Nullable String destination, @Nullable String inResponseTo)
    {
        Response response = createSamlObject(Response.DEFAULT_ELEMENT_NAME);

        response.setIssueInstant(Instant.now());
        response.setID(generateId());
        response.setIssuer(issuer(issuer));

        if (destination != null)
        {
            response.setDestination(destination);
        }

        if (inResponseTo != null)
        {
            response.setInResponseTo(inResponseTo);
        }

        return response;
    }

    public static Status status(@Nonnull String statusCode, @Nullable String message)
    {
        Status status = createSamlObject(Status.DEFAULT_ELEMENT_NAME);

        StatusCode code = createSamlObject(StatusCode.DEFAULT_ELEMENT_NAME);
        code.setValue(statusCode);
        status.setStatusCode(code);

        if (message != null)
        {
            StatusMessage statusMessage = createSamlObject(StatusMessage.DEFAULT_ELEMENT_NAME);
            statusMessage.setValue(message);
            status.setStatusMessage(statusMessage);
        }

        return status;
    }

    /**
     * Creates a saml2 isser element
     *
     * @param issuer the issuer value
     * @return the saml issuer
     */
    public static Issuer issuer(String issuer)
    {
        Issuer samlIssuer = createSamlObject(Issuer.DEFAULT_ELEMENT_NAME);
        samlIssuer.setValue(requireNonNull(issuer, "Issuer value must not be null"));

        return samlIssuer;
    }

    @Nonnull
    public static Subject subject(String assertionConsumerServiceUrl, int validityInSeconds, String authnRequestId)
    {
        Subject subject = createSamlObject(Subject.DEFAULT_ELEMENT_NAME);

        subject.setNameID(randomNameID());

        SubjectConfirmation subjectConfirm = createSamlObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConfirm.setMethod(SubjectConfirmation.METHOD_BEARER);

        SubjectConfirmationData confirmData = createSamlObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        confirmData.setRecipient(assertionConsumerServiceUrl);
        confirmData.setNotOnOrAfter(Instant.now().plusSeconds(validityInSeconds));

        if (authnRequestId != null)
        {
            confirmData.setInResponseTo(authnRequestId);
        }

        subjectConfirm.setSubjectConfirmationData(confirmData);

        subject.getSubjectConfirmations().add(subjectConfirm);

        return subject;
    }

    //
    //

    /**
     * Creates a SAML attribute.
     *
     * @param name the name of the attribute
     * @param nameFormat the name format
     * @return the attribute
     */
    public static Attribute attribute(String name, String nameFormat)
    {
        Attribute attribute = createSamlObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(name);
        attribute.setNameFormat(nameFormat);

        return attribute;
    }

    public static <T extends SAMLObject> T createSamlObject(QName defaultName)
    {
        XMLObjectBuilderFactory factory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        @SuppressWarnings("unchecked") SAMLObjectBuilder<T> builder =
            (SAMLObjectBuilder<T>) factory.getBuilder(defaultName);

        return builder.buildObject();
    }

    @Nonnull
    public static Conditions conditions(String serviceIdentifier)
    {
        Conditions conditions = createSamlObject(Conditions.DEFAULT_ELEMENT_NAME);
        conditions.getConditions().add(createSamlObject(OneTimeUse.DEFAULT_ELEMENT_NAME));

        AudienceRestriction restriction = createSamlObject(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        Audience audience = createSamlObject(Audience.DEFAULT_ELEMENT_NAME);
        audience.setURI(serviceIdentifier);

        restriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(restriction);

        return conditions;
    }

    @Nonnull
    public static AuthnStatement authnStatement(@Nonnull Instant authenticationTime,
        @Nonnull String authnContextClassRef)
    {
        AuthnStatement authn = createSamlObject(AuthnStatement.DEFAULT_ELEMENT_NAME);

        authn.setAuthnInstant(authenticationTime);
        authn.setAuthnContext(authnContext(authnContextClassRef));
        authn.setSessionIndex("ABC");

        return authn;
    }

    public static AttributeStatement attributeStatement()
    {
        AttributeStatement attributeStatement = createSamlObject(AttributeStatement.DEFAULT_ELEMENT_NAME);

        return attributeStatement;
    }

    @Nonnull
    public static Assertion assertion(@Nonnull String issuer, @Nonnull Subject subject, @Nonnull Conditions conditions,
        @Nonnull AuthnStatement authnStatement, @Nonnull AttributeStatement attributeStatement)
    {
        Assertion assertion = createSamlObject(Assertion.DEFAULT_ELEMENT_NAME);

        assertion.setIssuer(issuer(issuer));
        assertion.setID(generateId());
        assertion.setIssueInstant(Instant.now());
        assertion.setSubject(requireNonNull(subject, "subject must not be null"));
        assertion.setConditions(conditions);
        assertion.getAuthnStatements().add(authnStatement);
        assertion.getAttributeStatements().add(attributeStatement);

        return assertion;
    }

    @Nonnull
    public static EncryptedAssertion encryptAssertion(Assertion assertion, String recipientEntityId,
        Saml2X509Credential keyInfo) throws EncryptionException
    {
        BasicX509Credential credential = new BasicX509Credential(keyInfo.getCertificate());

        KeyInfoGeneratorFactory keyInfoGeneratorFactory = SecurityConfigurationSupport
            .getGlobalEncryptionConfiguration()
            .getKeyTransportKeyInfoGeneratorManager()
            .getDefaultManager()
            .getFactory(credential);

        DataEncryptionParameters dataParams = new DataEncryptionParameters();
        dataParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM);

        KeyEncryptionParameters keyParams = new KeyEncryptionParameters();
        keyParams.setRecipient(recipientEntityId);
        keyParams.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        keyParams.setEncryptionCredential(credential);
        keyParams.setKeyInfoGenerator(keyInfoGeneratorFactory.newInstance());

        Encrypter encrypter = new Encrypter(dataParams, keyParams);

        return encrypter.encrypt(assertion);
    }

    public static void sign(SignableSAMLObject signable, Saml2X509Credential credential)
        throws SecurityException, MarshallingException, SignatureException, KeyStoreException, NoSuchAlgorithmException,
        CertificateException, IOException
    {
        Credential samlCredential = new BasicX509Credential(credential.getCertificate(), credential.getPrivateKey());

        SignatureSigningParameters parameters = new SignatureSigningParameters();
        parameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        parameters.setSigningCredential(samlCredential);
        parameters.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        SignatureSupport.signObject(signable, parameters);
    }

    /**
     * We always create random nameIds because they are only used for single logout. As we kill all sessions immediately
     * after logout we don't have to worry about this anyway.
     *
     * @return the name id
     */
    @Nonnull
    private static NameID randomNameID()
    {
        NameID samlID = createSamlObject(NameID.DEFAULT_ELEMENT_NAME);

        samlID.setFormat(NameIDType.TRANSIENT);
        samlID.setValue(generateId());

        return samlID;
    }

    @Nonnull
    private static AuthnContext authnContext(@Nonnull String authnContextClassRef)
    {
        AuthnContext context = createSamlObject(AuthnContext.DEFAULT_ELEMENT_NAME);

        AuthnContextClassRef classRef = createSamlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        classRef.setURI(authnContextClassRef);

        context.setAuthnContextClassRef(classRef);

        return context;
    }

    /**
     * Creates a saml object from the given string representations
     *
     * @param xmlAsString the xml representation to unmarshal
     * @param <T> the type of object returned
     * @return the element
     * @throws XMLParserException Wenn beim Parsen was schiefgeht
     * @throws UnmarshallingException - Wenn beim unmarshall was schiefgeht
     * @throws XMLParserException - when something goes wrong
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(String xmlAsString) throws UnmarshallingException, XMLParserException
    {
        return (T) XMLObjectSupport.unmarshallFromReader(getParserPool(), new StringReader(xmlAsString));
    }
}
