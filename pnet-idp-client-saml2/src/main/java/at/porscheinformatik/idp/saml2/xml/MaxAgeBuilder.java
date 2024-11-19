package at.porscheinformatik.idp.saml2.xml;

import org.opensaml.core.xml.AbstractXMLObjectBuilder;

import at.porscheinformatik.idp.saml2.MaxAge;

public class MaxAgeBuilder extends AbstractXMLObjectBuilder<MaxAge>
{

    @Override
    public MaxAge buildObject(String namespaceURI, String localName, String namespacePrefix)
    {
        return new MaxAgeImpl(namespaceURI, localName, namespacePrefix);
    }
}
