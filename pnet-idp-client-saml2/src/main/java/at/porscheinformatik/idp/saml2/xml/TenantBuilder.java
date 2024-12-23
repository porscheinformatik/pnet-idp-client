package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.Tenant;
import org.opensaml.core.xml.AbstractXMLObjectBuilder;

public class TenantBuilder extends AbstractXMLObjectBuilder<Tenant> {

    @Override
    public Tenant buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new TenantImpl(namespaceURI, localName, namespacePrefix);
    }
}
