/**
 *
 */
package at.porscheinformatik.pnet.idp.clientshowcase;

import at.porscheinformatik.idp.Gender;
import at.porscheinformatik.idp.PartnerNetCompanyAddressDTO;
import at.porscheinformatik.idp.PartnerNetCompanyDTO;
import at.porscheinformatik.idp.PartnerNetCompanyTypeDTO;
import at.porscheinformatik.idp.PartnerNetContractDTO;
import at.porscheinformatik.idp.PartnerNetFunctionalNumberDTO;
import at.porscheinformatik.idp.PartnerNetRoleDTO;
import at.porscheinformatik.idp.PartnerNetUserType;
import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser;
import at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationPrincipal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author Daniel Furtlehner
 */
public class AuthenticationDTO {

    public static AuthenticationDTO of(PartnerNetOpenIdConnectUser principal) {
        return new AuthenticationDTO(
            "OpenId Connect",
            principal.getExternalId(),
            principal.getNistAuthenticationLevel() > 2,
            principal.getTransientSessionId(),
            principal.getLoginHint(),
            principal.isSupportDataAvailable(),
            principal.getNistAuthenticationLevel(),
            principal.getAuthenticatedAt(),
            principal.getUserType(),
            principal.getName(),
            principal.getEmail(),
            principal.getPhoneNumber(),
            principal.getPnetGender(),
            principal.getPnetLocale(),
            principal.getUpdatedAt(),
            principal.getAcademicTitle(),
            principal.getAcademicTitlePostNominal(),
            principal.getGuid(),
            principal.getCostcentre(),
            principal.getCountry(),
            principal.getLegacyId(),
            principal.getFavoriteCompanyId(),
            principal.getFavoriteBrand(),
            principal.getAdditionalLocales(),
            principal.getFunctionalNumbers(),
            principal.getCompanies(),
            principal.getContactCompanyIds(),
            principal.getCompaniesAddress(),
            principal.getRoles(),
            principal.getContracts(),
            principal.getCompanyTypes(),
            principal.getResponsibleUserExternalId(),
            principal.getResponsibleUserName(),
            principal.getResponsibleUserEmail(),
            principal.getResponsibleUserGuid(),
            principal.getSupportCompanies(),
            principal.getSupportContactCompanyIds(),
            principal.getSupportCompaniesAddress(),
            principal.getSupportRoles(),
            principal.getSupportContract(),
            principal.getSupportCompanyTypes()
        );
    }

    public static AuthenticationDTO of(PartnerNetSaml2AuthenticationPrincipal principal) {
        return new AuthenticationDTO(
            "Saml2.0",
            principal.getSubjectIdentifier(),
            principal.isStronglyAuthenticated(),
            principal.getTransientSessionId(),
            principal.getLoginHint(),
            principal.isSupportDataAvailable(),
            principal.getAuthnContextClass().getNistLevel(),
            null,
            principal.getUserType(),
            principal.getName(),
            principal.getMailAddress(),
            principal.getPhoneNumber(),
            principal.getGender(),
            principal.getLanguage(),
            principal.getLastUpdate(),
            principal.getAcademicTitle(),
            principal.getAcademicTitlePostNominal(),
            principal.getGuid(),
            principal.getCostCenter(),
            principal.getTenant(),
            principal.getLegacyId(),
            principal.getFavoriteCompanyId(),
            principal.getFavoriteBrand(),
            principal.getAdditionalLanguages(),
            principal.getFunctionalNumbers(),
            principal.getEmployments(),
            principal.getContactCompanyIds(),
            principal.getEmploymentsAddress(),
            principal.getRoles(),
            principal.getContracts(),
            principal.getCompanyTypes(),
            principal.getResponsibleUserExternalId(),
            principal.getResponsibleUserName(),
            principal.getResponsibleUserEmail(),
            principal.getResponsibleUserGuid(),
            principal.getSupportEmployments(),
            principal.getSupportContactCompanyIds(),
            principal.getSupportEmploymentsAddress(),
            principal.getSupportRoles(),
            principal.getSupportContracts(),
            principal.getSupportCompanyTypes()
        );
    }

