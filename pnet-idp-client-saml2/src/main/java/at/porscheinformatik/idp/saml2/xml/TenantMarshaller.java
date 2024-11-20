package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.Tenant;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;

public class TenantMarshaller extends AbstractXMLObjectMarshaller {

    @Override
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException {
        Tenant element = (Tenant) xmlObject;

        ElementSupport.appendTextContent(domElement, element.getTenant());
    }
}
