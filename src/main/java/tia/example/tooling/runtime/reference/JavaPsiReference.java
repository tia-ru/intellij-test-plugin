package tia.example.tooling.runtime.reference;

import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

import static tia.example.tooling.runtime.util.ConfigXmlUtils.NS_AF5_CONFIG;

/**
   Search for reference target
 */
class JavaPsiReference extends XmlAttributeReferenceBase<PsiLiteralExpression> {

    JavaPsiReference(@NotNull PsiLiteralExpression ref, String toTag, String idAttribute, XmlIdCache cache) {
        super(ref, NS_AF5_CONFIG, toTag, idAttribute, cache);
    }

    protected String getRef() {
        Object ref = myElement.getValue();
        if (ref instanceof String){
            return (String) ref;
        }
        return null;
    }
}
