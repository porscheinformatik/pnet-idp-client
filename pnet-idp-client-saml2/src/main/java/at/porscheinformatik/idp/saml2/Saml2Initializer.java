/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static org.opensaml.xmlsec.encryption.support.EncryptionConstants.*;
import static org.opensaml.xmlsec.signature.support.SignatureConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import at.porscheinformatik.idp.saml2.xml.*;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.impl.BasicDecryptionConfiguration;
import org.opensaml.xmlsec.impl.BasicEncryptionConfiguration;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.impl.BasicSignatureValidationConfiguration;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.springframework.security.saml2.core.OpenSamlInitializationService;

/**
 * @author Daniel Furtlehner
 */
public final class Saml2Initializer
{
    // Supported algorithms https://kantarainitiative.github.io/SAMLprofiles/saml2int.html#_cryptographic_algorithms
    private static final Collection<String> SIGNATURE_ALGORITHMS = Arrays
        .asList(ALGO_ID_SIGNATURE_RSA_SHA256, ALGO_ID_SIGNATURE_ECDSA_SHA256, SignatureConstants.ALGO_ID_DIGEST_SHA256);

    private static final Collection<String> ENCRYPTION_ALGORITHMS = Arrays
        .asList(ALGO_ID_BLOCKCIPHER_AES128_GCM, ALGO_ID_BLOCKCIPHER_AES256_GCM,
            EncryptionConstants.ALGO_ID_DIGEST_SHA256, ALGO_ID_KEYTRANSPORT_RSAOAEP, ALGO_ID_KEYTRANSPORT_RSAOAEP11);

    private Saml2Initializer()
    {
        super();
    }

    public static void initialize()
    {
        OpenSamlInitializationService.initialize();

        setupExtensions();
        setupSignatureAlgorithmWhitelists();
        setupEncryptionAlgorithmWhitelist();
    }

    private static void setupExtensions()
    {
        XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);

        registry
            .registerObjectProvider(XmlUtils.MAX_SESSION_AGE_ELEMENT_NAME, new MaxAgeBuilder(),
                new MaxAgeMarshaller(), new MaxAgeUnmarshaller());

        registry
            .registerObjectProvider(XmlUtils.MAX_AGE_MFA_ELEMENT_NAME, new MaxAgeBuilder(),
                new MaxAgeMarshaller(), new MaxAgeUnmarshaller());

        registry
            .registerObjectProvider(XmlUtils.TENANT_ELEMENT_NAME, new TenantBuilder(), new TenantMarshaller(),
                new TenantUnmarshaller());
    }

    private static void setupSignatureAlgorithmWhitelists()
    {
        BasicSignatureValidationConfiguration validationConfiguration =
            (BasicSignatureValidationConfiguration) SecurityConfigurationSupport
                .getGlobalSignatureValidationConfiguration();
        BasicSignatureSigningConfiguration signingConfiguration =
            (BasicSignatureSigningConfiguration) SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();

        validationConfiguration.setIncludedAlgorithms(SIGNATURE_ALGORITHMS);
        signingConfiguration.setIncludedAlgorithms(SIGNATURE_ALGORITHMS);
    }

    private static void setupEncryptionAlgorithmWhitelist()
    {
        BasicDecryptionConfiguration decryptionConfiguration =
            (BasicDecryptionConfiguration) SecurityConfigurationSupport.getGlobalDecryptionConfiguration();
        BasicEncryptionConfiguration encryptionConfiguration =
            (BasicEncryptionConfiguration) SecurityConfigurationSupport.getGlobalEncryptionConfiguration();

        decryptionConfiguration.setIncludedAlgorithms(ENCRYPTION_ALGORITHMS);
        encryptionConfiguration.setIncludedAlgorithms(ENCRYPTION_ALGORITHMS);

        // The only two blockciphers we have to support are not registered by default. lets do this here.
        List<String> dataEncryptionAlgorithms = new ArrayList<>(encryptionConfiguration.getDataEncryptionAlgorithms());

        dataEncryptionAlgorithms.add(ALGO_ID_BLOCKCIPHER_AES128_GCM);
        dataEncryptionAlgorithms.add(ALGO_ID_BLOCKCIPHER_AES256_GCM);

        encryptionConfiguration.setDataEncryptionAlgorithms(dataEncryptionAlgorithms);
    }
}
