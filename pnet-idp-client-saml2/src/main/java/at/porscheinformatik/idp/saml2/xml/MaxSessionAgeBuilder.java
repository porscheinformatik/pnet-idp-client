package at.porscheinformatik.idp.saml2.xml;

import org.opensaml.core.xml.AbstractXMLObjectBuilder;

import at.porscheinformatik.idp.saml2.MaxSessionAge;

public class MaxSessionAgeBuilder extends AbstractXMLObjectBuilder<MaxSessionAge>
{

    @Override
    public MaxSessionAge buildObject(String namespaceURI, String localName, String namespacePrefix)
    {
        return new MaxSessionAgeImpl(namespaceURI, localName, namespacePrefix);
    }
}
