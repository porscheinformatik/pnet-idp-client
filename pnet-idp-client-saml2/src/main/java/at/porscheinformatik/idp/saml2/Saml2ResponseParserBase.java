/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.XmlUtils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.Saml2Exception;

/**
 * @author Daniel Furtlehner
 */
public abstract class Saml2ResponseParserBase implements Saml2ResponseParser
{

    @Override
    public Authentication parseResponse(Response samlResponse, Optional<String> relayState) throws Saml2Exception
    {
        Saml2Data data = doParseResponse(samlResponse, relayState);

        return convert(data);
    }

    protected abstract Authentication convert(Saml2Data data) throws Saml2Exception;

    private Saml2Data doParseResponse(Response samlResponse, Optional<String> relayState)
    {
        String subjectIdentifier = null;
        Map<String, Serializable> additionalAttributes = new HashMap<>();
        Assertion assertion = samlResponse.getAssertions().get(0);
        String nameId = assertion.getSubject().getNameID().getValue();

        AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);

        for (Attribute attribute : attributeStatement.getAttributes())
        {
            if (isSubjectIdentifierAttribute(attribute))
            {
                subjectIdentifier = (String) getXmlValue(attribute.getAttributeValues().get(0));
            }
            else if (attribute.getAttributeValues().size() == 1)
            {
                additionalAttributes.put(attribute.getName(), getXmlValue(attribute.getAttributeValues().get(0)));
            }
            else
            {
                ArrayList<Serializable> values = new ArrayList<>();

                for (XMLObject attributeValue : attribute.getAttributeValues())
                {
                    values.add(getXmlValue(attributeValue));
                }

                additionalAttributes.put(attribute.getName(), values);
            }
        }

        AuthnContextClass authnContextClass = AuthnContextClass
            .fromReference(authnStatement.getAuthnContext().getAuthnContextClassRef().getURI())
            .orElse(AuthnContextClass.NONE);

        return new Saml2Data(subjectIdentifier, nameId, additionalAttributes, relayState, authnContextClass);
    }

    private boolean isSubjectIdentifierAttribute(Attribute attribute)
    {
        return Objects.equals(attribute.getName(), Saml2Utils.SUBJECT_ID_NAME)
            || Objects.equals(attribute.getName(), Saml2Utils.PAIRWISE_ID_NAME);
    }

    public static class Saml2Data
    {
        private final String subjectIdentifier;
        private final String nameId;
        private final Map<String, Serializable> samlAttributes;
        private final Optional<String> relayState;
        private final AuthnContextClass authnContextClass;

        Saml2Data(String subjectIdentifier, String nameId, Map<String, Serializable> samlAttributes,
            Optional<String> relayState, AuthnContextClass authnContextClass)
        {
            this.subjectIdentifier = subjectIdentifier;
            this.nameId = nameId;
            this.samlAttributes = samlAttributes;
            this.relayState = relayState;
            this.authnContextClass = authnContextClass;
        }

        public String getSubjectIdentifier()
        {
            return subjectIdentifier;
        }

        public String getNameId()
        {
            return nameId;
        }

        public Map<String, Serializable> getSamlAttributes()
        {
            return samlAttributes;
        }

        public Optional<String> getRelayState()
        {
            return relayState;
        }

        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String name)
        {
            return (T) samlAttributes.get(name);
        }

        public AuthnContextClass getAuthnContextClass()
        {
            return authnContextClass;
        }

    }
}
