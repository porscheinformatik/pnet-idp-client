package at.porscheinformatik.idp.saml2;

import at.porscheinformatik.idp.saml2.DefaultSaml2CredentialsManager.Saml2CredentialsConfig;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.util.CollectionUtils;

@ConfigurationProperties("poi.saml2")
public class Saml2CredentialsProperties implements Supplier<List<Saml2CredentialsConfig>> {

    private List<Saml2CredentialsProperty> credentials;

    @Override
    public List<Saml2CredentialsConfig> get() {
        if (CollectionUtils.isEmpty(credentials)) {
            return Collections.emptyList();
        }

        return credentials //
            .stream()
            .map(this::toConfig)
            .toList();
    }

    private Saml2CredentialsConfig toConfig(Saml2CredentialsProperty property) {
        return new Saml2CredentialsConfig(
            property.getLocation(),
            property.getType(),
            property.getPassword(),
            property.getPrivateAlias(),
            property.getPublicAlias(),
            property.getUsage()
        );
    }

    public List<Saml2CredentialsProperty> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Saml2CredentialsProperty> credentials) {
        this.credentials = credentials;
    }

    public static final class Saml2CredentialsProperty {

        private Resource location;
        private String type;
        private String password;
        private String privateAlias;
        private String publicAlias;
        private Saml2X509CredentialType usage;

        public Resource getLocation() {
            return location;
        }

        public void setLocation(Resource location) {
            this.location = location;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPrivateAlias() {
            return privateAlias;
        }

        public void setPrivateAlias(String privateAlias) {
            this.privateAlias = privateAlias;
        }

        public String getPublicAlias() {
            return publicAlias;
        }

        public void setPublicAlias(String publicAlias) {
            this.publicAlias = publicAlias;
        }

        public Saml2X509CredentialType getUsage() {
            return usage;
        }

        public void setUsage(Saml2X509CredentialType usage) {
            this.usage = usage;
        }
    }
}
