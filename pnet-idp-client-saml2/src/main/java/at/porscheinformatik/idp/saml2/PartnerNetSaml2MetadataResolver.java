package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.XmlUtils.*;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2alg.DigestMethod;
import org.opensaml.saml.ext.saml2alg.SigningMethod;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.EncryptionConfiguration;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.algorithm.AlgorithmSupport;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Data;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

public class PartnerNetSaml2MetadataResolver implements Saml2MetadataResolver {

    public static final String SUBJECT_ID_REQUIREMENT_NAME = "urn:oasis:names:tc:SAML:profiles:subject-id:req";
    private static final Duration METADATA_MAX_VALIDITY = Duration.ofDays(7);

    @Override
    public String resolve(@Nonnull RelyingPartyRegistration relyingPartyRegistration) {
        Objects.requireNonNull(relyingPartyRegistration, "relyingPartyRegistration must not be null");

        EntityDescriptor entityDescriptor = buildMetadata(relyingPartyRegistration);

        try {
            return XmlUtils.marshall(entityDescriptor);
        } catch (MarshallingException e) {
            throw new Saml2Exception("Error building metadata", e);
        }
    }

    private EntityDescriptor buildMetadata(RelyingPartyRegistration relyingPartyRegistration) {
        Instant validUntil = Instant.now().plus(METADATA_MAX_VALIDITY);

        EntityDescriptor entityDescriptor = entityDescriptor(relyingPartyRegistration.getEntityId(), validUntil);

        // We only sign when IDP wants assertions signed
        SPSSODescriptor ssoDescriptor = spSsoDescriptor(
            relyingPartyRegistration.getAssertingPartyMetadata().getWantAuthnRequestsSigned()
        );

        ssoDescriptor
            .getAssertionConsumerServices()
            .add(
                assertionConsumerService(
                    relyingPartyRegistration.getAssertionConsumerServiceBinding().getUrn(),
                    relyingPartyRegistration.getAssertionConsumerServiceLocation()
                )
            );

        addCertificates(relyingPartyRegistration, ssoDescriptor);
        buildExtensions(entityDescriptor);

        entityDescriptor.getRoleDescriptors().add(ssoDescriptor);

        return entityDescriptor;
    }

    private void addCertificates(RelyingPartyRegistration relyingPartyRegistration, SPSSODescriptor ssoDescriptor) {
        relyingPartyRegistration
            .getSigningX509Credentials()
            .stream()
            .map(certificate -> keyDescritor(certificate.getCertificate(), UsageType.SIGNING))
            .forEach(ssoDescriptor.getKeyDescriptors()::add);

        relyingPartyRegistration
            .getDecryptionX509Credentials()
            .stream()
            .map(certificate -> keyDescritor(certificate.getCertificate(), UsageType.ENCRYPTION))
            .forEach(ssoDescriptor.getKeyDescriptors()::add);
    }

    private void buildExtensions(EntityDescriptor entityDescriptor) {
        Extensions extensions = createSamlObject(Extensions.DEFAULT_ELEMENT_NAME);

        addAttributes(extensions);
        addDigestMethods(extensions);
        addSigningMethods(extensions);

        entityDescriptor.setExtensions(extensions);
    }

    private void addSigningMethods(Extensions extensions) {
        SignatureSigningConfiguration signingConfig =
            SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();

        List<String> allowedSigningAlgorithms = signingConfig //
            .getSignatureAlgorithms()
            .stream()
            .filter(algorithm ->
                AlgorithmSupport.validateAlgorithmURI(
                    algorithm,
                    signingConfig.getIncludedAlgorithms(),
                    signingConfig.getExcludedAlgorithms()
                )
            )
            .toList();

        for (String allowedAlgorithm : allowedSigningAlgorithms) {
            extensions.getUnknownXMLObjects().add(signingMethod(allowedAlgorithm));
        }
    }

    private void addDigestMethods(Extensions extensions) {
        SignatureSigningConfiguration signingConfig =
            SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();

        List<String> allowedDigestAlgorithms = signingConfig //
            .getSignatureReferenceDigestMethods()
            .stream()
            .filter(algorithm ->
                AlgorithmSupport.validateAlgorithmURI(
                    algorithm,
                    signingConfig.getIncludedAlgorithms(),
                    signingConfig.getExcludedAlgorithms()
                )
            )
            .toList();

        for (String allowedAlgorithm : allowedDigestAlgorithms) {
            extensions.getUnknownXMLObjects().add(digestMethod(allowedAlgorithm));
        }
    }

