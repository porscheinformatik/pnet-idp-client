package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.LoginHint;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class LoginHintImpl extends AbstractXMLObject implements LoginHint {
    private String value;

    protected LoginHintImpl(@Nullable String namespaceURI, @Nonnull String elementLocalName, @Nullable String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }

    @Override
    public String getLoginHint() {
        return value;
    }

    @Override
    public void setLoginHint(String loginHint) {
        value = prepareForAssignment(value, loginHint);
    }
}
