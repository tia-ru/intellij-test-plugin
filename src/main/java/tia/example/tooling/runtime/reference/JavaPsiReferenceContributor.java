package tia.example.tooling.runtime.reference;

import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

import static tia.example.tooling.runtime.util.ConfigXmlUtils.ATTRIBUTE_NAME;
import static tia.example.tooling.runtime.util.ConfigXmlUtils.TAG_DOP;

public class JavaPsiReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        XmlIdCache cache = ConfigXmlPsiReferenceContributor.getIdCache();

        PsiJavaElementPattern.Capture<PsiLiteralExpression> filter = PsiJavaPatterns.literalExpression()
                .annotationParam("ru.intertrust.cm.core.dao.api.extension.ExtensionPoint", "filter");

        registrar.registerReferenceProvider(filter, new JavaPsiReferenceProvider(TAG_DOP, ATTRIBUTE_NAME, cache));
    }
}
