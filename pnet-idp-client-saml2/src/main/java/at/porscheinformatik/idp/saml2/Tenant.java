package at.porscheinformatik.idp.saml2;

import org.opensaml.core.xml.XMLObject;

public interface Tenant extends XMLObject
{
    String getTenant();

    void setTenant(String tenant);
}
