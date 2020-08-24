package tia.example.tooling.runtime.reference;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Provides reference target resolver for xml-attributes found by matchers defined in {@code ConfigXmlPsiReferenceContributor}.
 */
class ConfigXmlPsiReferenceProvider extends PsiReferenceProvider {
    private static final Set<String> IGNORE_LIST = new HashSet<>(Arrays.asList("*"));
    private final boolean isInContainingFile;
    Function<XmlAttributeValue, XmlAttributeValuePattern> patternGenerator;
    private final XmlIdCache cache;

    ConfigXmlPsiReferenceProvider(boolean isInContainingFile, Function<XmlAttributeValue, XmlAttributeValuePattern> patternGenerator, XmlIdCache cache) {
        this.isInContainingFile = isInContainingFile;
        this.patternGenerator = patternGenerator;
        this.cache = cache;
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        if (element instanceof XmlAttributeValue) {
            final XmlAttributeValue ref = (XmlAttributeValue) element;
            if (!IGNORE_LIST.contains(ref.getValue())) {
                return new PsiReference[] { new ConfigXmlPsiReference(ref, isInContainingFile, patternGenerator, cache) };
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
