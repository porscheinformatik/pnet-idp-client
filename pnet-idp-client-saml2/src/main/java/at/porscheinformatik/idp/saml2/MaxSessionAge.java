package at.porscheinformatik.idp.saml2;

import org.opensaml.core.xml.XMLObject;

public interface MaxSessionAge extends XMLObject
{

    int getSessionAgeInSeconds();

    void setSessionAgeInSeconds(int sessionAgeInSeconds);
}
