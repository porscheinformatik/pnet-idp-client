package at.porscheinformatik.idp.saml2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public enum AuthnContextClass
{
    NONE(-1, "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified"),
    IP(-1, "urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocol"),
    PASSWORD(1, "urn:oasis:names:tc:SAML:2.0:ac:classes:Password"),
    PREVIOUS_SESSION(1, "urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession"),
    IP_PASSWORD(2, "urn:oasis:names:tc:SAML:2.0:ac:classes:InternetProtocolPassword"),
    USERPASS(2, "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport"),
    SMARTCARD(2, "urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard"),
    X509(2, "urn:oasis:names:tc:SAML:2.0:ac:classes:X509"),
    PGP(2, "urn:oasis:names:tc:SAML:2.0:ac:classes:PGP"),
    SPKI(2, "urn:oasis:names:tc:SAML:2.0:ac:classes:SPKI"),
    TOTP(3, "urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken"),
    SMARTCARDPKI(3, "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI"),
    SOFTWAREPKI(3, "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI");

    private int nistLevel;
    private String samlReference;

    AuthnContextClass(int nistLevel, String samlReference)
    {
        this.nistLevel = nistLevel;
        this.samlReference = samlReference;
    }

    public int getNistLevel()
    {
        return nistLevel;
    }

    public void setNistLevel(int nistLevel)
    {
        this.nistLevel = nistLevel;
    }

    public String getSamlReference()
    {
        return samlReference;
    }

    public void setSamlReference(String samlReference)
    {
        this.samlReference = samlReference;
    }

    boolean isStrongerThan(AuthnContextClass other)
    {
        return nistLevel > other.nistLevel;
    }

    public static List<AuthnContextClass> getAsLeastAsStrongAs(int nistAuthenticationLevel)
    {
        return Arrays
            .stream(values())
            .filter(entry -> entry.getNistLevel() >= nistAuthenticationLevel)
            .collect(Collectors.toList());
    }

    public static Optional<AuthnContextClass> fromReference(String authnContextClassRef)
    {
        return Arrays
            .stream(values())
            .filter(entry -> Objects.equals(entry.getSamlReference(), authnContextClassRef))
            .findAny();
    }

}
