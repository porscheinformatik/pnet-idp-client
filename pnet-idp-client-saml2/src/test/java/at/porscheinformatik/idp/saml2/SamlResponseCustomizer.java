/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static java.time.temporal.ChronoUnit.*;
import static java.util.Arrays.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SubjectConfirmation;

/**
 * @author Daniel Furtlehner
 */
@FunctionalInterface
public interface SamlResponseCustomizer
{
    static SamlResponseCustomizer outdatedResponse()
    {
        return response -> response.setIssueInstant(Instant.now().minus(1, HOURS));
    }

    static SamlResponseCustomizer noIssuer()
    {
        return response -> response.setIssuer(null);
    }

    static SamlResponseCustomizer issuer(String issuer)
    {
        return response -> response.getIssuer().setValue(issuer);
    }

    static SamlResponseCustomizer issuerFormat(String issuerFormat)
    {
        return response -> response.getIssuer().setFormat(issuerFormat);
    }

    static SamlResponseCustomizer noId()
    {
        return response -> response.setID(null);
    }

    static SamlResponseCustomizer noAssertion()
    {
        return response -> response.getAssertions().clear();
    }

    static SamlResponseCustomizer assertion(Assertion assertion)
    {
        return response -> response.getAssertions().add(assertion);
    }

    static SamlResponseCustomizer noAuthnStatement()
    {
        return response -> response.getAssertions().get(0).getAuthnStatements().clear();
    }

    static SamlResponseCustomizer authnStatement(AuthnStatement authnStatement)
    {
        return response -> response.getAssertions().get(0).getAuthnStatements().add(authnStatement);
    }

    static SamlResponseCustomizer noAttributeStatement()
    {
        return response -> response.getAssertions().get(0).getAttributeStatements().clear();
    }

    static SamlResponseCustomizer attributeStatement(AttributeStatement authnStatement)
    {
        return response -> response.getAssertions().get(0).getAttributeStatements().add(authnStatement);
    }

    static SamlResponseCustomizer noSubject()
    {
        return response -> response.getAssertions().get(0).setSubject(null);
    }

    static SamlResponseCustomizer noSubjectConfirmation()
    {
        return response -> response.getAssertions().get(0).getSubject().getSubjectConfirmations().clear();
    }

    static SamlResponseCustomizer wrongSubjectConfirmationMethod()
    {
        return response -> {
            List<SubjectConfirmation> subjectConfirmations =
                response.getAssertions().get(0).getSubject().getSubjectConfirmations();
            subjectConfirmations.get(0).setMethod(SubjectConfirmation.METHOD_HOLDER_OF_KEY);
        };
    }

    static SamlResponseCustomizer noSubjectConfirmationData()
    {
        return response -> {
            List<SubjectConfirmation> subjectConfirmations =
                response.getAssertions().get(0).getSubject().getSubjectConfirmations();
            subjectConfirmations.get(0).setSubjectConfirmationData(null);
        };
    }

    static SamlResponseCustomizer wrongRecipient()
    {
        return response -> {
            List<SubjectConfirmation> subjectConfirmations =
                response.getAssertions().get(0).getSubject().getSubjectConfirmations();
            subjectConfirmations.get(0).getSubjectConfirmationData().setRecipient("wrong");
        };
    }

    static SamlResponseCustomizer outdatedSubjectConfirmation()
    {
        return response -> {
            List<SubjectConfirmation> subjectConfirmations =
                response.getAssertions().get(0).getSubject().getSubjectConfirmations();
            subjectConfirmations.get(0).getSubjectConfirmationData().setNotOnOrAfter(Instant.now().minus(1, HOURS));
        };
    }

    static SamlResponseCustomizer notBeforeOnSubjectConfirmation()
    {
        return response -> {
            List<SubjectConfirmation> subjectConfirmations =
                response.getAssertions().get(0).getSubject().getSubjectConfirmations();
            subjectConfirmations.get(0).getSubjectConfirmationData().setNotBefore(Instant.now().minusSeconds(1));
        };
    }

    static SamlResponseCustomizer wrongInResponseTo()
    {
        return response -> {
            List<SubjectConfirmation> subjectConfirmations =
                response.getAssertions().get(0).getSubject().getSubjectConfirmations();
            subjectConfirmations.get(0).getSubjectConfirmationData().setInResponseTo("wrong");
        };
    }

    static SamlResponseCustomizer noAudienceRestriction()
    {
        return response -> {
            Assertion assertion = response.getAssertions().get(0);

            assertion.getConditions().getAudienceRestrictions().clear();
        };
    }

    static SamlResponseCustomizer emptyConditions()
    {
        return response -> {
            Assertion assertion = response.getAssertions().get(0);

            Conditions conditions = assertion.getConditions();
            conditions.setNotBefore(null);
            conditions.setNotOnOrAfter(null);
            conditions.getConditions().clear();
        };
    }

    static SamlResponseCustomizer conditionsValidity(Instant notBefore, Instant notOnOrAfter)
    {
        return response -> {
            Assertion assertion = response.getAssertions().get(0);

            Conditions conditions = assertion.getConditions();
            conditions.setNotBefore(notBefore);
            conditions.setNotOnOrAfter(notOnOrAfter);
        };
    }

    static SamlResponseCustomizer wrongAudienceRestriction()
    {
        return response -> {
            Assertion assertion = response.getAssertions().get(0);

            assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).setURI("wrong");
        };
    }

    static SamlResponseCustomizer missingDestination()
    {
        return response -> response.setDestination(null);
    }

    static SamlResponseCustomizer wrongDestination()
    {
        return response -> response.setDestination("https://service.com/wrong/path");
    }

    static SamlResponseCustomizer oldAuthnInstant()
    {
        return response -> response
            .getAssertions()
            .get(0)
            .getAuthnStatements()
            .get(0)
            .setAuthnInstant(Instant.now().minus(10, MINUTES));
    }

    static SamlResponseCustomizer noAttributes()
    {
        return response -> response.getAssertions().get(0).getAttributeStatements().get(0).getAttributes().clear();
    }

    static SamlResponseCustomizer singleStringAttribute(String name, String value)
    {
        return attribute(name, () -> asList(XmlUtils.xmlString(value)));
    }

    static SamlResponseCustomizer singleIntegerAttribute(String name, Integer value)
    {
        return attribute(name, () -> asList(XmlUtils.xmlInt(value)));
    }

    static SamlResponseCustomizer singleBooleanAttribute(String name, Boolean value)
    {
        return attribute(name, () -> asList(XmlUtils.xmlBoolean(value)));
    }

    static SamlResponseCustomizer multiStringAttribute(String name, String... values)
    {
        List<XSString> xmlValues = new ArrayList<>();

        for (String value : values)
        {
            xmlValues.add(XmlUtils.xmlString(value));
        }

        return attribute(name, () -> xmlValues);
    }

    static SamlResponseCustomizer attribute(String name, Supplier<List<? extends XMLObject>> valueSupplier)
    {
        return response -> {
            AttributeStatement attributeStatement = response.getAssertions().get(0).getAttributeStatements().get(0);

            Attribute attribute = Saml2ObjectUtils.attribute(name, Attribute.URI_REFERENCE);
            attribute.getAttributeValues().addAll(valueSupplier.get());

            attributeStatement.getAttributes().add(attribute);
        };
    }

    void doWithResponse(Response response);
}
