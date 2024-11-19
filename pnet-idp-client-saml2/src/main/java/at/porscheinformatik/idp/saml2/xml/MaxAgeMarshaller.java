package at.porscheinformatik.idp.saml2.xml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;

import at.porscheinformatik.idp.saml2.MaxAge;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

public class MaxAgeMarshaller extends AbstractXMLObjectMarshaller
{

    @Override
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException
    {
        MaxAge element = (MaxAge) xmlObject;

        ElementSupport.appendTextContent(domElement, Integer.toString(element.getMaxAgeInSeconds()));
    }

}
