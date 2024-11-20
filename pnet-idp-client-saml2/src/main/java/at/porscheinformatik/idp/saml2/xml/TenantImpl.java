package at.porscheinformatik.idp.saml2.xml;

import at.porscheinformatik.idp.saml2.Tenant;
import java.util.List;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;

public class TenantImpl extends AbstractXMLObject implements Tenant {

    private String value;

    TenantImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }

    @Override
    public String getTenant() {
        return value;
    }

    @Override
    public void setTenant(String tenant) {
        value = prepareForAssignment(value, tenant);
    }
}
