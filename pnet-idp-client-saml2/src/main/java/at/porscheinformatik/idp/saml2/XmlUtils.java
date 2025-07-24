/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static java.util.Objects.*;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.w3c.dom.Element;

/**
 * @author Daniel Furtlehner
 */
public final class XmlUtils {

    public static final String PNET_NAMESPACE = "https://identity.auto-partner.net/identity/saml2";
    public static final String DEFAULT_PREFIX = "pnet";
    public static final QName MAX_SESSION_AGE_ELEMENT_NAME = new QName(PNET_NAMESPACE, "MaxSessionAge", DEFAULT_PREFIX);
    public static final QName MAX_AGE_MFA_ELEMENT_NAME = new QName(PNET_NAMESPACE, "MaxAgeMfa", DEFAULT_PREFIX);
    public static final QName TENANT_ELEMENT_NAME = new QName(PNET_NAMESPACE, "Tenant", DEFAULT_PREFIX);
    public static final QName PROMPT_ELEMENT_NAME = new QName(PNET_NAMESPACE, "Prompt", DEFAULT_PREFIX);
    public static final QName LOGIN_HINT_ELEMENT_NAME = new QName(PNET_NAMESPACE, "LoginHint", DEFAULT_PREFIX);

    private XmlUtils() {
        super();
    }

    /**
     * Will try to extract the value of the given xml object. Return types are as follows:
     * <table>
     * <thead>
     * <tr>
     * <th>Argument Type</th>
     * <th>Return Type</th>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td>xmlObject.isNil()</td>
     * <td>null</td>
     * </tr>
     * <tr>
     * <td>XSAny</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>XSBase64Binary</td>
     * <td>byte[]</td>
     * </tr>
     * <tr>
     * <td>XSBoolean</td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td>XSDateTime</td>
     * <td>Instant</td>
     * </tr>
     * <tr>
     * <td>XSInteger</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>XSString</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>XSURI</td>
     * <td>Uri</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param xmlObject the xml object to extract the value from
     * @return the objects value
     */
    @Nullable
    public static Serializable getXmlValue(XMLObject xmlObject) {
        if (Boolean.TRUE.equals(xmlObject.isNil())) {
            return null;
        }

        if (xmlObject instanceof XSAny xsAny) {
            return xsAny.getTextContent();
        }

        if (xmlObject instanceof XSBase64Binary xsBase64Binary) {
            String base64String = xsBase64Binary.getValue();

            return Base64.decodeBase64(base64String);
        }

        if (xmlObject instanceof XSBoolean xsBoolean) {
            return xsBoolean.getValue().getValue();
        }

        if (xmlObject instanceof XSDateTime xsDateTime) {
            return xsDateTime.getValue();
        }

        if (xmlObject instanceof XSInteger xsInteger) {
            return xsInteger.getValue();
        }

        if (xmlObject instanceof XSString xsString) {
            return xsString.getValue();
        }

        if (xmlObject instanceof XSURI xsUri) {
            String uriString = xsUri.getURI();

            return URI.create(uriString);
        }

        throw new IllegalArgumentException("Unsupported xml object type " + xmlObject.getClass());
    }

    public static MaxAge maxSessionAgeRequest(Integer sessionAgeInSeconds) {
        MaxAge sessionAgeRequest = createXmlObject(MAX_SESSION_AGE_ELEMENT_NAME);

        sessionAgeRequest.setMaxAgeInSeconds(sessionAgeInSeconds);

        return sessionAgeRequest;
    }

    public static MaxAge maxAgeMfaRequest(Integer maxAgeInSeconds) {
        MaxAge request = createXmlObject(MAX_AGE_MFA_ELEMENT_NAME);

        request.setMaxAgeInSeconds(maxAgeInSeconds);

        return request;
    }

    public static Tenant tenantRequest(String tenant) {
        Tenant tenantRequest = createXmlObject(TENANT_ELEMENT_NAME);

        tenantRequest.setTenant(tenant);

        return tenantRequest;
    }

    public static Prompt promptRequest(String prompt) {
        Prompt promptRequest = createXmlObject(PROMPT_ELEMENT_NAME);

        promptRequest.setPrompt(prompt);

        return promptRequest;
    }

