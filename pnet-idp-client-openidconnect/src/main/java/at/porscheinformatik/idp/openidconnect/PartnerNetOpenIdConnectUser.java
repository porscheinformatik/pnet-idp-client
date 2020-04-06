/**
 * 
 */
package at.porscheinformatik.idp.openidconnect;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetOpenIdConnectUser extends DefaultOidcUser
{
    private static final long serialVersionUID = 1L;

    public static final String ID_TOKEN_TRANSIENT_SESSION = "transient_session_id";
    public static final String ID_TOKEN_SUPPORT_AVAILABLE = "pnet_support_available";

    public static final String USER_INFO_ACADEMIC_TITLE = "pnet_academic_title";
    public static final String USER_INFO_ACADEMIC_TITLE_POST_NOMINAL = "pnet_academic_title_post_nominal";
    public static final String USER_INFO_GUID = "pnet_guid";
    public static final String USER_INFO_COSTCENTRE = "pnet_costcentre";
    public static final String USER_INFO_COUNTRY = "pnet_country";
    public static final String USER_INFO_ADDITIONAL_LOCALES = "pnet_additional_locales";
    public static final String USER_INFO_FUNCTIONAL_NUMBERS = "pnet_functional_numbers";
    public static final String USER_INFO_COMPANIES = "pnet_companies";
    public static final String USER_INFO_COMPANIES_ADDRESS = "pnet_companies_address";
    public static final String USER_INFO_ROLES = "pnet_roles";
    public static final String USER_INFO_CONTRACTS = "pnet_contracts";

    public static final String USER_INFO_SUPPORT_COMPANIES = "pnet_support_companies";
    public static final String USER_INFO_SUPPORT_COMPANIES_ADDRESS = "pnet_support_companies_address";
    public static final String USER_INFO_SUPPORT_ROLES = "pnet_support_roles";
    public static final String USER_INFO_SUPPORT_CONTRACTS = "pnet_support_contracts";

    public PartnerNetOpenIdConnectUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken,
        OidcUserInfo userInfo)
    {
        super(authorities, idToken, userInfo);
    }

    /**
     * alias for {@link #getSubject()}
     * 
     * @return the external id
     */
    public String getExternalId()
    {
        return getSubject();
    }

    public String getTransientSessionId()
    {
        return idTokenClaim(ID_TOKEN_TRANSIENT_SESSION);
    }

    public boolean isSupportDataAvailable()
    {
        Boolean value = idTokenClaim(ID_TOKEN_SUPPORT_AVAILABLE);

        return value == null ? false : value.booleanValue();
    }

    public int getNistAuthenticationLevel()
    {
        return Integer.parseInt(getAuthenticationContextClass());
    }

    @Override
    public String getName()
    {
        String givenName = getGivenName();
        String familyName = getFamilyName();

        if (givenName == null || familyName == null)
        {
            return getExternalId();
        }

        return givenName + " " + familyName;
    }

    public String getAcademicTitle()
    {
        return userInfoClaims(USER_INFO_ACADEMIC_TITLE);
    }

    public String getAcademicTitlePostNominal()
    {
        return userInfoClaims(USER_INFO_ACADEMIC_TITLE_POST_NOMINAL);
    }

    public String getGuid()
    {
        return userInfoClaims(USER_INFO_GUID);
    }

    public String getCostcentre()
    {
        return userInfoClaims(USER_INFO_COSTCENTRE);
    }

    public String getCountry()
    {
        return userInfoClaims(USER_INFO_COUNTRY);
    }

    public List<String> getAdditionalLocales()
    {
        return userInfoClaims(USER_INFO_ADDITIONAL_LOCALES);
    }

    public Collection<PartnerNetFunctionalNumberDTO> getFunctionalNumbers()
    {
        return userInfoClaims(USER_INFO_FUNCTIONAL_NUMBERS);
    }

    public Collection<PartnerNetCompanyDTO> getCompanies()
    {
        return userInfoClaims(USER_INFO_COMPANIES);
    }

    public Collection<PartnerNetCompanyAddressDTO> getCompaniesAddress()
    {
        return userInfoClaims(USER_INFO_COMPANIES_ADDRESS);
    }

    public Collection<PartnerNetRoleDTO> getRoles()
    {
        return userInfoClaims(USER_INFO_ROLES);
    }

    public Collection<PartnerNetContractDTO> getContracts()
    {
        return userInfoClaims(USER_INFO_CONTRACTS);
    }

    public Collection<PartnerNetCompanyDTO> getSupportCompanies()
    {
        return userInfoClaims(USER_INFO_SUPPORT_COMPANIES);
    }

    public Collection<PartnerNetCompanyAddressDTO> getSupportCompaniesAddress()
    {
        return userInfoClaims(USER_INFO_SUPPORT_COMPANIES_ADDRESS);
    }

    public Collection<PartnerNetRoleDTO> getSupportRoles()
    {
        return userInfoClaims(USER_INFO_SUPPORT_ROLES);
    }

    public Collection<PartnerNetContractDTO> getSupportContract()
    {
        return userInfoClaims(USER_INFO_SUPPORT_CONTRACTS);
    }

    private <T> T idTokenClaim(String claimName)
    {
        return getIdToken().getClaim(claimName);
    }

    private <T> T userInfoClaims(String claimName)
    {
        return getUserInfo().getClaim(claimName);
    }
}
