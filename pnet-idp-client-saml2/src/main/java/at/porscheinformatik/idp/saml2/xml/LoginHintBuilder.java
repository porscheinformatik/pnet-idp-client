package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.LoginHint;
import org.opensaml.core.xml.AbstractXMLObjectBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoginHintBuilder extends AbstractXMLObjectBuilder<LoginHint> {
    @Nonnull
    @Override
    public LoginHint buildObject(@Nullable String namespaceURI, @Nonnull String localName, @Nullable String namespacePrefix) {
        return new LoginHintImpl(namespaceURI, localName, namespacePrefix);
    }
}
