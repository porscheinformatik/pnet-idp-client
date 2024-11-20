/**
 *
 */
package at.porscheinformatik.idp.openidconnect.convert;

import static at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser.*;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.converter.ClaimConversionService;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetClaimTypeConverterFactory
    implements Function<ClientRegistration, Converter<Map<String, Object>, Map<String, Object>>> {

    private static final ClaimTypeConverter PNET_CLAIM_TYPE_CONVERTER = new ClaimTypeConverter(
        createPnetClaimTypeConverters()
    );

    private static Map<String, Converter<Object, ?>> createPnetClaimTypeConverters() {
        ClaimConversionService.getSharedInstance().addConverter(new ObjectToIntegerConverter());

        Converter<Object, ?> booleanConverter = getConverter(TypeDescriptor.valueOf(Boolean.class));
        Converter<Object, ?> intConverter = getConverter(TypeDescriptor.valueOf(Integer.class));
        Converter<Object, ?> stringConverter = getConverter(TypeDescriptor.valueOf(String.class));
        PartnerNetFunctionalNumberConverter functionalNumberConverter = new PartnerNetFunctionalNumberConverter(
            intConverter,
            stringConverter
        );
        PartnerNetCompanyConverter companyConverter = new PartnerNetCompanyConverter(intConverter, stringConverter);
        PartnerNetCompanyAddressConverter companyAddressConverter = new PartnerNetCompanyAddressConverter(
            intConverter,
            stringConverter
        );
        PartnerNetRoleConverter roleConverter = new PartnerNetRoleConverter(intConverter, stringConverter);
        PartnerNetContractConverter contractConverter = new PartnerNetContractConverter(intConverter, stringConverter);
        PartnerNetContactCompaniesConverter contactCompaniesConverter = new PartnerNetContactCompaniesConverter();
        PartnerNetCompanyTypesConverter companyTypesConverter = new PartnerNetCompanyTypesConverter(
            intConverter,
            stringConverter
        );

        Map<String, Converter<Object, ?>> converters = OidcUserService.createDefaultClaimTypeConverters();

        converters.put(USER_INFO_ADDITIONAL_LOCALES, new AdditionalLocalesConverter());

        converters.put(ID_TOKEN_SUPPORT_AVAILABLE, booleanConverter);
        converters.put(USER_INFO_FUNCTIONAL_NUMBERS, functionalNumberConverter);
        converters.put(USER_INFO_COMPANIES, companyConverter);
        converters.put(USER_INFO_COMPANIES_ADDRESS, companyAddressConverter);
        converters.put(USER_INFO_CONTACT_COMPANIES, contactCompaniesConverter);
        converters.put(USER_INFO_COMPANY_TYPES, companyTypesConverter);
        converters.put(USER_INFO_ROLES, roleConverter);
        converters.put(USER_INFO_CONTRACTS, contractConverter);

        converters.put(USER_INFO_SUPPORT_COMPANIES, companyConverter);
        converters.put(USER_INFO_SUPPORT_COMPANIES_ADDRESS, companyAddressConverter);
        converters.put(USER_INFO_SUPPORT_ROLES, roleConverter);
        converters.put(USER_INFO_SUPPORT_CONTRACTS, contractConverter);
        converters.put(USER_INFO_SUPPORT_COMPANY_TYPES, companyTypesConverter);

        return converters;
    }

    private static Converter<Object, ?> getConverter(TypeDescriptor targetDescriptor) {
        final TypeDescriptor sourceDescriptor = TypeDescriptor.valueOf(Object.class);

        return source -> ClaimConversionService.getSharedInstance().convert(source, sourceDescriptor, targetDescriptor);
    }

    @Override
    public Converter<Map<String, Object>, Map<String, Object>> apply(ClientRegistration t) {
        String registrationId = t.getRegistrationId();

        if (Objects.equals(registrationId, "pnet")) {
            return PNET_CLAIM_TYPE_CONVERTER;
        }

        // Null is perfectly fine, as the default converter is used in this case
        return null;
    }
}
