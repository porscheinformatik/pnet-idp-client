package at.porscheinformatik.idp.saml2.xml;

import java.util.List;

import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;

import at.porscheinformatik.idp.saml2.MaxAge;

public class MaxAgeImpl extends AbstractXMLObject implements MaxAge
{
    private int value;

    MaxAgeImpl(String namespaceURI, String elementLocalName, String namespacePrefix)
    {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public List<XMLObject> getOrderedChildren()
    {
        return null;
    }

    @Override
    public int getMaxAgeInSeconds()
    {
        return value;
    }

    @Override
    public void setMaxAgeInSeconds(int maxAgeInSeconds)
    {
        value = prepareForAssignment(value, maxAgeInSeconds);
    }

}
