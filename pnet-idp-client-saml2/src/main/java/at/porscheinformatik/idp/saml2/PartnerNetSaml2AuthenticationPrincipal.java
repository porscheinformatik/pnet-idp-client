package at.porscheinformatik.idp.saml2;

import at.porscheinformatik.idp.Gender;
import at.porscheinformatik.idp.PartnerNetCompanyAddressDTO;
import at.porscheinformatik.idp.PartnerNetCompanyDTO;
import at.porscheinformatik.idp.PartnerNetCompanyTypeDTO;
import at.porscheinformatik.idp.PartnerNetContractDTO;
import at.porscheinformatik.idp.PartnerNetFunctionalNumberDTO;
import at.porscheinformatik.idp.PartnerNetRoleDTO;
import at.porscheinformatik.idp.PartnerNetUserType;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class PartnerNetSaml2AuthenticationPrincipal implements Serializable {

    @Serial
    private static final long serialVersionUID = 8462523068524794768L;

    private final String subjectIdentifier;
    private final String relayState;
    private final String transientSessionId;
    private final AuthnContextClass authnContextClass;
    private final Instant lastUpdate;

    private final String guid;
    private final String personnelNumber;
    private final Integer legacyId;

    private final PartnerNetUserType userType;
    private final String academicTitle;
    private final String academicTitlePostNominal;
    private final String firstname;
    private final String lastname;
    private final Gender gender;
    private final Locale language;
    private final List<Locale> additionalLanguages;

    private final String mailAddress;
    private final String phoneNumber;

    private final String tenant;
    private final String costCenter;
    private final Integer favoriteCompanyId;
    private final String favoriteBrand;

    private final List<PartnerNetFunctionalNumberDTO> functionalNumbers;

    private final List<PartnerNetCompanyDTO> employments;
    private final List<PartnerNetCompanyAddressDTO> employmentsAddress;
    private final List<PartnerNetRoleDTO> roles;
    private final List<PartnerNetContractDTO> contracts;
    private final Collection<Integer> contactCompanyIds;
    private final Collection<PartnerNetCompanyTypeDTO> companyTypes;

    private final boolean responsibleUser;
    private final String responsibleUserExternalId;
    private final String responsibleUserFirstname;
    private final String responsibleUserLastname;
    private final String responsibleUserEmail;
    private final String responsibleUserGuid;

    private final boolean supportData;
    private final List<PartnerNetCompanyDTO> supportEmployments;
    private final List<PartnerNetCompanyAddressDTO> supportEmploymentsAddress;
    private final List<PartnerNetRoleDTO> supportRoles;
    private final List<PartnerNetContractDTO> supportContracts;
    private final Collection<Integer> supportContactCompanyIds;
    private final Collection<PartnerNetCompanyTypeDTO> supportCompanyTypes;

    public PartnerNetSaml2AuthenticationPrincipal(
        String subjectIdentifier,
        String relayState,
        String transientSessionId,
        AuthnContextClass authnContextClass,
        Instant lastUpdate,
        String guid,
        String personnelNumber,
        Integer legacyId,
        PartnerNetUserType userType,
        String academicTitle,
        String academicTitlePostNominal,
        String firstname,
        String lastname,
        Gender gender,
        Locale language,
        List<Locale> additionalLanguages,
        String mailAddress,
        String phoneNumber,
        String tenant,
        String costCenter,
        Integer favoriteCompanyId,
        String favoriteBrand,
        List<PartnerNetFunctionalNumberDTO> functionalNumbers,
        List<PartnerNetCompanyDTO> employments,
        List<PartnerNetCompanyAddressDTO> employmentsAddress,
        List<PartnerNetRoleDTO> roles,
        List<PartnerNetContractDTO> contracts,
        Collection<Integer> contactCompanyIds,
        Collection<PartnerNetCompanyTypeDTO> companyTypes,
        boolean responsibleUser,
        String responsibleUserExternalId,
        String responsibleUserFirstname,
        String responsibleUserLastname,
        String responsibleUserEmail,
        String responsibleUserGuid,
        boolean supportData,
        List<PartnerNetCompanyDTO> supportEmployments,
        List<PartnerNetCompanyAddressDTO> supportEmploymentsAddress,
        List<PartnerNetRoleDTO> supportRoles,
        List<PartnerNetContractDTO> supportContracts,
        Collection<Integer> supportContactCompanyIds,
        Collection<PartnerNetCompanyTypeDTO> supportCompanyTypes
    ) {
        super();
        this.subjectIdentifier = subjectIdentifier;
        this.relayState = relayState;
        this.transientSessionId = transientSessionId;
        this.authnContextClass = authnContextClass;
        this.lastUpdate = lastUpdate;
        this.guid = guid;
        this.personnelNumber = personnelNumber;
        this.legacyId = legacyId;
        this.userType = userType;
        this.academicTitle = academicTitle;
        this.academicTitlePostNominal = academicTitlePostNominal;
        this.firstname = firstname;
        this.lastname = lastname;
        this.gender = gender;
        this.language = language;
        this.additionalLanguages = additionalLanguages;
        this.mailAddress = mailAddress;
        this.phoneNumber = phoneNumber;
        this.tenant = tenant;
        this.costCenter = costCenter;
        this.favoriteCompanyId = favoriteCompanyId;
        this.favoriteBrand = favoriteBrand;
        this.functionalNumbers = functionalNumbers;
        this.employments = employments;
        this.employmentsAddress = employmentsAddress;
        this.roles = roles;
        this.contracts = contracts;
        this.contactCompanyIds = contactCompanyIds;
        this.companyTypes = companyTypes;
        this.responsibleUser = responsibleUser;
        this.responsibleUserExternalId = responsibleUserExternalId;
        this.responsibleUserFirstname = responsibleUserFirstname;
        this.responsibleUserLastname = responsibleUserLastname;
        this.responsibleUserEmail = responsibleUserEmail;
        this.responsibleUserGuid = responsibleUserGuid;
        this.supportData = supportData;
        this.supportEmployments = supportEmployments;
        this.supportEmploymentsAddress = supportEmploymentsAddress;
        this.supportRoles = supportRoles;
        this.supportContracts = supportContracts;
        this.supportContactCompanyIds = supportContactCompanyIds;
        this.supportCompanyTypes = supportCompanyTypes;
    }

    public String getSubjectIdentifier() {
        return subjectIdentifier;
    }

    public String getRelayState() {
        return relayState;
    }

    public String getGuid() {
        return guid;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    /**
     * @return the internal Partner.Net Id of the user
     * @deprecated will be removed in a future release. Migrate to {@link #getSubjectIdentifier()}
     */
    @Deprecated
    public Integer getLegacyId() {
        return legacyId;
    }

    public PartnerNetUserType getUserType() {
        return userType;
    }

    public String getAcademicTitle() {
        return academicTitle;
    }

    public String getAcademicTitlePostNominal() {
        return academicTitlePostNominal;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public Gender getGender() {
        return gender;
    }

    public Locale getLanguage() {
        return language;
    }

    public List<Locale> getAdditionalLanguages() {
        return additionalLanguages;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getTenant() {
        return tenant;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public Integer getFavoriteCompanyId() {
        return favoriteCompanyId;
    }

    public String getFavoriteBrand() {
        return favoriteBrand;
    }

    public List<PartnerNetFunctionalNumberDTO> getFunctionalNumbers() {
        return functionalNumbers;
    }

    public List<PartnerNetCompanyDTO> getEmployments() {
        return employments;
    }

    public List<PartnerNetCompanyAddressDTO> getEmploymentsAddress() {
        return employmentsAddress;
    }

    public List<PartnerNetRoleDTO> getRoles() {
        return roles;
    }

    public List<PartnerNetContractDTO> getContracts() {
        return contracts;
    }

    public Collection<Integer> getContactCompanyIds() {
        return contactCompanyIds;
    }

    public Collection<PartnerNetCompanyTypeDTO> getCompanyTypes() {
        return companyTypes;
    }

    public boolean isResponsibleUserAvailable() {
        return responsibleUser;
    }

    public String getResponsibleUserExternalId() {
        return responsibleUserExternalId;
    }

    public String getResponsibleUserFirstname() {
        return responsibleUserFirstname;
    }

    public String getResponsibleUserLastname() {
        return responsibleUserLastname;
    }

    public String getResponsibleUserEmail() {
        return responsibleUserEmail;
    }

    public String getResponsibleUserGuid() {
        return responsibleUserGuid;
    }

    public boolean isSupportDataAvailable() {
        return supportData;
    }

    public List<PartnerNetCompanyDTO> getSupportEmployments() {
        return supportEmployments;
    }

    public List<PartnerNetCompanyAddressDTO> getSupportEmploymentsAddress() {
        return supportEmploymentsAddress;
    }

    public List<PartnerNetRoleDTO> getSupportRoles() {
        return supportRoles;
    }

    public List<PartnerNetContractDTO> getSupportContracts() {
        return supportContracts;
    }

    public Collection<Integer> getSupportContactCompanyIds() {
        return supportContactCompanyIds;
    }

    public Collection<PartnerNetCompanyTypeDTO> getSupportCompanyTypes() {
        return supportCompanyTypes;
    }

    public String getName() {
        String givenName = getFirstname();
        String familyName = getLastname();

        if (givenName == null || familyName == null) {
            return getSubjectIdentifier();
        }

        return givenName + " " + familyName;
    }

    public String getResponsibleUserName() {
        String givenName = getFirstname();
        String familyName = getLastname();

        if (givenName == null || familyName == null) {
            return getSubjectIdentifier();
        }

        return givenName + " " + familyName;
    }

    public String getTransientSessionId() {
        return transientSessionId;
    }

    public AuthnContextClass getAuthnContextClass() {
        return authnContextClass;
    }

    public boolean isStronglyAuthenticated() {
        return authnContextClass.isStrongerThan(AuthnContextClass.USERPASS);
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }
}
