package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.Prompt;
import jakarta.annotation.Nonnull;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;


public class PromptMarshaller extends AbstractXMLObjectMarshaller {

    @Override
    protected void marshallElementContent(@Nonnull XMLObject xmlObject, @Nonnull Element domElement) throws MarshallingException {
        Prompt element = (Prompt) xmlObject;

        ElementSupport.appendTextContent(domElement, element.getPrompt());
    }
}
