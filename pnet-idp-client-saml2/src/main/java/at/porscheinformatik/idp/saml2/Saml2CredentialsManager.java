package at.porscheinformatik.idp.saml2;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;

public interface Saml2CredentialsManager
{
    /**
     * @return a list of credentials used by the relying party
     */
    @Nonnull
    List<Saml2X509Credential> getCredentials();

    /**
     * Registers a listener that is called when the underlying list of certificates changes.
     * 
     * @param action the action to perform on update
     */
    void onUpdate(UpdateListener action);

    default List<Saml2X509Credential> getCredentials(Saml2X509CredentialType type)
    {
        return getCredentials()
            .stream()
            .filter(credential -> credential.getCredentialTypes().contains(type))
            .collect(Collectors.toList());
    }

}
