package tia.example.tooling.runtime.reference;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import tia.example.tooling.runtime.util.PatternBuilder;

import java.util.function.Function;

import static tia.example.tooling.runtime.util.ConfigXmlUtils.*;

/**
 * Register IntelliJ's reference providers for AF5 config reference xml-attributes.
 * It enables navigation actions 'Declaration or Usage', 'Find usage' and `rename` refactoring.
 * <p/>
 * Declarations (reference target tags) has to has self reference to enable 'find usage' action and `rename` refactoring within them.
 */
public class ConfigXmlPsiReferenceContributor extends PsiReferenceContributor {

    private static final XmlAttributeValuePattern ID_DECLARATION_PATTERN = PatternBuilder.create()
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
                    .insideTag("widget-config", t1 -> t1
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

    private static final XmlIdCache ID_CACHE = new XmlIdCache(ID_DECLARATION_PATTERN);

    public static XmlIdCache getIdCache() {
        return ID_CACHE;
    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        XmlAttributeValuePattern tmpPattern;
        // Self references.
        registrar.registerReferenceProvider(ID_DECLARATION_PATTERN, new ConfigXmlIdProvider());

        // <reference name="Type" type="SS_ModuleType"/>  -->  <domain-object-type name="SS_ModuleType">
        addRef(registrar, TAG_REF, ATTRIBUTE_TYPE, TAG_DOP, ATTRIBUTE_NAME);
        //<domain-object-type extends="SS_ModuleAdd"> --> <domain-object-type name="SS_ModuleAdd">
        addRef(registrar, TAG_DOP, ATTRIBUTE_EXTENDS, TAG_DOP, ATTRIBUTE_NAME);

        //<include-group name="GroupActions" --> <field-group name="GroupActions"> />
        addRef(registrar, TAG_INCLUDE_GROUP, ATTRIBUTE_NAME, TAG_FIELD_GROUP, ATTRIBUTE_NAME);
        //<attachment-type storage="SOPersonFiles" />  --> <attachment-storage name = "SOPersonFiles">
        addRef(registrar, "attachment-type", "storage", "attachment-storage", ATTRIBUTE_NAME);
        addRef(registrar, "permit-role", ATTRIBUTE_NAME, "context-role", ATTRIBUTE_NAME);
        addRef(registrar, "dependent-domain-object", ATTRIBUTE_NAME, TAG_DOP, ATTRIBUTE_NAME);


        tmpPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns1 -> ns1
                        .addIdPath("dynamic-group", ATTRIBUTE_NAME)
                        .addIdPath("static-group", ATTRIBUTE_NAME)
                ).build();
        addRef(registrar, NS_AF5_CONFIG, "permit-group", ATTRIBUTE_NAME, tmpPattern);

        tmpPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns1 -> ns1
                        .addIdPath(TAG_DOP, ATTRIBUTE_NAME)
                        .insideTag(TAG_DOP, dop -> dop
                                .insideTag("attachment-types", at -> at
                                        .addIdPath("attachment-type", ATTRIBUTE_NAME)
                                )
                        )
                ).build();
        addRef(registrar, NS_AF5_CONFIG, "access-matrix", "type", tmpPattern);


        // UI ===============================================================================================
        addRef(registrar, "form", "domain-object-type", TAG_DOP, ATTRIBUTE_NAME);
        addRef(registrar, "form-mapping", "domain-object-type", TAG_DOP, ATTRIBUTE_NAME);
        addRef(registrar, "form-mapping", "form", "form", ATTRIBUTE_NAME);
        addRef(registrar, "collection-view", "collection", "collection", ATTRIBUTE_NAME);
        addRef(registrar, "collection-ref", ATTRIBUTE_NAME, "collection", ATTRIBUTE_NAME);
        addRef(registrar, "action-ref", "name-ref", "action", ATTRIBUTE_NAME, NS_AF5_ACTION);

        XmlAttributeValuePattern widgetPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns1 -> ns1
                        .insideTag("widget-config", c -> c
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
                ).build();
        addRef(registrar, NS_AF5_CONFIG, "widget", "id", true, (refValue) -> {
            XmlTag form = (XmlTag) PsiTreeUtil.findFirstParent(refValue, e -> e instanceof XmlTag && ((XmlTag) e).getLocalName().equals("form"));
            String formName = form.getAttributeValue("name");
            return widgetPattern.withSuperParent(4,  XmlPatterns.xmlTag().withAttributeValue("name", formName));
        });

    }


    private void addRef(PsiReferenceRegistrar registrar,
                        String refTag,
                        String refAtt,
                        String toTag,
                        String toAtt) {
        addRef(registrar, refTag, refAtt, toTag, toAtt, NS_AF5_CONFIG);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar,
                        @NotNull String refTag, @NotNull String refAtt, @NotNull String toTag, @NotNull String toAtt, @NotNull String namespace) {

        XmlAttributeValuePattern pattern = XmlPatterns.xmlAttributeValue().withLocalName(toAtt)
                .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(toTag).withNamespace(namespace));
        addRef(registrar, namespace, refTag, refAtt, pattern);
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar,
                        @NotNull String namespace,
                        @NotNull String refTag,
                        @NotNull String refAtt,
                        @NotNull XmlAttributeValuePattern pattern) {

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(false, (e)->pattern, ID_CACHE));
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar,
                        @NotNull String namespace,
                        @NotNull String refTag,
                        @NotNull String refAtt,
                        @NotNull boolean isInContainingFile,
                        @NotNull Function<XmlAttributeValue, XmlAttributeValuePattern> patternGenerator) {

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(isInContainingFile, patternGenerator, ID_CACHE));
    }
}

