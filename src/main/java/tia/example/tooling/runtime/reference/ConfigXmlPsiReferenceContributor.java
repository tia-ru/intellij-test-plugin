package tia.example.tooling.runtime.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_EXTENDS;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_INCLUDE_GROUP;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_NAME;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_TYPE;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.NS_AF5_CONFIG;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_DOP;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_FIELD_GROUP;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_REF;

public class ConfigXmlPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        // <reference name="Type" type="SS_ModuleType"/>  -->  <domain-object-type name="SS_ModuleType">
        //<domain-object-type extends="SS_ModuleAdd"> --> <domain-object-type name="SS_ModuleAdd">
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(ATTRIBUTE_TYPE, ATTRIBUTE_EXTENDS)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(TAG_REF, TAG_DOP)
                                .withNamespace(NS_AF5_CONFIG))                 ,
                new ConfigXmlPsiReferenceProvider(TAG_DOP, ATTRIBUTE_NAME));


        //<include-group name="GroupActions" --> <field-group name="GroupActions"> />
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(ATTRIBUTE_NAME)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(ATTRIBUTE_INCLUDE_GROUP).withNamespace(NS_AF5_CONFIG))                 ,
                new ConfigXmlPsiReferenceProvider(TAG_FIELD_GROUP, ATTRIBUTE_NAME));

        // Self references. It enables 'find usage' action and `rename` refactoring
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(ATTRIBUTE_NAME)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(TAG_DOP).withNamespace(NS_AF5_CONFIG))                 ,
                new ConfigXmlPsiReferenceProvider(TAG_DOP, ATTRIBUTE_NAME));

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(ATTRIBUTE_NAME)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(TAG_FIELD_GROUP).withNamespace(NS_AF5_CONFIG))                 ,
                new ConfigXmlPsiReferenceProvider(TAG_FIELD_GROUP, ATTRIBUTE_NAME));

        //XmlUtil.registerXmlAttributeValueReferenceProvider(registrar, new String[]{MuleConfigUtils.REF_ATTRIBUTE}, )
    }

}


