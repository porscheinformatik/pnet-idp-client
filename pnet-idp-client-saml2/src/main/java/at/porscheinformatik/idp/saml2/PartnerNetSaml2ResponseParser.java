/**
 *
 */
package at.porscheinformatik.idp.saml2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.util.Assert;

import at.porscheinformatik.idp.PartnerNetCompanyAddressDTO;
import at.porscheinformatik.idp.PartnerNetCompanyDTO;
import at.porscheinformatik.idp.PartnerNetContractDTO;
import at.porscheinformatik.idp.PartnerNetFunctionalNumberDTO;
import at.porscheinformatik.idp.PartnerNetRoleDTO;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetSaml2ResponseParser extends Saml2ResponseParserBase
{
    private final String attributePrefix;
    private final BiFunction<PartnerNetSaml2AuthenticationPrincipal, Saml2Data, Collection<? extends GrantedAuthority>> authoritiesMapper;

    public PartnerNetSaml2ResponseParser(
        BiFunction<PartnerNetSaml2AuthenticationPrincipal, Saml2Data, Collection<? extends GrantedAuthority>> authoritiesMapper)
    {
        super();

        Assert.notNull(authoritiesMapper, "authoritiesMapper must not be null");

        attributePrefix = "https://identity.auto-partner.net/identity/saml2/attributes/";
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    protected Authentication convert(Saml2Data data) throws Saml2Exception
    {
        PartnerNetSaml2AuthenticationPrincipal principal = buildPrincipal(data);
        Collection<? extends GrantedAuthority> authorities = authoritiesMapper.apply(principal, data);

        return new PartnerNetSaml2Authentication(principal, authorities);
    }

    private PartnerNetSaml2AuthenticationPrincipal buildPrincipal(Saml2Data data)
    {
        String guid = singleString(data, attributeName("guid"));
        String personnelNumber = singleString(data, attributeName("personnel_number"));
        Integer legacyId = singleInteger(data, attributeName("person_id"));
        String academicTitle = singleString(data, attributeName("academic_title"));
        String academicTitlePostNominal = singleString(data, attributeName("academic_title_post_nominal"));
        String firstname = singleString(data, attributeName("firstname"));
        String lastname = singleString(data, attributeName("lastname"));
        Gender gender = singleGender(data, attributeName("gender"));
        Locale language = singleLocale(data, attributeName("language"));
        List<Locale> additionalLanguages = localeList(data, attributeName("additional_languages"));
        String mailAddress = singleString(data, attributeName("email"));
        String phoneNumber = singleString(data, attributeName("phone_number"));
        String tenant = singleString(data, attributeName("tenant"));
        String costCenter = singleString(data, attributeName("cost_center"));
        List<PartnerNetFunctionalNumberDTO> functionalNumbers =
            functionalNumbersList(data, attributeName("functional_numbers"));
        List<PartnerNetCompanyDTO> employments = employmentList(data, attributeName("employment"));
        List<PartnerNetCompanyAddressDTO> employmentsAddress = addressList(data, attributeName("employment_address"));
        List<PartnerNetRoleDTO> roles = roleList(data, attributeName("roles"));
        List<PartnerNetContractDTO> contracts = contractsList(data, attributeName("employment_contracts"));
        boolean supportData = singleBoolean(data, attributeName("support_data"));
        List<PartnerNetCompanyDTO> supportEmployments = employmentList(data, attributeName("support_employment"));
        List<PartnerNetCompanyAddressDTO> supportEmploymentsAddress =
            addressList(data, attributeName("support_employment_address"));
        List<PartnerNetRoleDTO> supportRoles = roleList(data, attributeName("support_roles"));
        List<PartnerNetContractDTO> supportContracts =
            contractsList(data, attributeName("support_employment_contracts"));

        return new PartnerNetSaml2AuthenticationPrincipal(data.getSubjectIdentifier(), data.getRelayState(),
            data.getNameId(), data.getAuthnContextClass(), guid, personnelNumber, legacyId, academicTitle,
            academicTitlePostNominal, firstname, lastname, gender, language, additionalLanguages, mailAddress,
            phoneNumber, tenant, costCenter, functionalNumbers, employments, employmentsAddress, roles, contracts,
            supportData, supportEmployments, supportEmploymentsAddress, supportRoles, supportContracts);
    }

    private List<Locale> localeList(Saml2Data data, String attributeName)
    {
        Stream<String> languageTags = stringStream(data, attributeName);

        return languageTags //
            .map(Locale::forLanguageTag)
            .collect(Collectors.toList());
    }

    private List<PartnerNetFunctionalNumberDTO> functionalNumbersList(Saml2Data data, String attributeName)
    {
        return entryStream(data, attributeName).map(entry -> {

            Integer companyId = Integer.parseInt(entry[0]);
            String matchcode = entry[1];
            Integer number = Integer.parseInt(entry[2]);

            return new PartnerNetFunctionalNumberDTO(companyId, matchcode, number);
        }).collect(Collectors.toList());
    }

    private List<PartnerNetContractDTO> contractsList(Saml2Data data, String attributeName)
    {
        return entryStream(data, attributeName).map(entry -> {

            Integer companyId = Integer.parseInt(entry[0]);
            String brandId = entry[1];
            String matchcode = entry[2];

            return new PartnerNetContractDTO(companyId, brandId, matchcode);
        }).collect(Collectors.toList());
    }

    private List<PartnerNetRoleDTO> roleList(Saml2Data data, String attributeName)
    {
        return entryStream(data, attributeName).map(entry -> {

            Integer companyId = Integer.parseInt(entry[0]);
            String brandId = entry[1];
            String matchcode = entry[2];

            return new PartnerNetRoleDTO(companyId, brandId, matchcode);
        }).collect(Collectors.toList());
    }

    private List<PartnerNetCompanyDTO> employmentList(Saml2Data data, String attributeName)
    {
        return entryStream(data, attributeName).map(entry -> {

            Integer companyId = Integer.parseInt(entry[0]);
            String companyNumber = StringUtils.isEmpty(entry[1]) ? null : entry[1];
            String name = entry[2];

            return new PartnerNetCompanyDTO(companyId, companyNumber, name);
        }).collect(Collectors.toList());
    }

    private List<PartnerNetCompanyAddressDTO> addressList(Saml2Data data, String attributeName)
    {
        return entryStream(data, attributeName, ";;").map(entry -> {

            Integer companyId = Integer.parseInt(entry[0]);
            String street = StringUtils.isEmpty(entry[1]) ? null : entry[1];
            String postalCode = StringUtils.isEmpty(entry[2]) ? null : entry[2];
            String locality = StringUtils.isEmpty(entry[3]) ? null : entry[3];
            String countryCode = StringUtils.isEmpty(entry[4]) ? null : entry[4];

            return new PartnerNetCompanyAddressDTO(companyId, street, postalCode, locality, countryCode);
        }).collect(Collectors.toList());
    }

    private Locale singleLocale(Saml2Data data, String attributeName)
    {
        String languageTag = singleString(data, attributeName);

        if (languageTag == null)
        {
            return null;
        }

        return Locale.forLanguageTag(languageTag);
    }

    private Gender singleGender(Saml2Data data, String attributeName)
    {
        Integer value = singleInteger(data, attributeName);

        if (value == null)
        {
            return Gender.UNKNOWN;
        }

        return Gender.fromCode(value);
    }

    private Stream<String[]> entryStream(Saml2Data data, String attributeName)
    {
        return entryStream(data, attributeName, ";");
    }

    private Stream<String[]> entryStream(Saml2Data data, String attributeName, String separator)
    {
        return stringStream(data, attributeName) //
            .map(entry -> entry.split(separator));
    }

    private Stream<String> stringStream(Saml2Data data, String attributeName)
    {
        Object value = data.getAttribute(attributeName);

        if (value == null)
        {
            return Stream.empty();
        }

        List<String> stringEntries = new ArrayList<>();

        if (value instanceof String)
        {
            stringEntries.add((String) value);
        }
        else
        {
            @SuppressWarnings("unchecked")
            List<String> valueList = (List<String>) value;

            stringEntries.addAll(valueList);
        }

        return stringEntries //
            .stream();
    }

    private String singleString(Saml2Data data, String attributeName)
    {
        return data.getAttribute(attributeName);
    }

    private Integer singleInteger(Saml2Data data, String attributeName)
    {
        return data.getAttribute(attributeName);
    }

    private boolean singleBoolean(Saml2Data data, String attributeName)
    {
        Boolean booleanValue = data.getAttribute(attributeName);

        return booleanValue != null ? booleanValue : false;
    }

    private String attributeName(String name)
    {
        return attributePrefix + name;
    }
}
