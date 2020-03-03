package org.mule.tooling.runtime.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.mule.tooling.runtime.util.MuleConfigUtils;

public class MuleConfigurationReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        /*registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(MuleConfigUtils.REF_ATTRIBUTE),
                new ConfigRefProvider());*/

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(MuleConfigUtils.REF_ATTRIBUTE)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(MuleConfigUtils.REF_TAG).withNamespace(MuleConfigUtils.AF5_CONFIG_NAMESPACE))                 ,
                new ConfigRefProvider());

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue(MuleConfigUtils.EXTENDS_ATTRIBUTE)
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(MuleConfigUtils.DOP_TAG).withNamespace(MuleConfigUtils.AF5_CONFIG_NAMESPACE))                 ,
                new ConfigRefProvider());

        //XmlUtil.registerXmlAttributeValueReferenceProvider(registrar, new String[]{MuleConfigUtils.REF_ATTRIBUTE}, )
    }

    private static class ConfigRefProvider extends PsiReferenceProvider {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            if (element instanceof XmlAttributeValue) {
                final XmlAttributeValue value = (XmlAttributeValue) element;
                return new PsiReference[]{new ConfigRefPsiReference(value)};
                //return new PsiReference[]{new XmlAttributeReference((XmlAttributeImpl) value.getParent())};
            }
            return PsiReference.EMPTY_ARRAY;
        }
    }

}


