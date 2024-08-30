package ru.intertrust.af5.idea.reference;

import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

import static ru.intertrust.af5.idea.util.ConfigXmlUtils.NS_AF5_CONFIG;

/**
   Search for reference target
 */
class JavaPsiReference extends XmlReferenceBase<PsiLiteralExpression> {

    JavaPsiReference(@NotNull PsiLiteralExpression ref, String toTag, String idAttribute, XmlIdCache cache) {
        super(ref, false, NS_AF5_CONFIG, toTag, idAttribute, cache);
    }

    protected String getRef() {
        Object ref = myElement.getValue();
        if (ref instanceof String){
            return (String) ref;
        }
        return null;
    }
}
