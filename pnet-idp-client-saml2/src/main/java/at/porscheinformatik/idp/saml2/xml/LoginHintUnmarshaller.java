package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.LoginHint;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;

public class LoginHintUnmarshaller extends AbstractXMLObjectUnmarshaller {

    @Override
    protected void processElementContent(final XMLObject xmlObject, final String elementContent) {
        LoginHint loginHint = (LoginHint) xmlObject;

        if (elementContent != null) {
            loginHint.setLoginHint(elementContent.trim());
        }
    }
}
