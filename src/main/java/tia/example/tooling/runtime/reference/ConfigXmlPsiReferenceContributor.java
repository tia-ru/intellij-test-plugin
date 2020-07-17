package tia.example.tooling.runtime.reference;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
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

    private static final ElementPattern<? extends PsiElement> ATTR_ID_FILTER = FilterBuilder.create()
            .withNamespace(NS_AF5_CONFIG)
            .addIdPath(TAG_DOP, ATTRIBUTE_NAME)
            .addIdPath(TAG_FIELD_GROUP, ATTRIBUTE_NAME)
            .addIdPath("attachment-type", ATTRIBUTE_NAME)
            .addIdPath("collection", ATTRIBUTE_NAME)
            .addIdPath("attachment-storage", ATTRIBUTE_NAME)
            .addIdPath("context-role", ATTRIBUTE_NAME)
            .addIdPath("dynamic-group", ATTRIBUTE_NAME)
            .addIdPath("static-group", ATTRIBUTE_NAME)
            .addIdPath("form", ATTRIBUTE_NAME)
            .withNamespace(NS_AF5_ACTION)
            .addIdPath("action", ATTRIBUTE_NAME)
            .build();

    private static final XmlIdCache ID_CACHE = new XmlIdCache(ATTR_ID_FILTER);

    public static XmlIdCache getIdCache(){
        return ID_CACHE;
    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        // Self references.
        registrar.registerReferenceProvider(ATTR_ID_FILTER, new ConfigXmlIdProvider());


        // Self reference.
        //addRef(registrar, TAG_DOP, ATTRIBUTE_NAME, TAG_DOP, ATTRIBUTE_NAME);
        // <reference name="Type" type="SS_ModuleType"/>  -->  <domain-object-type name="SS_ModuleType">
        addRef(registrar, TAG_REF, ATTRIBUTE_TYPE, TAG_DOP, ATTRIBUTE_NAME);
        //<domain-object-type extends="SS_ModuleAdd"> --> <domain-object-type name="SS_ModuleAdd">
        addRef(registrar, TAG_DOP, ATTRIBUTE_EXTENDS, TAG_DOP, ATTRIBUTE_NAME);

        //<include-group name="GroupActions" --> <field-group name="GroupActions"> />
        addRef(registrar, TAG_INCLUDE_GROUP, ATTRIBUTE_NAME, TAG_FIELD_GROUP, ATTRIBUTE_NAME);
        //<attachment-type storage="SOPersonFiles" />  --> <attachment-storage name = "SOPersonFiles">
        addRef(registrar, "attachment-type", "storage", "attachment-storage", ATTRIBUTE_NAME);
        addRef(registrar, "permit-role", ATTRIBUTE_NAME, "context-role", ATTRIBUTE_NAME);


        List<XmlIdPath> groupPaths = Arrays.asList(
                new XmlIdPath(NS_AF5_CONFIG, "dynamic-group", ATTRIBUTE_NAME),
                new XmlIdPath(NS_AF5_CONFIG, "static-group", ATTRIBUTE_NAME)
        );
        addRef(registrar, "permit-group", ATTRIBUTE_NAME, groupPaths);


        List<XmlIdPath> dopTypePaths = Arrays.asList(
            new XmlIdPath(NS_AF5_CONFIG, TAG_DOP, ATTRIBUTE_NAME),
            new XmlIdPath(NS_AF5_CONFIG,TAG_DOP + "/attachment-types/attachment-type", ATTRIBUTE_NAME)
        );
        addRef(registrar, "access-matrix", "type", dopTypePaths);


        // UI ===============================================================================================
        addRef(registrar, "form", "domain-object-type", TAG_DOP, ATTRIBUTE_NAME);
        //addRef(registrar, "form", ATTRIBUTE_NAME, "form", ATTRIBUTE_NAME);

        addRef(registrar, "form-mapping", "domain-object-type", TAG_DOP, ATTRIBUTE_NAME);
        addRef(registrar, "form-mapping", "form", "form", ATTRIBUTE_NAME);

        addRef(registrar, "collection-view", "collection", "collection", ATTRIBUTE_NAME);
        addRef(registrar, "collection-ref", ATTRIBUTE_NAME, "collection", ATTRIBUTE_NAME);

        //addRef(registrar, NS_AF5_ACTION, "action", ATTRIBUTE_NAME, "action", ATTRIBUTE_NAME);
        addRef(registrar, NS_AF5_ACTION, "action-ref", "name-ref", "action", ATTRIBUTE_NAME);
    }


    private void addRef(@NotNull PsiReferenceRegistrar registrar, String refTag, String refAtt, String toTag, String toAtt){
        addRef(registrar, NS_AF5_CONFIG, refTag, refAtt, toTag, toAtt);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String refTag, String refAtt, List<XmlIdPath> pathsToAttributes){
        addRef(registrar, NS_AF5_CONFIG, refTag, refAtt, pathsToAttributes);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String namespace, String refTag, String refAtt, String toTag, String toAtt){
        List<XmlIdPath> toAttributes = new ArrayList<>(1);
        XmlIdPath attribute = new XmlIdPath(namespace, toTag, toAtt);
        toAttributes.add(attribute);
        addRef(registrar, namespace, refTag, refAtt, toAttributes);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String namespace, String refTag, String refAtt, List<XmlIdPath> pathsToAttributes){

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(pathsToAttributes, ID_CACHE));
    }

}


