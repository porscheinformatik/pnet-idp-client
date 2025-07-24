package at.porscheinformatik.idp.saml2;

import org.opensaml.core.xml.XMLObject;

public interface LoginHint extends XMLObject {
    String getLoginHint();

    void setLoginHint(String loginHint);
}
