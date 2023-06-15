package at.porscheinformatik.idp.saml2.xml;

import java.util.List;

import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;

import at.porscheinformatik.idp.saml2.MaxSessionAge;

public class MaxSessionAgeImpl extends AbstractXMLObject implements MaxSessionAge
{
    private int value;

    MaxSessionAgeImpl(String namespaceURI, String elementLocalName, String namespacePrefix)
    {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public List<XMLObject> getOrderedChildren()
    {
        return null;
    }

    @Override
    public int getSessionAgeInSeconds()
    {
        return value;
    }

    @Override
    public void setSessionAgeInSeconds(int sessionAgeInSeconds)
    {
        value = prepareForAssignment(value, sessionAgeInSeconds);
    }

}
