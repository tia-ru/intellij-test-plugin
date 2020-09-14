package tia.example.tooling.runtime.usage;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;

public class Af5ImplicitUsageProvider implements ImplicitUsageProvider {
    @Override
    public boolean isImplicitUsage(PsiElement element) {
        if (element instanceof PsiClass) {
            return AnnotationUtil.isAnnotated((PsiClass) element, "ru.intertrust.cm.core.dao.api.extension.ExtensionPoint", 0)
                    || AnnotationUtil.isAnnotated((PsiClass) element, "ru.intertrust.cm.core.gui.model.ComponentName", 0)
                    || AnnotationUtil.isAnnotated((PsiClass) element, "ru.intertrust.am.client.api.annotations.ManagedAgent", 0);
        }
        return false;
    }

    @Override
    public boolean isImplicitRead(PsiElement element) {
        return false;
    }

    @Override
    public boolean isImplicitWrite(PsiElement element) {
        return false;
    }
}
