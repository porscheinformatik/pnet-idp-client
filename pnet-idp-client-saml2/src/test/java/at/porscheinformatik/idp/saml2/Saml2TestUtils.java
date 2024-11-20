package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.DefaultSaml2CredentialsManager.Saml2CredentialsConfig.*;

import at.porscheinformatik.idp.saml2.DefaultSaml2CredentialsManager.Saml2CredentialsConfig;
import java.util.Arrays;
import java.util.List;

public class Saml2TestUtils {

    public static List<Saml2CredentialsConfig> defaultCredentials() {
        return Arrays.asList(
            signingKey(
                "classpath:at/porscheinformatik/idp/saml2/keys.keystore",
                "PKCS12",
                "somepass",
                "myprivate",
                "mypublic"
            ),
            decryptionKey(
                "classpath:at/porscheinformatik/idp/saml2/keys.keystore",
                "PKCS12",
                "somepass",
                "myprivate",
                "mypublic"
            )
        );
    }

    public static Saml2CredentialsManager defaultCredentialsManager() throws Exception {
        DefaultSaml2CredentialsManager manager = new DefaultSaml2CredentialsManager(Saml2TestUtils::defaultCredentials);
        manager.initialize();

        return manager;
    }
}