    public static LoginHint loginHintRequest(String loginHint) {
        LoginHint loginHintRequest = createXmlObject(LOGIN_HINT_ELEMENT_NAME);

        loginHintRequest.setLoginHint(loginHint);

        return loginHintRequest;
    }

    public static RequestedAuthnContext requestedAuthnContext(List<AuthnContextClass> authnContextClasses) {
        RequestedAuthnContext authnContext = createSamlObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        authnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

        authnContextClasses.forEach(entry -> {
            AuthnContextClassRef reference = authnContextClassRef(entry.getSamlReference());

            authnContext.getAuthnContextClassRefs().add(reference);
        });

        return authnContext;
    }

    private static AuthnContextClassRef authnContextClassRef(String reference) {
        AuthnContextClassRef authnContext = createSamlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContext.setURI(reference);

        return authnContext;
    }

    /**
     * Creates a saml2 isser element
     *
     * @param issuer the issuer value
     * @return the saml issuer
     */
    public static Issuer issuer(String issuer) {
        Issuer samlIssuer = createSamlObject(Issuer.DEFAULT_ELEMENT_NAME);
        samlIssuer.setValue(requireNonNull(issuer, "Issuer value must not be null"));

        return samlIssuer;
    }

    /**
     * @param object to marshall
     * @return marshalled object.
     * @throws MarshallingException if an exception occurs while marshalling
     */
    public static String marshall(SAMLObject object) throws MarshallingException {
        return serialize(XMLObjectSupport.marshall(object), false);
    }

    /**
     * @param element the element to transform to its string representation
     * @param prettyprint when true a formatted xml will be produced. Should only be used for testing because the
     *            Signature is not valid on pretty printed xmls.
     * @return the string representation of the given element
     */
    private static String serialize(Element element, boolean prettyprint) {
        if (prettyprint) {
            return SerializeSupport.prettyPrintXML(element);
        }

        return SerializeSupport.nodeToString(element);
    }

    public static <T extends SAMLObject> T createSamlObject(QName defaultName) {
        XMLObjectBuilderFactory factory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        @SuppressWarnings("unchecked")
        SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>) factory.getBuilder(defaultName);

        return builder.buildObject();
    }

    public static <T extends XMLObject> T createXmlObject(QName defaultName) {
        XMLObjectBuilderFactory factory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        @SuppressWarnings("unchecked")
        XMLObjectBuilder<T> builder = (XMLObjectBuilder<T>) factory.getBuilder(defaultName);

        return builder.buildObject(defaultName);
    }

    @SuppressWarnings("unchecked")
    public static <T extends XMLObject> T createXMLObject(QName typeName, QName defaultName) {
        XMLObjectBuilder<?> builder = XMLObjectSupport.getBuilder(typeName);

        return (T) builder.buildObject(defaultName, typeName);
    }

    public static XSInteger xmlInt(Integer value) {
        XSInteger xsInteger = createXMLObject(XSInteger.TYPE_NAME, AttributeValue.DEFAULT_ELEMENT_NAME);

        if (value == null) {
            xsInteger.setNil(Boolean.TRUE);
        } else {
            xsInteger.setValue(value);
        }

        return xsInteger;
    }

    public static XSBoolean xmlBoolean(Boolean value) {
        XSBoolean xsBoolean = createXMLObject(XSBoolean.TYPE_NAME, AttributeValue.DEFAULT_ELEMENT_NAME);

        if (value == null) {
            xsBoolean.setNil(Boolean.TRUE);
        } else {
            xsBoolean.setValue(new XSBooleanValue(value, false));
        }

        return xsBoolean;
    }

    /**
     * @param value - String
     * @return XML String
     */
    public static XSString xmlString(String value) {
        XSString xsstring = createXMLObject(XSString.TYPE_NAME, AttributeValue.DEFAULT_ELEMENT_NAME);

        if (value == null) {
            xsstring.setNil(Boolean.TRUE);
        } else {
            xsstring.setValue(value);
        }

        return xsstring;
    }
}
