package at.porscheinformatik.idp.saml2.xml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;

import at.porscheinformatik.idp.saml2.MaxSessionAge;

public class MaxSessionAgeUnmarshaller extends AbstractXMLObjectUnmarshaller
{
    @Override
    protected void processElementContent(final XMLObject xmlObject, final String elementContent)
    {
        MaxSessionAge sessionAge = (MaxSessionAge) xmlObject;

        if (elementContent != null)
        {
            sessionAge.setSessionAgeInSeconds(Integer.parseInt(elementContent.trim()));
        }
    }

}