    private final String info;
    private final String externalId;
    private final boolean secondFactorUsed;

    private final String transientSessionId;
    private final String loginHint;
    private final boolean supportDataAvailable;
    private final int nistAuthenticationLevel;
    private final Instant authenticatedAt;
    private final PartnerNetUserType userType;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final Gender gender;
    private final Locale locale;
    private final Instant updatedAt;
    private final String academicTitle;
    private final String academicTitlePostNominal;
    private final String guid;
    private final String costcentre;
    private final String country;
    private final Integer internalId;
    private final Integer favoriteCompanyId;
    private final String favoriteBrand;
    private final List<Locale> additionalLocales;
    private final Collection<PartnerNetFunctionalNumberDTO> functionalNumbers;
    private final Collection<PartnerNetCompanyDTO> companies;
    private final Collection<Integer> contactCompanyIds;
    private final Collection<PartnerNetCompanyAddressDTO> companiesAddress;
    private final Collection<PartnerNetRoleDTO> roles;
    private final Collection<PartnerNetContractDTO> contracts;
    private final Collection<PartnerNetCompanyTypeDTO> companyTypes;
    private final String responsibleUserExternalId;
    private final String responsibleUserName;
    private final String responsibleUserEmail;
    private final String responsibleUserGuid;
    private final Collection<PartnerNetCompanyDTO> supportCompanies;
    private final Collection<Integer> supportContactCompanyIds;
    private final Collection<PartnerNetCompanyAddressDTO> supportCompaniesAddress;
    private final Collection<PartnerNetRoleDTO> supportRoles;
    private final Collection<PartnerNetContractDTO> supportContract;
    private final Collection<PartnerNetCompanyTypeDTO> supportCompanyTypes;

    public AuthenticationDTO(
        String info,
        String externalId,
        boolean secondFactorUsed,
        String transientSessionId,
        String loginHint,
        boolean supportDataAvailable,
        int nistAuthenticationLevel,
        Instant authenticatedAt,
        PartnerNetUserType userType,
        String name,
        String email,
        String phoneNumber,
        Gender gender,
        Locale locale,
        Instant updatedAt,
        String academicTitle,
        String academicTitlePostNominal,
        String guid,
        String costcentre,
        String country,
        Integer internalId,
        Integer favoriteCompanyId,
        String favoriteBrand,
        List<Locale> additionalLocales,
        Collection<PartnerNetFunctionalNumberDTO> functionalNumbers,
        Collection<PartnerNetCompanyDTO> companies,
        Collection<Integer> contactCompanyIds,
        Collection<PartnerNetCompanyAddressDTO> companiesAddress,
        Collection<PartnerNetRoleDTO> roles,
        Collection<PartnerNetContractDTO> contracts,
        Collection<PartnerNetCompanyTypeDTO> companyTypes,
        String responsibleUserExternalId,
        String responsibleUserName,
        String responsibleUserEmail,
        String responsibleUserGuid,
        Collection<PartnerNetCompanyDTO> supportCompanies,
        Collection<Integer> supportContactCompanyIds,
        Collection<PartnerNetCompanyAddressDTO> supportCompaniesAddress,
        Collection<PartnerNetRoleDTO> supportRoles,
        Collection<PartnerNetContractDTO> supportContract,
        Collection<PartnerNetCompanyTypeDTO> supportCompanyTypes
    ) {
        super();
        this.info = info;
        this.externalId = externalId;
        this.secondFactorUsed = secondFactorUsed;
        this.transientSessionId = transientSessionId;
        this.loginHint = loginHint;
        this.supportDataAvailable = supportDataAvailable;
        this.nistAuthenticationLevel = nistAuthenticationLevel;
        this.authenticatedAt = authenticatedAt;
        this.userType = userType;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.locale = locale;
        this.updatedAt = updatedAt;
        this.academicTitle = academicTitle;
        this.academicTitlePostNominal = academicTitlePostNominal;
        this.guid = guid;
        this.costcentre = costcentre;
        this.country = country;
        this.internalId = internalId;
        this.favoriteCompanyId = favoriteCompanyId;
        this.favoriteBrand = favoriteBrand;
        this.additionalLocales = additionalLocales;
        this.functionalNumbers = functionalNumbers;
        this.companies = companies;
        this.contactCompanyIds = contactCompanyIds;
        this.companiesAddress = companiesAddress;
        this.roles = roles;
        this.contracts = contracts;
        this.companyTypes = companyTypes;
        this.responsibleUserExternalId = responsibleUserExternalId;
        this.responsibleUserName = responsibleUserName;
        this.responsibleUserEmail = responsibleUserEmail;
        this.responsibleUserGuid = responsibleUserGuid;
        this.supportCompanies = supportCompanies;
        this.supportContactCompanyIds = supportContactCompanyIds;
        this.supportCompaniesAddress = supportCompaniesAddress;
        this.supportRoles = supportRoles;
        this.supportContract = supportContract;
        this.supportCompanyTypes = supportCompanyTypes;
    }

