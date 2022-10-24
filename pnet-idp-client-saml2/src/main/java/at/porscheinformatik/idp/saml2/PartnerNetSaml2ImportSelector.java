package at.porscheinformatik.idp.saml2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class PartnerNetSaml2ImportSelector implements ImportSelector
{
    /**
     *
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata)
    {
        List<String> importClasses = new ArrayList<>();
        importClasses.add(PartnerNetSaml2Configuration.class.getName());

        return importClasses.toArray(String[]::new);
    }

}
