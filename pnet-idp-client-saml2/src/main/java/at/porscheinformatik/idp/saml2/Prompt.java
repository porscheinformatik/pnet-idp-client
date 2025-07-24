package at.porscheinformatik.idp.saml2;

import org.opensaml.core.xml.XMLObject;

public interface Prompt extends XMLObject {
    String getPrompt();

    void setPrompt(String prompt);
}
