package at.porscheinformatik.idp.saml2;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class DefaultSaml2CredentialsManager implements Saml2CredentialsManager {

    private static final String MISSING_KEYINFO_MESSAGE =
        "At least one Saml2KeyInfoConfiguration with usage = DECRYPTION must be set. Partner.Net Authentication does not work without a decryption key.";
    private static final Duration WARNING_TRESHOLD = Duration.ofDays(60);
    private static final long NOTIFICATION_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L;
    private static final Logger LOG = LoggerFactory.getLogger(Saml2CredentialsManager.class);

    private final Supplier<List<Saml2CredentialsConfig>> configSupplier;
    private final List<UpdateListener> listeners = new ArrayList<>();

    private long lastupdate = -1;
    private long lastNotificationSent = -1;
    private List<Saml2X509Credential> credentials = Collections.emptyList();
    private List<Saml2CredentialsConfig> actualConfig;

    public DefaultSaml2CredentialsManager(Supplier<List<Saml2CredentialsConfig>> configSupplier) {
        super();
        this.configSupplier = configSupplier;
    }

    @PostConstruct
    public void initialize() throws Exception {
        update();
    }

    @Override
    public List<Saml2X509Credential> getCredentials() {
        return credentials;
    }

    @Override
    public void onUpdate(UpdateListener action) {
        listeners.add(action);
    }

    /**
     * Checks for updates and reloads the certificate and keystore if needed.
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void refresh() {
        try {
            update();
        } catch (Exception ex) {
            LOG.error("Error updating saml credentials", ex);
        }
    }

    public synchronized void update() throws Exception {
        //Remember the new lastupdate before the check. Otherwise we might lose the check time later on
        long newLastupdate = System.currentTimeMillis();
        List<Saml2CredentialsConfig> newConfig = configSupplier.get();

        if (mustReload(newConfig)) {
            setupEntries(newConfig);
            callListeners();
        }

        if (CollectionUtils.isEmpty(credentials)) {
            LOG.error("No valid keyInfo configured!");
        }

        lastupdate = newLastupdate;
        actualConfig = newConfig;
    }

    private void callListeners() {
        for (UpdateListener listener : listeners) {
            try {
                listener.onUpdate();
            } catch (Exception e) {
                LOG.error("Error calling update listener", e);
            }
        }
    }

    private void setupEntries(List<Saml2CredentialsConfig> newConfig) throws Exception {
        validateNewConfig(newConfig);

        List<Saml2X509Credential> newEntries = new ArrayList<>();

        //Create a key info for each config entry
        for (Saml2CredentialsConfig config : newConfig) {
            Resource location = config.getKeystoreLocation();
            String type = config.getKeystoreType();
            String password = config.getKeystorePassword();
            String privateAlias = config.getPrivateAlias();
            String publicAlias = config.getPublicAlias();
            Saml2X509CredentialType usage = config.getUsage();

            if (!location.isReadable()) {
                throw new IllegalArgumentException(String.format("keystore [%s] is not readable", location));
            }

            Assert.hasText(type, "type for location [" + location + "] must not be null");
            Assert.hasText(password, "password for location [" + location + "] must not be null");
            Assert.hasText(privateAlias, "privatealias  for location [" + location + "] must not be null");
            Assert.hasText(publicAlias, "publicalias for location [" + location + "] must not be null");
            Assert.notNull(usage, "usage for location [" + location + "] must not be null");

            KeyStore keystore = createKeystore(location, type, password);
            X509Certificate publicKey = extractPublicKey(keystore, publicAlias);
            PrivateKey privateKey = extractPrivateKey(keystore, password, privateAlias);

            Assert.notNull(privateKey, "Private key for location [" + location + "] is null");
            Assert.notNull(publicKey, "Public key for location [" + location + "] is null");

            newEntries.add(new Saml2X509Credential(privateKey, publicKey, usage));
        }

        removeOutdatedCertificates(newEntries);

        credentials = newEntries;
    }

    private void validateNewConfig(List<Saml2CredentialsConfig> newConfig) {
        Assert.notEmpty(newConfig, MISSING_KEYINFO_MESSAGE);

        if (newConfig.stream().noneMatch(entry -> entry.getUsage() == Saml2X509CredentialType.DECRYPTION)) {
            throw new IllegalArgumentException(MISSING_KEYINFO_MESSAGE);
        }
    }

    /**
     * Checks each certificate and remove it from the list if it is outdated.
     *
     * @param entries the entries to check
     */
    private void removeOutdatedCertificates(List<Saml2X509Credential> entries) {
        Iterator<Saml2X509Credential> it = entries.iterator();

        while (it.hasNext()) {
            Saml2X509Credential entry = it.next();

            X509Certificate cert = entry.getCertificate();

            //Wenn nicht mehr gültig --> remove
            Date now = new Date();
            Date notAfter = cert.getNotAfter();

            if (notAfter.before(now)) {
                it.remove();
            } else {
                // Check if the certificate will expire soon and log an error so that it gets replaced
                Date monthAgo = new Date(notAfter.getTime() - (WARNING_TRESHOLD.getSeconds() * 1000));
                long nowMillis = System.currentTimeMillis();

                // When outdated we sent the message once every hour
                if (
                    monthAgo.before(now) &&
                    (lastNotificationSent == -1 || lastNotificationSent + NOTIFICATION_INTERVAL_MILLIS < nowMillis)
                ) {
                    LOG.error(
                        "A Certificate in the configuration will expire on {} Configure a new Keystore in addition to the current one. ",
                        cert.getNotAfter()
                    );

                    lastNotificationSent = nowMillis;
                }
            }
        }
    }

    private KeyStore createKeystore(Resource location, String type, String password) throws Exception {
        LOG.info("Creating keystore [{}] of type [{}]", location.getDescription(), type);

        try (InputStream stream = location.getInputStream()) {
            KeyStore keystore = KeyStore.getInstance(type);
            keystore.load(stream, password.toCharArray());

            return keystore;
        }
    }

    private PrivateKey extractPrivateKey(KeyStore keystore, String password, String alias)
        throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        LOG.info("Extracting Certificate [{}]", alias);

        return (PrivateKey) keystore.getKey(alias, password.toCharArray());
    }

    private X509Certificate extractPublicKey(KeyStore keystore, String alias)
        throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        LOG.info("Extracting Certificate [{}]", alias);

        return (X509Certificate) keystore.getCertificate(alias);
    }

    /**
     * Check if it is neccessary to reload the entries. Only when some entries changed or
     *
     * @return true when something changed. false otherwise
     * @throws IOException wenn etwas beim lastupdate lesen schiefgeht
     */
    private boolean mustReload(List<Saml2CredentialsConfig> newConfig) throws IOException {
        //First time we load the config. So update is needed
        if (CollectionUtils.isEmpty(actualConfig)) {
            return true;
        }

        if (newConfig.size() != actualConfig.size()) {
            return true;
        }

        for (Saml2CredentialsConfig config : newConfig) {
            // One of the keystores changed. Reload
            if (config.getKeystoreLocation().lastModified() > lastupdate) {
                return true;
            }
        }

        return false;
    }

    public static final class Saml2CredentialsConfig {

        private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

        public static Saml2CredentialsConfig signingKey(
            String keystoreResourceLocation,
            String keystoreType,
            String keystorePassword,
            String privateAlias,
            String publicAlias
        ) {
            Resource keystoreLocation = RESOURCE_LOADER.getResource(keystoreResourceLocation);

            return new Saml2CredentialsConfig(
                keystoreLocation,
                keystoreType,
                keystorePassword,
                privateAlias,
                publicAlias,
                Saml2X509CredentialType.SIGNING
            );
        }

        public static Saml2CredentialsConfig decryptionKey(
            String keystoreResourceLocation,
            String keystoreType,
            String keystorePassword,
            String privateAlias,
            String publicAlias
        ) {
            Resource keystoreLocation = RESOURCE_LOADER.getResource(keystoreResourceLocation);

            return new Saml2CredentialsConfig(
                keystoreLocation,
                keystoreType,
                keystorePassword,
                privateAlias,
                publicAlias,
                Saml2X509CredentialType.DECRYPTION
            );
        }

        private final Resource keystoreLocation;
        private final String keystoreType;
        private final String keystorePassword;
        private final String privateAlias;
        private final String publicAlias;
        private final Saml2X509CredentialType usage;

        public Saml2CredentialsConfig(
            Resource keystoreLocation,
            String keystoreType,
            String keystorePassword,
            String privateAlias,
            String publicAlias,
            Saml2X509CredentialType usage
        ) {
            super();
            this.keystoreLocation = keystoreLocation;
            this.keystoreType = keystoreType;
            this.keystorePassword = keystorePassword;
            this.privateAlias = privateAlias;
            this.publicAlias = publicAlias;
            this.usage = usage;
        }

        public Resource getKeystoreLocation() {
            return keystoreLocation;
        }

        public String getKeystoreType() {
            return keystoreType;
        }

        public String getKeystorePassword() {
            return keystorePassword;
        }

        public String getPrivateAlias() {
            return privateAlias;
        }

        public String getPublicAlias() {
            return publicAlias;
        }

        public Saml2X509CredentialType getUsage() {
            return usage;
        }

        @Override
        public int hashCode() {
            return Objects.hash(keystoreLocation);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            Saml2CredentialsConfig other = (Saml2CredentialsConfig) obj;

            return Objects.equals(keystoreLocation, other.keystoreLocation);
        }
    }
}
