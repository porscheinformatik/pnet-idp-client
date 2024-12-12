/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import at.porscheinformatik.idp.Gender;
import at.porscheinformatik.idp.PartnerNetCompanyAddressDTO;
import at.porscheinformatik.idp.PartnerNetCompanyDTO;
import at.porscheinformatik.idp.PartnerNetCompanyTypeDTO;
import at.porscheinformatik.idp.PartnerNetContractDTO;
import at.porscheinformatik.idp.PartnerNetFunctionalNumberDTO;
import at.porscheinformatik.idp.PartnerNetRoleDTO;
import at.porscheinformatik.idp.PartnerNetUserType;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetOpenIdConnectUser extends DefaultOidcUser {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ID_TOKEN_TRANSIENT_SESSION = "transient_session_id";
    public static final String ID_TOKEN_RESPONSIBLE_USER_AVAILABLE = "pnet_responsible_user_available";
    public static final String ID_TOKEN_SUPPORT_AVAILABLE = "pnet_support_available";

    private static final String USER_INFO_INTERNAL_ID = "pnet_internal_id";
    public static final String USER_INFO_USER_TYPE = "pnet_person_type";
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
    public static final String USER_INFO_FAVORITE_COMPANY_ID = "pnet_favorite_company";
    public static final String USER_INFO_FAVORITE_BRAND = "pnet_favorite_brand";
    public static final String USER_INFO_CONTACT_COMPANIES = "pnet_contact_companies";
    public static final String USER_INFO_COMPANY_TYPES = "pnet_company_types";

    public static final String USER_INFO_RESPONSIBLE_USER_EXTERNAL_ID = "pnet_responsible_user_external_id";
    public static final String USER_INFO_RESPONSIBLE_USER_NAME = "pnet_responsible_user_name";
    public static final String USER_INFO_RESPONSIBLE_USER_EMAIL = "pnet_responsible_user_email";
    public static final String USER_INFO_RESPONSIBLE_USER_GUID = "pnet_responsible_user_guid";

    public static final String USER_INFO_SUPPORT_COMPANIES = "pnet_support_companies";
    public static final String USER_INFO_SUPPORT_COMPANIES_ADDRESS = "pnet_support_companies_address";
    public static final String USER_INFO_SUPPORT_ROLES = "pnet_support_roles";
    public static final String USER_INFO_SUPPORT_CONTRACTS = "pnet_support_contracts";
    public static final String USER_INFO_SUPPORT_CONTACT_COMPANIES = "pnet_support_contact_companies";
    public static final String USER_INFO_SUPPORT_COMPANY_TYPES = "pnet_support_company_types";

    public PartnerNetOpenIdConnectUser(
        Collection<? extends GrantedAuthority> authorities,
        OidcIdToken idToken,
        OidcUserInfo userInfo
    ) {
        super(authorities, idToken, userInfo);
    }

    /**
     * alias for {@link #getSubject()}
     *
     * @return the external id
     */
    public String getExternalId() {
        return getSubject();
    }

    public String getTransientSessionId() {
        return idTokenClaim(ID_TOKEN_TRANSIENT_SESSION);
    }

    public boolean isResponsibleUserAvailable() {
        Boolean value = idTokenClaim(ID_TOKEN_RESPONSIBLE_USER_AVAILABLE);

        return value != null && value;
    }

    public boolean isSupportDataAvailable() {
        Boolean value = idTokenClaim(ID_TOKEN_SUPPORT_AVAILABLE);

        return value != null && value;
    }

    public int getNistAuthenticationLevel() {
        return Integer.parseInt(getAuthenticationContextClass());
    }

    public PartnerNetUserType getUserType() {
        return PartnerNetUserType.valueOfOrUnknown(userInfoClaims(USER_INFO_USER_TYPE));
    }

    @Override
    public String getName() {
        String givenName = getGivenName();
        String familyName = getFamilyName();

        if (givenName == null || familyName == null) {
            return getExternalId();
        }

        return givenName + " " + familyName;
    }

    public String getAcademicTitle() {
        return userInfoClaims(USER_INFO_ACADEMIC_TITLE);
    }

    public String getAcademicTitlePostNominal() {
        return userInfoClaims(USER_INFO_ACADEMIC_TITLE_POST_NOMINAL);
    }

    public String getGuid() {
        return userInfoClaims(USER_INFO_GUID);
    }

    public String getCostcentre() {
        return userInfoClaims(USER_INFO_COSTCENTRE);
    }

    public String getCountry() {
        return userInfoClaims(USER_INFO_COUNTRY);
    }

    public List<Locale> getAdditionalLocales() {
        return userInfoClaims(USER_INFO_ADDITIONAL_LOCALES);
    }

    public Collection<PartnerNetFunctionalNumberDTO> getFunctionalNumbers() {
        return userInfoClaims(USER_INFO_FUNCTIONAL_NUMBERS);
    }

    public Collection<PartnerNetCompanyDTO> getCompanies() {
        return userInfoClaims(USER_INFO_COMPANIES);
    }

    public Collection<PartnerNetCompanyAddressDTO> getCompaniesAddress() {
        return userInfoClaims(USER_INFO_COMPANIES_ADDRESS);
    }

    public Collection<PartnerNetRoleDTO> getRoles() {
        return userInfoClaims(USER_INFO_ROLES);
    }

    public Collection<PartnerNetContractDTO> getContracts() {
        return userInfoClaims(USER_INFO_CONTRACTS);
    }

    public Collection<PartnerNetCompanyDTO> getSupportCompanies() {
        return userInfoClaims(USER_INFO_SUPPORT_COMPANIES);
    }

    public Collection<PartnerNetCompanyAddressDTO> getSupportCompaniesAddress() {
        return userInfoClaims(USER_INFO_SUPPORT_COMPANIES_ADDRESS);
    }

    public Collection<PartnerNetRoleDTO> getSupportRoles() {
        return userInfoClaims(USER_INFO_SUPPORT_ROLES);
    }

    public Collection<PartnerNetContractDTO> getSupportContract() {
        return userInfoClaims(USER_INFO_SUPPORT_CONTRACTS);
    }

    /**
     * @return the internal Partner.Net Id of the user
     * @deprecated will be removed in a future release. Migrate to {@link #getExternalId()}
     */
    @Deprecated(since = "1.0.0")
    public Integer getLegacyId() {
        return userInfoClaims(USER_INFO_INTERNAL_ID);
    }

    public Gender getPnetGender() {
        return Gender.fromOidcValue(getGender());
    }

    public Locale getPnetLocale() {
        String localeAsString = getLocale();

        if (localeAsString == null) {
            return null;
        }

        return Locale.forLanguageTag(localeAsString);
    }

    public Integer getFavoriteCompanyId() {
        return userInfoClaims(USER_INFO_FAVORITE_COMPANY_ID);
    }

    public String getFavoriteBrand() {
        return userInfoClaims(USER_INFO_FAVORITE_BRAND);
    }

    public Collection<Integer> getContactCompanyIds() {
        return userInfoClaims(USER_INFO_CONTACT_COMPANIES);
    }

    public String getResponsibleUserExternalId() {
        return userInfoClaims(USER_INFO_RESPONSIBLE_USER_EXTERNAL_ID);
    }

    public String getResponsibleUserName() {
        return userInfoClaims(USER_INFO_RESPONSIBLE_USER_NAME);
    }

    public String getResponsibleUserEmail() {
        return userInfoClaims(USER_INFO_RESPONSIBLE_USER_EMAIL);
    }

    public String getResponsibleUserGuid() {
        return userInfoClaims(USER_INFO_RESPONSIBLE_USER_GUID);
    }

    public Collection<Integer> getSupportContactCompanyIds() {
        return userInfoClaims(USER_INFO_SUPPORT_CONTACT_COMPANIES);
    }

    public Collection<PartnerNetCompanyTypeDTO> getCompanyTypes() {
        return userInfoClaims(USER_INFO_COMPANY_TYPES);
    }

    public Collection<PartnerNetCompanyTypeDTO> getSupportCompanyTypes() {
        return userInfoClaims(USER_INFO_SUPPORT_COMPANY_TYPES);
    }

    private <T> T idTokenClaim(String claimName) {
        return getIdToken().getClaim(claimName);
    }

    private <T> T userInfoClaims(String claimName) {
        return getUserInfo().getClaim(claimName);
    }
}
