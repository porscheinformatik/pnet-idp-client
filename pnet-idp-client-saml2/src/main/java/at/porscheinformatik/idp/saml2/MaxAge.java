package at.porscheinformatik.idp.saml2;

import org.opensaml.core.xml.XMLObject;

public interface MaxAge extends XMLObject
{

    int getMaxAgeInSeconds();

    void setMaxAgeInSeconds(int maxAgeInSeconds);
}
