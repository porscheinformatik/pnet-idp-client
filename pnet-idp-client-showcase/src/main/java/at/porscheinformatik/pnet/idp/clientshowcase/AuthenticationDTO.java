/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase;

import java.util.Collection;
import java.util.List;

import at.porscheinformatik.idp.PartnerNetCompanyAddressDTO;
import at.porscheinformatik.idp.PartnerNetCompanyDTO;
import at.porscheinformatik.idp.PartnerNetContractDTO;
import at.porscheinformatik.idp.PartnerNetFunctionalNumberDTO;
import at.porscheinformatik.idp.PartnerNetRoleDTO;
import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser;

/**
 * @author Daniel Furtlehner
 */
public class AuthenticationDTO
{
    public static AuthenticationDTO info(String info)
    {
        return new AuthenticationDTO(info, null, false, null, false, 0, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null);
    }

    public static AuthenticationDTO of(PartnerNetOpenIdConnectUser principal)
    {
        return new AuthenticationDTO("OpenId Connect", principal.getExternalId(),
            principal.getNistAuthenticationLevel() > 2, principal.getTransientSessionId(),
            principal.isSupportDataAvailable(), principal.getNistAuthenticationLevel(), principal.getName(),
            principal.getAcademicTitle(), principal.getAcademicTitlePostNominal(), principal.getGuid(),
            principal.getCostcentre(), principal.getCountry(), principal.getAdditionalLocales(),
            principal.getFunctionalNumbers(), principal.getCompanies(), principal.getCompaniesAddress(),
            principal.getRoles(), principal.getContracts(), principal.getSupportCompanies(),
            principal.getSupportCompaniesAddress(), principal.getSupportRoles(), principal.getSupportContract());
    }

    private final String info;
    private final String externalId;
    private final boolean secondFactorUsed;

    private final String transientSessionId;
    private final boolean supportDataAvailable;
    private final int nistAuthenticationLevel;
    private final String name;
    private final String academicTitle;
    private final String academicTitlePostNominal;
    private final String guid;
    private final String costcentre;
    private final String country;
    private final List<String> additionalLocales;
    private final Collection<PartnerNetFunctionalNumberDTO> functionalNumbers;
    private final Collection<PartnerNetCompanyDTO> companies;
    private final Collection<PartnerNetCompanyAddressDTO> companiesAddress;
    private final Collection<PartnerNetRoleDTO> roles;
    private final Collection<PartnerNetContractDTO> contracts;
    private final Collection<PartnerNetCompanyDTO> supportCompanies;
    private final Collection<PartnerNetCompanyAddressDTO> supportCompaniesAddress;
    private final Collection<PartnerNetRoleDTO> supportRoles;
    private final Collection<PartnerNetContractDTO> supportContract;

    public AuthenticationDTO(String info, String externalId, boolean secondFactorUsed, String transientSessionId,
        boolean supportDataAvailable, int nistAuthenticationLevel, String name, String academicTitle,
        String academicTitlePostNominal, String guid, String costcentre, String country, List<String> additionalLocales,
        Collection<PartnerNetFunctionalNumberDTO> functionalNumbers, Collection<PartnerNetCompanyDTO> companies,
        Collection<PartnerNetCompanyAddressDTO> companiesAddress, Collection<PartnerNetRoleDTO> roles,
        Collection<PartnerNetContractDTO> contracts, Collection<PartnerNetCompanyDTO> supportCompanies,
        Collection<PartnerNetCompanyAddressDTO> supportCompaniesAddress, Collection<PartnerNetRoleDTO> supportRoles,
        Collection<PartnerNetContractDTO> supportContract)
    {
        super();

        this.info = info;
        this.externalId = externalId;
        this.secondFactorUsed = secondFactorUsed;
        this.transientSessionId = transientSessionId;
        this.supportDataAvailable = supportDataAvailable;
        this.nistAuthenticationLevel = nistAuthenticationLevel;
        this.name = name;
        this.academicTitle = academicTitle;
        this.academicTitlePostNominal = academicTitlePostNominal;
        this.guid = guid;
        this.costcentre = costcentre;
        this.country = country;
        this.additionalLocales = additionalLocales;
        this.functionalNumbers = functionalNumbers;
        this.companies = companies;
        this.companiesAddress = companiesAddress;
        this.roles = roles;
        this.contracts = contracts;
        this.supportCompanies = supportCompanies;
        this.supportCompaniesAddress = supportCompaniesAddress;
        this.supportRoles = supportRoles;
        this.supportContract = supportContract;
    }

    public String getInfo()
    {
        return info;
    }

    public String getExternalId()
    {
        return externalId;
    }

    public boolean isSecondFactorUsed()
    {
        return secondFactorUsed;
    }

    public String getTransientSessionId()
    {
        return transientSessionId;
    }

    public boolean isSupportDataAvailable()
    {
        return supportDataAvailable;
    }

    public int getNistAuthenticationLevel()
    {
        return nistAuthenticationLevel;
    }

    public String getName()
    {
        return name;
    }

    public String getAcademicTitle()
    {
        return academicTitle;
    }

    public String getAcademicTitlePostNominal()
    {
        return academicTitlePostNominal;
    }

    public String getGuid()
    {
        return guid;
    }

    public String getCostcentre()
    {
        return costcentre;
    }

    public String getCountry()
    {
        return country;
    }

    public List<String> getAdditionalLocales()
    {
        return additionalLocales;
    }

    public Collection<PartnerNetFunctionalNumberDTO> getFunctionalNumbers()
    {
        return functionalNumbers;
    }

    public Collection<PartnerNetCompanyDTO> getCompanies()
    {
        return companies;
    }

    public Collection<PartnerNetCompanyAddressDTO> getCompaniesAddress()
    {
        return companiesAddress;
    }

    public Collection<PartnerNetRoleDTO> getRoles()
    {
        return roles;
    }

    public Collection<PartnerNetContractDTO> getContracts()
    {
        return contracts;
    }

    public Collection<PartnerNetCompanyDTO> getSupportCompanies()
    {
        return supportCompanies;
    }

    public Collection<PartnerNetCompanyAddressDTO> getSupportCompaniesAddress()
    {
        return supportCompaniesAddress;
    }

    public Collection<PartnerNetRoleDTO> getSupportRoles()
    {
        return supportRoles;
    }

    public Collection<PartnerNetContractDTO> getSupportContract()
    {
        return supportContract;
    }

}
