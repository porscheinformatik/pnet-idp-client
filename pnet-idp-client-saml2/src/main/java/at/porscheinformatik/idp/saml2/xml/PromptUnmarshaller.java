package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.Prompt;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;

public class PromptUnmarshaller extends AbstractXMLObjectUnmarshaller {

    @Override
    protected void processElementContent(final XMLObject xmlObject, final String elementContent) {
        Prompt prompt = (Prompt) xmlObject;

        if (elementContent != null) {
            prompt.setPrompt(elementContent.trim());
        }
    }
}
