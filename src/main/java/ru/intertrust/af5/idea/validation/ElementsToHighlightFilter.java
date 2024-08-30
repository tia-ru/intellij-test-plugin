package ru.intertrust.af5.idea.validation;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import ru.intertrust.af5.idea.util.ConfigXmlUtils;

/**
 *
 */
public class ElementsToHighlightFilter implements Condition<PsiElement> {
    @Override
    public boolean value(PsiElement element) {
        return (element instanceof XmlFile && ConfigXmlUtils.isAF5ConfigFile((XmlFile) element));
    }
}
