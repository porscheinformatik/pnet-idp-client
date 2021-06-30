package at.porscheinformatik.idp.saml2.xml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;

import at.porscheinformatik.idp.saml2.MaxSessionAge;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

public class MaxSessionAgeMarshaller extends AbstractXMLObjectMarshaller
{

    @Override
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException
    {
        MaxSessionAge element = (MaxSessionAge) xmlObject;

        ElementSupport.appendTextContent(domElement, Integer.toString(element.getSessionAgeInSeconds()));
    }

}
