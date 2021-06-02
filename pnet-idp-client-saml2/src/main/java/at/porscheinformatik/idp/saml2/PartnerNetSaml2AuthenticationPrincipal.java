package at.porscheinformatik.idp.saml2;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import at.porscheinformatik.idp.PartnerNetCompanyAddressDTO;
import at.porscheinformatik.idp.PartnerNetCompanyDTO;
import at.porscheinformatik.idp.PartnerNetContractDTO;
import at.porscheinformatik.idp.PartnerNetFunctionalNumberDTO;
import at.porscheinformatik.idp.PartnerNetRoleDTO;

public class PartnerNetSaml2AuthenticationPrincipal implements Serializable
{
    private static final long serialVersionUID = 8462523068524794768L;

    private final String subjectIdentifier;
    private final String relayState;
    private final String transientSessionId;

    private final String guid;
    private final String personnelNumber;
    private final Integer legacyId;

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

    private final List<PartnerNetFunctionalNumberDTO> functionalNumbers;

    private final List<PartnerNetCompanyDTO> employments;
    private final List<PartnerNetCompanyAddressDTO> employmentsAddress;
    private final List<PartnerNetRoleDTO> roles;
    private final List<PartnerNetContractDTO> contracts;

    private final boolean supportData;
    private final List<PartnerNetCompanyDTO> supportEmployments;
    private final List<PartnerNetCompanyAddressDTO> supportEmploymentsAddress;
    private final List<PartnerNetRoleDTO> supportRoles;
    private final List<PartnerNetContractDTO> supportContracts;

    public PartnerNetSaml2AuthenticationPrincipal(String subjectIdentifier, String relayState,
        String transientSessionId, String guid, String personnelNumber, Integer legacyId, String academicTitle,
        String academicTitlePostNominal, String firstname, String lastname, Gender gender, Locale language,
        List<Locale> additionalLanguages, String mailAddress, String phoneNumber, String tenant, String costCenter,
        List<PartnerNetFunctionalNumberDTO> functionalNumbers, List<PartnerNetCompanyDTO> employments,
        List<PartnerNetCompanyAddressDTO> employmentsAddress, List<PartnerNetRoleDTO> roles,
        List<PartnerNetContractDTO> contracts, boolean supportData, List<PartnerNetCompanyDTO> supportEmployments,
        List<PartnerNetCompanyAddressDTO> supportEmploymentsAddress, List<PartnerNetRoleDTO> supportRoles,
        List<PartnerNetContractDTO> supportContracts)
    {
        super();

        this.subjectIdentifier = subjectIdentifier;
        this.relayState = relayState;
        this.transientSessionId = transientSessionId;
        this.guid = guid;
        this.personnelNumber = personnelNumber;
        this.legacyId = legacyId;
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
        this.functionalNumbers = functionalNumbers;
        this.employments = employments;
        this.employmentsAddress = employmentsAddress;
        this.roles = roles;
        this.contracts = contracts;
        this.supportData = supportData;
        this.supportEmployments = supportEmployments;
        this.supportEmploymentsAddress = supportEmploymentsAddress;
        this.supportRoles = supportRoles;
        this.supportContracts = supportContracts;
    }

    public String getSubjectIdentifier()
    {
        return subjectIdentifier;
    }

    public String getRelayState()
    {
        return relayState;
    }

    public String getGuid()
    {
        return guid;
    }

    public String getPersonnelNumber()
    {
        return personnelNumber;
    }

    public Integer getLegacyId()
    {
        return legacyId;
    }

    public String getAcademicTitle()
    {
        return academicTitle;
    }

    public String getAcademicTitlePostNominal()
    {
        return academicTitlePostNominal;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public Gender getGender()
    {
        return gender;
    }

    public Locale getLanguage()
    {
        return language;
    }

    public List<Locale> getAdditionalLanguages()
    {
        return additionalLanguages;
    }

    public String getMailAddress()
    {
        return mailAddress;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public String getTenant()
    {
        return tenant;
    }

    public String getCostCenter()
    {
        return costCenter;
    }

    public List<PartnerNetFunctionalNumberDTO> getFunctionalNumbers()
    {
        return functionalNumbers;
    }

    public List<PartnerNetCompanyDTO> getEmployments()
    {
        return employments;
    }

    public List<PartnerNetCompanyAddressDTO> getEmploymentsAddress()
    {
        return employmentsAddress;
    }

    public List<PartnerNetRoleDTO> getRoles()
    {
        return roles;
    }

    public List<PartnerNetContractDTO> getContracts()
    {
        return contracts;
    }

    public boolean isSupportDataAvailable()
    {
        return supportData;
    }

    public List<PartnerNetCompanyDTO> getSupportEmployments()
    {
        return supportEmployments;
    }

    public List<PartnerNetCompanyAddressDTO> getSupportEmploymentsAddress()
    {
        return supportEmploymentsAddress;
    }

    public List<PartnerNetRoleDTO> getSupportRoles()
    {
        return supportRoles;
    }

    public List<PartnerNetContractDTO> getSupportContracts()
    {
        return supportContracts;
    }

    public String getName()
    {
        String givenName = getFirstname();
        String familyName = getLastname();

        if (givenName == null || familyName == null)
        {
            return getSubjectIdentifier();
        }

        return givenName + " " + familyName;
    }

    public String getTransientSessionId()
    {
        return transientSessionId;
    }

}