    private void addAttributes(Extensions extensions) {
        EntityAttributes entityAttributes = createSamlObject(EntityAttributes.DEFAULT_ELEMENT_NAME);
        entityAttributes.getAttributes().add(entityAttribute(SUBJECT_ID_REQUIREMENT_NAME, "subject-id"));

        extensions.getUnknownXMLObjects().add(entityAttributes);
    }

    private Attribute entityAttribute(String attributeName, String attributeValue) {
        Attribute subjectIdAttribute = attribute(attributeName, Attribute.URI_REFERENCE);
        subjectIdAttribute.getAttributeValues().add(xmlString(attributeValue));
        return subjectIdAttribute;
    }

    /**
     * @param entityId the saml identifier
     * @param validUntil how long the metadata should be valid. Around a week or so is good.
     * @return The entity descriptor
     */
    private EntityDescriptor entityDescriptor(String entityId, Instant validUntil) {
        EntityDescriptor entityDescriptor = createSamlObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        entityDescriptor.setEntityID(entityId);
        entityDescriptor.setValidUntil(validUntil);

        return entityDescriptor;
    }

    private SPSSODescriptor spSsoDescriptor(boolean signedRequests) {
        SPSSODescriptor descriptor = createSamlObject(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        descriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        descriptor.getNameIDFormats().add(nameIdFormat(NameIDType.TRANSIENT));
        descriptor.setAuthnRequestsSigned(signedRequests);

        return descriptor;
    }

    private NameIDFormat nameIdFormat(String nameIdFormat) {
        NameIDFormat format = createSamlObject(NameIDFormat.DEFAULT_ELEMENT_NAME);
        format.setURI(nameIdFormat);

        return format;
    }

    private AssertionConsumerService assertionConsumerService(String bindingName, String endpointUrl) {
        AssertionConsumerService service = createSamlObject(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        service.setBinding(bindingName);
        service.setLocation(endpointUrl);

        return service;
    }

    private KeyDescriptor keyDescritor(X509Certificate certificate, UsageType usage) {
        KeyDescriptor keyDescriptor = createSamlObject(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        keyDescriptor.setUse(usage);

        KeyInfo keyInfo = createXmlObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = createXmlObject(X509Data.DEFAULT_ELEMENT_NAME);
        org.opensaml.xmlsec.signature.X509Certificate x509Cert = createXmlObject(
            org.opensaml.xmlsec.signature.X509Certificate.DEFAULT_ELEMENT_NAME
        );

        try {
            x509Cert.setValue(Base64.encodeBase64String(certificate.getEncoded()));
        } catch (CertificateEncodingException e) {
            throw new Saml2Exception("Error encoding certificate", e);
        }

        x509Data.getX509Certificates().add(x509Cert);

        keyInfo.getX509Datas().add(x509Data);
        keyDescriptor.setKeyInfo(keyInfo);

        if (usage == UsageType.ENCRYPTION) {
            addEncryptionMethods(keyDescriptor);
        }

        return keyDescriptor;
    }

    private void addEncryptionMethods(KeyDescriptor keyDescriptor) {
        EncryptionConfiguration config = SecurityConfigurationSupport.getGlobalEncryptionConfiguration();

        Set<String> allowedAlgorithms = config //
            .getDataEncryptionAlgorithms()
            .stream()
            .filter(algorithm ->
                AlgorithmSupport.validateAlgorithmURI(
                    algorithm,
                    config.getIncludedAlgorithms(),
                    config.getExcludedAlgorithms()
                )
            )
            .collect(Collectors.toSet());

        for (String allowedAlgorithm : allowedAlgorithms) {
            keyDescriptor.getEncryptionMethods().add(encryptionMethod(allowedAlgorithm));
        }
    }

    private EncryptionMethod encryptionMethod(String algorithm) {
        EncryptionMethod method = createSamlObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
        method.setAlgorithm(algorithm);

        return method;
    }

    private SigningMethod signingMethod(String algorithm) {
        SigningMethod method = createSamlObject(SigningMethod.DEFAULT_ELEMENT_NAME);
        method.setAlgorithm(algorithm);

        return method;
    }

    private DigestMethod digestMethod(String algorithm) {
        DigestMethod method = createSamlObject(DigestMethod.DEFAULT_ELEMENT_NAME);
        method.setAlgorithm(algorithm);

        return method;
    }

    /**
     * Creates a SAML attribute.
     *
     * @param name the name of the attribute
     * @param nameFormat the name format
     * @return the attribute
     */
    private Attribute attribute(String name, String nameFormat) {
        Attribute attribute = createSamlObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(name);
        attribute.setNameFormat(nameFormat);

        return attribute;
    }
}
