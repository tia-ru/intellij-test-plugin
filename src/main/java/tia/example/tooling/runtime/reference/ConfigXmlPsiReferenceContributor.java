package tia.example.tooling.runtime.reference;

import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.patterns.XmlTagPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PathListReferenceProvider;
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
            .withNamespace(NS_AF5_CONFIG, ns -> ns
                    .addIdPath(TAG_DOP, ATTR_NAME)
                    .addIdPath(TAG_FIELD_GROUP, ATTR_NAME)
                    .addIdPath("attachment-type", ATTR_NAME)
                    .addIdPath("collection", ATTR_NAME)
                    .addIdPath("context-role", ATTR_NAME)
                    .addIdPath("dynamic-group", ATTR_NAME)
                    .addIdPath("static-group", ATTR_NAME)
                    //UI
                    .addIdPath("form", ATTR_NAME)
                    .insideTag("configuration", cfg -> cfg
                            //Distinguish attachment-storage inside domain-object-type that is reference
                            .addIdPath("attachment-storage", ATTR_NAME)
                    )
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
                    .addIdPath("action", ATTR_NAME)
                    .addIdPath("simple-action", ATTR_NAME)
            )
            .build();

    private static final XmlIdCache ID_CACHE = new XmlIdCache(ID_DECLARATION_PATTERN);

    public static XmlIdCache getIdCache() {
        return ID_CACHE;
    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        registerFileReferences(registrar);

        XmlAttributeValuePattern tmpPattern;
        // Self references.
        registrar.registerReferenceProvider(ID_DECLARATION_PATTERN, new ConfigXmlIdProvider());

        // <reference name="Type" type="SS_ModuleType"/>  -->  <domain-object-type name="SS_ModuleType">
        addRef(registrar, TAG_REF, ATTRIBUTE_TYPE, TAG_DOP, ATTR_NAME);
        //<domain-object-type extends="SS_ModuleAdd"> --> <domain-object-type name="SS_ModuleAdd">
        addRef(registrar, TAG_DOP, ATTRIBUTE_EXTENDS, TAG_DOP, ATTR_NAME);

        //<include-group name="GroupActions" --> <field-group name="GroupActions"> />
        addRef(registrar, TAG_INCLUDE_GROUP, ATTR_NAME, TAG_FIELD_GROUP, ATTR_NAME);

        //<attachment-type storage="SOPersonFiles" />  --> <attachment-storage name = "SOPersonFiles">
        //
        //<domain-object-type>
        //    <attachment-storage name = "SOPersonFiles"/>
        //</domain-object-type>
        //-->
        // <configuration>
        //     <attachment-storage name = "SOPersonFiles">
        // </configuration>

        XmlAttributeValuePattern attachmentStorageIdPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns -> ns
                        .insideTag("configuration", c -> c
                                .addIdPath("attachment-storage", ATTR_NAME)
                        )
                ).build();
        XmlAttributeValuePattern attachmentStorageRefPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns -> ns
                        .addIdPath("attachment-type", "storage")
                        .insideTag(TAG_DOP, c -> c
                                .addIdPath("attachment-storage", ATTR_NAME)
                        )
                ).build();
        addRef(registrar, attachmentStorageRefPattern, (e) -> attachmentStorageIdPattern, false);

        tmpPattern = XmlPatterns.xmlAttributeValue().withLocalName(ATTR_NAME)
                .withSuperParent(2, XmlPatterns.xmlTag()
                        .withLocalName(TAG_DOP)
                        .withAttributeValue("template", "true")
                        .withNamespace(NS_AF5_CONFIG));
        addRef(registrar, NS_AF5_CONFIG, "attachment-type", "template", tmpPattern);

        addRef(registrar, "permit-role", ATTR_NAME, "context-role", ATTR_NAME);
        addRef(registrar, "dependent-domain-object", ATTR_NAME, TAG_DOP, ATTR_NAME);


        tmpPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns -> ns
                        .addIdPath("dynamic-group", ATTR_NAME)
                        .addIdPath("static-group", ATTR_NAME)
                ).build();
        addRef(registrar, NS_AF5_CONFIG, "permit-group", ATTR_NAME, tmpPattern);

        tmpPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns -> ns
                        .addIdPath(TAG_DOP, ATTR_NAME)
                        .insideTag(TAG_DOP, dop -> dop
                                .insideTag("attachment-types", at -> at
                                        .addIdPath("attachment-type", ATTR_NAME)
                                )
                        )
                ).build();
        addRef(registrar, NS_AF5_CONFIG, "access-matrix", "type", tmpPattern);


        // UI ===============================================================================================
        addRef(registrar, "form", "domain-object-type", TAG_DOP, ATTR_NAME);
        addRef(registrar, "form-mapping", "domain-object-type", TAG_DOP, ATTR_NAME);
        addRef(registrar, "form-mapping", "form", "form", ATTR_NAME);

        //<act:form-mapping form="A">  -> <form name="A">
        //<form-mapping form="A">  -> <form name="A">
        XmlAttributeValuePattern formMappingRefPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns -> ns
                        .addIdPath("form-mapping", "form")
                ).withNamespace(NS_AF5_ACTION, ns -> ns
                        .addIdPath("form-mapping", "form")
                ).build();
        XmlAttributeValuePattern formMappingIdPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns -> ns
                        .addIdPath("form", ATTR_NAME)
                ).build();
        addRef(registrar, formMappingRefPattern, (e) -> formMappingIdPattern, false);

        addRef(registrar, "collection-view", "collection", "collection", ATTR_NAME);
        addRef(registrar, "collection-ref", ATTR_NAME, "collection", ATTR_NAME);
        addRef(registrar, "domain-object-surfer", "domain-object-type-to-create", TAG_DOP, ATTR_NAME);

        tmpPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_ACTION, ns -> ns
                        .addIdPath("action", ATTR_NAME)
                        .addIdPath("simple-action", ATTR_NAME)
                ).build();
        addRef(registrar, NS_AF5_ACTION, "action-ref", "name-ref", tmpPattern);

        XmlAttributeValuePattern widgetPattern = PatternBuilder.create()
                .withNamespace(NS_AF5_CONFIG, ns -> ns
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

        addRef(registrar, NS_AF5_CONFIG, "widget", "id", (refValue) -> {
            XmlTag form = (XmlTag) PsiTreeUtil.findFirstParent(refValue, e -> e instanceof XmlTag && ((XmlTag) e).getLocalName().equals("form"));
            String formName = form.getAttributeValue("name");
            return widgetPattern.withSuperParent(4, XmlPatterns.xmlTag().withAttributeValue("name", formName));
        }, true);

    }

    private void registerFileReferences(PsiReferenceRegistrar registrar) {
        //final PsiReferenceProvider filePathReferenceProvider = new FilePathReferenceProvider();
        final PsiReferenceProvider filePathReferenceProvider = new PathListReferenceProvider(){
            @Override
            protected boolean disableNonSlashedPaths() {
                return false;
            }
            @NotNull
            protected PsiReference[] createReferences(@NotNull PsiElement element, String s, int offset, final boolean soft) {
                int contentOffset = StringUtil.findFirst(s, CharFilter.NOT_WHITESPACE_FILTER);
                if (contentOffset >= 0) {
                    offset += contentOffset;
                }
                FileReferenceSet fileReferenceSet = new ConfigXmlFileReferenceSet(s.trim(), element, offset, this, false, soft);
                return fileReferenceSet.getAllReferences();
            }
        };

        XmlTagPattern xmlTextPattern = XmlPatterns.xmlTag().withLocalName("configuration-path").withNamespace(NS_AF5_MODULE);
        registrar.registerReferenceProvider(xmlTextPattern, filePathReferenceProvider);
    }

    private void addRef(PsiReferenceRegistrar registrar, String refTag, String refAtt, String toTag, String toAtt) {
        addRef(registrar, NS_AF5_CONFIG, refTag, refAtt, toTag, toAtt);
    }

    private void addRef(
            @NotNull PsiReferenceRegistrar registrar,
            @NotNull String namespace, @NotNull String refTag, @NotNull String refAtt,
            @NotNull String toTag, @NotNull String toAtt) {

        XmlAttributeValuePattern pattern = XmlPatterns.xmlAttributeValue().withLocalName(toAtt)
                .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(toTag).withNamespace(namespace));
        addRef(registrar, namespace, refTag, refAtt, pattern);
    }

    private void addRef(
            @NotNull PsiReferenceRegistrar registrar,
            @NotNull String namespace, @NotNull String refTag, @NotNull String refAtt,
            @NotNull XmlAttributeValuePattern idPattern) {

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(false, (e) -> idPattern, ID_CACHE));
    }

    private void addRef(
            @NotNull PsiReferenceRegistrar registrar,
            @NotNull String namespace,
            @NotNull String refTag,
            @NotNull String refAtt, @NotNull Function<XmlAttributeValue, XmlAttributeValuePattern> idPatternGenerator,
            @NotNull boolean isInContainingFile) {

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(refAtt)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(refTag).withNamespace(namespace)),
                new ConfigXmlPsiReferenceProvider(isInContainingFile, idPatternGenerator, ID_CACHE));
    }

    private void addRef(@NotNull PsiReferenceRegistrar registrar, @NotNull XmlAttributeValuePattern refPattern,
                        @NotNull Function<XmlAttributeValue, XmlAttributeValuePattern> idPatternGenerator,
                        @NotNull boolean isInContainingFile) {

        registrar.registerReferenceProvider(
                refPattern,
                new ConfigXmlPsiReferenceProvider(isInContainingFile, idPatternGenerator, ID_CACHE));
    }
}

