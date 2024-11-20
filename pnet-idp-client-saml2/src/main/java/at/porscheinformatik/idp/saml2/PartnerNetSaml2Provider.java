/**
 *
 */
package at.porscheinformatik.idp.saml2;

/**
 * @author Daniel Furtlehner
 */
public enum PartnerNetSaml2Provider {
    PROD {
        @Override
        public String getEntityId() {
            return "https://identity.auto-partner.net/identity/saml2";
        }
    },
    QA {
        @Override
        public String getEntityId() {
            return "https://qa-identity.auto-partner.net/identity/saml2";
        }
    },
    DEV {
        @Override
        public String getEntityId() {
            return "https://pnet-identity-web-app-dev.nonprod1.ocp.porscheinformatik.cloud/identity/saml2";
        }
    },
    LOCAL {
        @Override
        public String getEntityId() {
            return "https://localhost:5443/identity/saml2";
        }
    };

    /**
     * Get the entity id of the Identity Provider
     *
     * @return the entity id
     */
    public abstract String getEntityId();
}
