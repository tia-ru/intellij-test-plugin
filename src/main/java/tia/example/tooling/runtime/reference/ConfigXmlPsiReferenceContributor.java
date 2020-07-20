package tia.example.tooling.runtime.reference;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import tia.example.tooling.runtime.util.FilterBuilder;

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

    private static final ElementPattern<XmlAttributeValue> ATTR_ID_FILTER = FilterBuilder.create()
            .withNamespace(NS_AF5_CONFIG, ns1 -> ns1
                    .addIdPath(TAG_DOP, ATTRIBUTE_NAME)
                    .addIdPath(TAG_FIELD_GROUP, ATTRIBUTE_NAME)
                    .addIdPath("attachment-type", ATTRIBUTE_NAME)
                    .addIdPath("collection", ATTRIBUTE_NAME)
                    .addIdPath("attachment-storage", ATTRIBUTE_NAME)
                    .addIdPath("context-role", ATTRIBUTE_NAME)
                    .addIdPath("dynamic-group", ATTRIBUTE_NAME)
                    .addIdPath("static-group", ATTRIBUTE_NAME)
                    //UI
                    .addIdPath("form", ATTRIBUTE_NAME)

                    // Widgets
                    .insideTag("widget-configs", t1 -> t1
                            .addIdPath("action-executor", ATTRIBUTE_ID)
                            .addIdPath("attachment-box", ATTRIBUTE_ID)
                            .addIdPath("attachment-text-area", ATTRIBUTE_ID)
                            .addIdPath("attachment-text-box", ATTRIBUTE_ID)
                            .addIdPath("attachment-viewer", ATTRIBUTE_ID)
                            .addIdPath("changed-field-viewer", ATTRIBUTE_ID)
                            .addIdPath("check-box", ATTRIBUTE_ID)
                            .addIdPath("combo-box", ATTRIBUTE_ID)
                            .addIdPath("date-box", ATTRIBUTE_ID)
                            .addIdPath("decimal-box", ATTRIBUTE_ID)
                            .addIdPath("editable-table-browser", ATTRIBUTE_ID)
                            .addIdPath("enumeration-box", ATTRIBUTE_ID)
                            .addIdPath("hierarchy-browser", ATTRIBUTE_ID)
                            .addIdPath("integer-box", ATTRIBUTE_ID)
                            .addIdPath("label", ATTRIBUTE_ID)
                            .addIdPath("linked-domain-object-hyperlink", ATTRIBUTE_ID)
                            .addIdPath("linked-domain-objects-editable-table", ATTRIBUTE_ID)
                            .addIdPath("linked-domain-objects-table", ATTRIBUTE_ID)
                            .addIdPath("list-box", ATTRIBUTE_ID)
                            .addIdPath("list-cell", ATTRIBUTE_ID)
                            .addIdPath("radio-button", ATTRIBUTE_ID)
                            .addIdPath("rich-attachment-text-area", ATTRIBUTE_ID)
                            .addIdPath("rich-text-area", ATTRIBUTE_ID)
                            .addIdPath("suggest-box", ATTRIBUTE_ID)
                            .addIdPath("table-browser", ATTRIBUTE_ID)
                            .addIdPath("table-viewer", ATTRIBUTE_ID)
                            .addIdPath("template-based-widget", ATTRIBUTE_ID)
                            .addIdPath("text-area", ATTRIBUTE_ID)
                            .addIdPath("text-box", ATTRIBUTE_ID)
                    )
            )
            .withNamespace(NS_AF5_ACTION, ns2 -> ns2
                    .addIdPath("action", ATTRIBUTE_NAME)
            )
            .build();

    private static final XmlIdCache ID_CACHE = new XmlIdCache(ATTR_ID_FILTER);

    public static XmlIdCache getIdCache() {
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
                new XmlIdPath(NS_AF5_CONFIG, TAG_DOP + "/attachment-types/attachment-type", ATTRIBUTE_NAME)
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

        List<XmlIdPath> widgetPaths = Arrays.asList(
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/action-executor", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/attachment-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/attachment-text-area", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/attachment-text-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/attachment-viewer", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/changed-field-viewer", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/check-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/combo-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/date-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/decimal-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/editable-table-browser", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/enumeration-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/hierarchy-browser", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/integer-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/label", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/linked-domain-object-hyperlink", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/linked-domain-objects-editable-table", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/linked-domain-objects-table", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/list-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/list-cell", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/radio-button", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/rich-attachment-text-area", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/rich-text-area", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/suggest-box", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/table-browser", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/table-viewer", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/template-based-widget", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/text-area", ATTRIBUTE_ID),
                new XmlIdPath(NS_AF5_CONFIG, "widget-config/text-box", ATTRIBUTE_ID)
        );
        addRef(registrar, "widget", "id", widgetPaths);

    }


    private void addRef(@NotNull PsiReferenceRegistrar registrar, String refTag, String refAtt, String toTag, String toAtt) {
        addRef(registrar, NS_AF5_CONFIG, refTag, refAtt, toTag, toAtt);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String refTag, String refAtt, List<XmlIdPath> pathsToAttributes) {
        addRef(registrar, NS_AF5_CONFIG, refTag, refAtt, pathsToAttributes);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String namespace, String refTag, String refAtt, String toTag, String toAtt) {
        List<XmlIdPath> toAttributes = new ArrayList<>(1);
        XmlIdPath attribute = new XmlIdPath(namespace, toTag, toAtt);
        toAttributes.add(attribute);
        addRef(registrar, namespace, refTag, refAtt, toAttributes);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, String namespace, String refTag, String refAtt, List<XmlIdPath> pathsToAttributes) {

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(pathsToAttributes, ID_CACHE));
    }

}