    public String getInfo() {
        return info;
    }

    public String getExternalId() {
        return externalId;
    }

    public boolean isSecondFactorUsed() {
        return secondFactorUsed;
    }

    public String getTransientSessionId() {
        return transientSessionId;
    }

    public String getLoginHint() {
        return loginHint;
    }

    public boolean isSupportDataAvailable() {
        return supportDataAvailable;
    }

    public int getNistAuthenticationLevel() {
        return nistAuthenticationLevel;
    }

    public Instant getAuthenticatedAt() {
        return authenticatedAt;
    }

    public PartnerNetUserType getUserType() {
        return userType;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Gender getGender() {
        return gender;
    }

    public Locale getLocale() {
        return locale;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getAcademicTitle() {
        return academicTitle;
    }

    public String getAcademicTitlePostNominal() {
        return academicTitlePostNominal;
    }

    public String getGuid() {
        return guid;
    }

    public String getCostcentre() {
        return costcentre;
    }

    public String getCountry() {
        return country;
    }

    public Integer getInternalId() {
        return internalId;
    }

    public Integer getFavoriteCompanyId() {
        return favoriteCompanyId;
    }

    public String getFavoriteBrand() {
        return favoriteBrand;
    }

    public List<Locale> getAdditionalLocales() {
        return additionalLocales;
    }

    public Collection<PartnerNetFunctionalNumberDTO> getFunctionalNumbers() {
        return functionalNumbers;
    }

    public Collection<PartnerNetCompanyDTO> getCompanies() {
        return companies;
    }

    public Collection<Integer> getContactCompanyIds() {
        return contactCompanyIds;
    }

    public Collection<PartnerNetCompanyAddressDTO> getCompaniesAddress() {
        return companiesAddress;
    }

    public Collection<PartnerNetRoleDTO> getRoles() {
        return roles;
    }

    public Collection<PartnerNetContractDTO> getContracts() {
        return contracts;
    }

    public Collection<PartnerNetCompanyTypeDTO> getCompanyTypes() {
        return companyTypes;
    }

    public String getResponsibleUserExternalId() {
        return responsibleUserExternalId;
    }

    public String getResponsibleUserName() {
        return responsibleUserName;
    }

    public String getResponsibleUserEmail() {
        return responsibleUserEmail;
    }

    public String getResponsibleUserGuid() {
        return responsibleUserGuid;
    }

    public Collection<PartnerNetCompanyDTO> getSupportCompanies() {
        return supportCompanies;
    }

    public Collection<Integer> getSupportContactCompanyIds() {
        return supportContactCompanyIds;
    }

    public Collection<PartnerNetCompanyAddressDTO> getSupportCompaniesAddress() {
        return supportCompaniesAddress;
    }

    public Collection<PartnerNetRoleDTO> getSupportRoles() {
        return supportRoles;
    }

    public Collection<PartnerNetContractDTO> getSupportContract() {
        return supportContract;
    }

    public Collection<PartnerNetCompanyTypeDTO> getSupportCompanyTypes() {
        return supportCompanyTypes;
    }
}
