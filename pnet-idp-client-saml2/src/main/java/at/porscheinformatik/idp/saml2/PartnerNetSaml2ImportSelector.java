package at.porscheinformatik.idp.saml2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import at.porscheinformatik.idp.saml2.workaround.PartnerNetSaml2SerializationFixConfiguration;

public class PartnerNetSaml2ImportSelector implements ImportSelector
{
    public static final String REGISTER_SERIALIZATION_FIX_ATTRIBUTE = "registerSerializationFix";

    /**
     *
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata)
    {
        List<String> importClasses = new ArrayList<>();
        importClasses.add(PartnerNetSaml2Configuration.class.getName());

        Map<String, Object> metadata =
            importingClassMetadata.getAnnotationAttributes(EnablePartnerNetSaml2.class.getName());

        Boolean registerFix = (Boolean) metadata.getOrDefault(REGISTER_SERIALIZATION_FIX_ATTRIBUTE, Boolean.FALSE);

        if (registerFix.booleanValue())
        {
            importClasses.add(PartnerNetSaml2SerializationFixConfiguration.class.getName());
        }

        return importClasses.toArray(String[]::new);
    }

}
