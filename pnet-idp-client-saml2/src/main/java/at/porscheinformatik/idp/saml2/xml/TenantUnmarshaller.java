package at.porscheinformatik.idp.saml2.xml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;

import at.porscheinformatik.idp.saml2.Tenant;

public class TenantUnmarshaller extends AbstractXMLObjectUnmarshaller
{
    @Override
    protected void processElementContent(final XMLObject xmlObject, final String elementContent)
    {
        Tenant tenant = (Tenant) xmlObject;

        if (elementContent != null)
        {
            tenant.setTenant(elementContent.trim());
        }
    }
}
