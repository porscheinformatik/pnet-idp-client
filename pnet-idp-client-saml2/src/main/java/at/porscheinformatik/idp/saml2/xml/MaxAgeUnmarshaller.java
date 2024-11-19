package at.porscheinformatik.idp.saml2.xml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;

import at.porscheinformatik.idp.saml2.MaxAge;

public class MaxAgeUnmarshaller extends AbstractXMLObjectUnmarshaller
{
    @Override
    protected void processElementContent(final XMLObject xmlObject, final String elementContent)
    {
        MaxAge maxAge = (MaxAge) xmlObject;

        if (elementContent != null)
        {
            maxAge.setMaxAgeInSeconds(Integer.parseInt(elementContent.trim()));
        }
    }

}
