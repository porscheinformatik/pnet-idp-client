package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.Prompt;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opensaml.core.xml.AbstractXMLObjectBuilder;

public class PromptBuilder extends AbstractXMLObjectBuilder<Prompt> {

    @Nonnull
    @Override
    public Prompt buildObject(
        @Nullable String namespaceURI,
        @Nonnull String localName,
        @Nullable String namespacePrefix
    ) {
        return new PromptImpl(namespaceURI, localName, namespacePrefix);
    }
}
