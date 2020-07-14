package tia.example.tooling.runtime.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides reference target resolver for xml-attributes found by matchers defined in {@code ConfigXmlPsiReferenceContributor}.
 */
class ConfigXmlPsiReferenceProvider extends PsiReferenceProvider {

    private final List<ConfigXmlAttribute> pathsToAttributes;

/*    ConfigXmlPsiReferenceProvider(String toTag, String idAttribute) {
        this.toAttributes = new ArrayList<>(1);
        this.toAttributes.add(new XmlAttributeReferenceBase.XmlAttributeRef(toTag, idAttribute));
    }*/

    ConfigXmlPsiReferenceProvider(List<ConfigXmlAttribute> pathsToAttributes) {
        this.pathsToAttributes = pathsToAttributes;
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        if (element instanceof XmlAttributeValue) {
            final XmlAttributeValue ref = (XmlAttributeValue) element;
            return new PsiReference[]{
                    new ConfigXmlPsiReference(ref, pathsToAttributes)
                    };
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
