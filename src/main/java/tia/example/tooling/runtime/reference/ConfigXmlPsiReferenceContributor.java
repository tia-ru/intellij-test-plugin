package tia.example.tooling.runtime.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tia.example.tooling.runtime.util.ConfigXmlUtils.*;

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
        addRef(registrar, TAG_REF, ATTRIBUTE_TYPE, TAG_DOP, ATTRIBUTE_NAME);

        // Self reference.
        addRef(registrar, TAG_DOP, ATTRIBUTE_NAME, TAG_DOP, ATTRIBUTE_NAME);
        //<domain-object-type extends="SS_ModuleAdd"> --> <domain-object-type name="SS_ModuleAdd">
        addRef(registrar, TAG_DOP, ATTRIBUTE_EXTENDS, TAG_DOP, ATTRIBUTE_NAME);

        //<include-group name="GroupActions" --> <field-group name="GroupActions"> />
        addRef(registrar, TAG_INCLUDE_GROUP, ATTRIBUTE_NAME, TAG_FIELD_GROUP, ATTRIBUTE_NAME);
        // Self reference.
        addRef(registrar, TAG_FIELD_GROUP, ATTRIBUTE_NAME, TAG_FIELD_GROUP, ATTRIBUTE_NAME);

        // Self reference.
        addRef(registrar, "collection", ATTRIBUTE_NAME, "collection", ATTRIBUTE_NAME);

        // Self reference.
        addRef(registrar, "attachment-type", ATTRIBUTE_NAME, TAG_DOP + "/attachment-types/attachment-type", ATTRIBUTE_NAME);
        //<attachment-type storage="SOPersonFiles" />  --> <attachment-storage name = "SOPersonFiles">
        addRef(registrar, "attachment-type", "storage", "attachment-storage", ATTRIBUTE_NAME);
        // Self reference.
        addRef(registrar, "attachment-storage", ATTRIBUTE_NAME, "attachment-storage", ATTRIBUTE_NAME);


        // Self reference.
        addRef(registrar, "context-role", ATTRIBUTE_NAME, "context-role", ATTRIBUTE_NAME);
        addRef(registrar, "permit-role", ATTRIBUTE_NAME, "context-role", ATTRIBUTE_NAME);

        // Self reference.
        addRef(registrar, "dynamic-group", ATTRIBUTE_NAME, "dynamic-group", ATTRIBUTE_NAME);
        // Self reference.
        addRef(registrar, "static-group", ATTRIBUTE_NAME, "static-group", ATTRIBUTE_NAME);

        List<ConfigXmlAttribute> groupPaths = Arrays.asList(
                new ConfigXmlAttribute("dynamic-group", ATTRIBUTE_NAME),
                new ConfigXmlAttribute("static-group", ATTRIBUTE_NAME)
        );
        addRef(registrar, "permit-group", ATTRIBUTE_NAME, groupPaths);


        List<ConfigXmlAttribute> dopTypePaths = Arrays.asList(
            new ConfigXmlAttribute(TAG_DOP, ATTRIBUTE_NAME),
            new ConfigXmlAttribute(TAG_DOP + "/attachment-types/attachment-type", ATTRIBUTE_NAME)
        );
        addRef(registrar, "access-matrix", "type", dopTypePaths);


        // UI ===============================================================================================
        addRef(registrar, "form", "domain-object-type", TAG_DOP, ATTRIBUTE_NAME);
        addRef(registrar, "form", ATTRIBUTE_NAME, "form", ATTRIBUTE_NAME);

        addRef(registrar, "form-mapping", "domain-object-type", TAG_DOP, ATTRIBUTE_NAME);
        addRef(registrar, "form-mapping", "form", "form", ATTRIBUTE_NAME);

        addRef(registrar, "collection-view", "collection", "collection", ATTRIBUTE_NAME);
        addRef(registrar, "collection-ref", ATTRIBUTE_NAME, "collection", ATTRIBUTE_NAME);

        addRef(registrar, NS_AF5_ACTION, "action", ATTRIBUTE_NAME, "action", ATTRIBUTE_NAME);
        addRef(registrar, NS_AF5_ACTION, "action-ref", "name-ref", "action", ATTRIBUTE_NAME);
    }


    private void addRef(@NotNull PsiReferenceRegistrar registrar, String refTag, String refAtt, String toTag, String toAtt){
        addRef(registrar, NS_AF5_CONFIG, refTag, refAtt, toTag, toAtt);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String refTag, String refAtt, List<ConfigXmlAttribute> pathsToAttributes){
        addRef(registrar, NS_AF5_CONFIG, refTag, refAtt, pathsToAttributes);
    }
    private void addRef(@NotNull PsiReferenceRegistrar registrar, String namespace, String refTag, String refAtt, String toTag, String toAtt){
        List<ConfigXmlAttribute> toAttributes = new ArrayList<>(1);
        ConfigXmlAttribute attribute = new ConfigXmlAttribute(toTag, toAtt);
        toAttributes.add(attribute);
        addRef(registrar, namespace, refTag, refAtt, toAttributes);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String namespace, String refTag, String refAtt, List<ConfigXmlAttribute> pathsToAttributes){

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(pathsToAttributes));
    }
}


