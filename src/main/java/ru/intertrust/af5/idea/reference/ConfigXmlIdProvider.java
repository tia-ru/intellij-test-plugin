package ru.intertrust.af5.idea.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.AttributeValueSelfReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.XmlExtension;
import org.jetbrains.annotations.NotNull;

/**
 * Provides reference target resolver for xml-attributes
 * that is called by {@code ConfigXmlPsiReferenceContributor}.
 */
class ConfigXmlIdProvider extends PsiReferenceProvider {

    ConfigXmlIdProvider() {
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        if (!(element instanceof XmlAttributeValue)) {
            return PsiReference.EMPTY_ARRAY;
        }
        final XmlExtension extension = XmlExtension.getExtensionByElement(element);
        if (extension != null && extension.hasDynamicComponents(element)) {
            return PsiReference.EMPTY_ARRAY;
        }

        final PsiElement parentElement = element.getParent();
        if (!(parentElement instanceof XmlAttribute)){
            return PsiReference.EMPTY_ARRAY;
        }

        /*XmlAttribute xmlAttribute = (XmlAttribute) parentElement;
        final String name = xmlAttribute.getName();
        final String ns = xmlAttribute.getParent().getNamespace();*/

        return new PsiReference[]{
                new AttributeValueSelfReference(element)
                };
    }


}
