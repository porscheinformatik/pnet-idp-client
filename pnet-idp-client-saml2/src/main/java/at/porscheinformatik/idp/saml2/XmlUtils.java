/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static java.util.Objects.*;

import java.io.Serializable;
import java.net.URI;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;

/**
 * @author Daniel Furtlehner
 */
public final class XmlUtils
{
    private XmlUtils()
    {
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
     * <td>LocalDateTime</td>
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
    public static Serializable getXmlValue(XMLObject xmlObject)
    {
        if (xmlObject.isNil())
        {
            return null;
        }

        if (xmlObject instanceof XSAny)
        {
            return ((XSAny) xmlObject).getTextContent();
        }

        if (xmlObject instanceof XSBase64Binary)
        {
            String base64String = ((XSBase64Binary) xmlObject).getValue();

            return Base64.decodeBase64(base64String);
        }

        if (xmlObject instanceof XSBoolean)
        {
            return ((XSBoolean) xmlObject).getValue().getValue();
        }

        if (xmlObject instanceof XSDateTime)
        {
            DateTime dateTime = ((XSDateTime) xmlObject).getValue();

            return dateTime;
        }

        if (xmlObject instanceof XSInteger)
        {
            return ((XSInteger) xmlObject).getValue();
        }

        if (xmlObject instanceof XSString)
        {
            return ((XSString) xmlObject).getValue();
        }

        if (xmlObject instanceof XSURI)
        {
            String uriString = ((XSURI) xmlObject).getValue();

            return URI.create(uriString);
        }

        throw new IllegalArgumentException("Unsupported xml object type " + xmlObject.getClass());

    }

    public static AuthnRequest authnRequest(String spEntityId, String destination, String assertionConsumerServiceUrl,
        String id)
    {
        AuthnRequest request = createSamlObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        request.setID(id);
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(DateTime.now());

        request.setIssuer(issuer(spEntityId));

        request.setAssertionConsumerServiceURL(assertionConsumerServiceUrl);

        if (destination != null)
        {
            request.setDestination(destination);
        }

        return request;
    }

    /**
     * Creates a saml2 isser element
     *
     * @param issuer the issuer value
     * @return the saml issuer
     */
    public static Issuer issuer(String issuer)
    {
        Issuer samlIssuer = createSamlObject(Issuer.DEFAULT_ELEMENT_NAME);
        samlIssuer.setValue(requireNonNull(issuer, "Issuer value must not be null"));

        return samlIssuer;
    }

    private static <T extends SAMLObject> T createSamlObject(QName defaultName)
    {
        XMLObjectBuilderFactory factory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        @SuppressWarnings("unchecked")
        SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>) factory.getBuilder(defaultName);

        return builder.buildObject();
    }
}
