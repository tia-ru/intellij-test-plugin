package tia.example.tooling.runtime.reference;

import org.jetbrains.annotations.NotNull;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;

import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_EXTENDS;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_NAME;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_TYPE;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.NS_AF5_ACTION;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.NS_AF5_CONFIG;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_DOP;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_FIELD_GROUP;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_INCLUDE_GROUP;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_REF;

/**
 * Register IntelliJ's reference providers for AF5 config reference xml-attributes.
 * It enables navigation actions 'Declaration or Usage', 'Find usage' and `rename` refactoring.
 * <p/>
 * Declarations (reference target tags) has to has self reference to enable 'find usage' action and `rename` refactoring within them.
 */
public class ConfigXmlPsiReferenceContributor extends PsiReferenceContributor {


    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        // <reference name="Type" type="SS_ModuleType"/>  -->  <domain-object-type name="SS_ModuleType">
        addRef(TAG_REF, ATTRIBUTE_TYPE, TAG_DOP, ATTRIBUTE_NAME, registrar);

        // Self reference.
        addRef(TAG_DOP, ATTRIBUTE_NAME, TAG_DOP, ATTRIBUTE_NAME, registrar);
        //<domain-object-type extends="SS_ModuleAdd"> --> <domain-object-type name="SS_ModuleAdd">
        addRef(TAG_DOP, ATTRIBUTE_EXTENDS, TAG_DOP, ATTRIBUTE_NAME, registrar);

        //<include-group name="GroupActions" --> <field-group name="GroupActions"> />
        addRef(TAG_INCLUDE_GROUP, ATTRIBUTE_NAME, TAG_FIELD_GROUP, ATTRIBUTE_NAME, registrar);
        // Self reference.
        addRef(TAG_FIELD_GROUP, ATTRIBUTE_NAME, TAG_FIELD_GROUP, ATTRIBUTE_NAME, registrar);

        // Self reference.
        addRef("collection", ATTRIBUTE_NAME, "collection", ATTRIBUTE_NAME, registrar);

        //<attachment-type storage="SOPersonFiles" />  --> <attachment-storage name = "SOPersonFiles">
        addRef("attachment-type", "storage", "attachment-storage", ATTRIBUTE_NAME, registrar);
        // Self reference.
        addRef("attachment-storage", ATTRIBUTE_NAME, "attachment-storage", ATTRIBUTE_NAME, registrar);

        // UI ===============================================================================================
        addRef("form", "domain-object-type", "domain-object-type", ATTRIBUTE_NAME, registrar);
        addRef("form", ATTRIBUTE_NAME, "form", ATTRIBUTE_NAME, registrar);

        addRef("form-mapping", "domain-object-type", TAG_DOP, ATTRIBUTE_NAME, registrar);
        addRef("form-mapping", "form", "form", ATTRIBUTE_NAME, registrar);

        addRef("collection-view", "collection", "collection", ATTRIBUTE_NAME, registrar);
        addRef("collection-ref", ATTRIBUTE_NAME, "collection", ATTRIBUTE_NAME, registrar);

        addRef(NS_AF5_ACTION, "action", ATTRIBUTE_NAME, "action", ATTRIBUTE_NAME, registrar);
        addRef(NS_AF5_ACTION, "action-ref", "name-ref", "action", ATTRIBUTE_NAME, registrar);
    }


    private void addRef(String refTag, String refAtt, String toTag, String toAtt, @NotNull PsiReferenceRegistrar registrar){
        addRef(NS_AF5_CONFIG, refTag, refAtt, toTag, toAtt, registrar);
    }

    private void addRef(String namespace, String refTag, String refAtt, String toTag, String toAtt, @NotNull PsiReferenceRegistrar registrar){

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(toTag, toAtt));
    }
}


