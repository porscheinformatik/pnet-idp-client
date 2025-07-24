package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.LoginHint;
import jakarta.annotation.Nonnull;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;


public class LoginHintMarshaller extends AbstractXMLObjectMarshaller {

    @Override
    protected void marshallElementContent(@Nonnull XMLObject xmlObject, @Nonnull Element domElement) throws MarshallingException {
        LoginHint element = (LoginHint) xmlObject;

        ElementSupport.appendTextContent(domElement, element.getLoginHint());
    }
}
