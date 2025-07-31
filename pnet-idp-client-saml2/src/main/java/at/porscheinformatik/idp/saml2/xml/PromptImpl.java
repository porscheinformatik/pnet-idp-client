package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.Prompt;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;

public class PromptImpl extends AbstractXMLObject implements Prompt {

    private String value;

    protected PromptImpl(
        @Nullable String namespaceURI,
        @Nonnull String elementLocalName,
        @Nullable String namespacePrefix
    ) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }

    @Override
    public String getPrompt() {
        return value;
    }

    @Override
    public void setPrompt(String prompt) {
        value = prepareForAssignment(value, prompt);
    }
}
