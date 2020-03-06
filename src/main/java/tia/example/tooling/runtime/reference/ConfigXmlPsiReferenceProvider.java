package tia.example.tooling.runtime.reference;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;

/**
 * Provides reference target resolver for xml-attributes found by matchers defined in {@code ConfigXmlPsiReferenceContributor}.
 */
class ConfigXmlPsiReferenceProvider extends PsiReferenceProvider {
    private final String toTag;
    private final String idAttribute;

    ConfigXmlPsiReferenceProvider(String toTag, String idAttribute) {
        this.toTag = toTag;
        this.idAttribute = idAttribute;
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        if (element instanceof XmlAttributeValue) {
            final XmlAttributeValue ref = (XmlAttributeValue) element;
            return new PsiReference[]{new ConfigXmlPsiReference(ref, toTag, idAttribute)};
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
