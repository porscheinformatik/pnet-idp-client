package at.porscheinformatik.idp.saml2;

import org.springframework.security.web.DefaultRedirectStrategy;

public class Saml2UrlSanitizingRedirectStrategy extends DefaultRedirectStrategy
{

    @Override
    protected String calculateRedirectUrl(String contextPath, String url)
    {
        String redirectUrl = super.calculateRedirectUrl(contextPath, url);

        return Saml2Utils.sanitizeUrl(redirectUrl);
    }

}
