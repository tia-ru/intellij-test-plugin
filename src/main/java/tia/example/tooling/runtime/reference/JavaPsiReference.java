package tia.example.tooling.runtime.reference;

import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
   Search for reference target
 */
class JavaPsiReference extends XmlAttributeReferenceBase<PsiLiteralExpression> {

    JavaPsiReference(@NotNull PsiLiteralExpression ref, String toTag, String idAttribute) {
        super(ref, toTag, idAttribute);
    }

    protected String getRef() {
        Object ref = myElement.getValue();
        if (ref instanceof String){
            return (String) ref;
        }
        return null;
    }
}
