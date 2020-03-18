package tia.example.tooling.runtime.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class JavaPsiReferenceProvider extends PsiReferenceProvider {

    private final String toTag;
    private final String idAttribute;

    JavaPsiReferenceProvider(String toTag, String idAttribute) {
        this.toTag = toTag;
        this.idAttribute = idAttribute;
    }
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

        if (element instanceof PsiLiteralExpression) {
            PsiLiteralExpression ref = (PsiLiteralExpression) element;
            return new PsiReference[]{new JavaPsiReference(ref, toTag, idAttribute)};
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
