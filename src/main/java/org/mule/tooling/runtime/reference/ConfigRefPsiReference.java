package org.mule.tooling.runtime.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mule.tooling.runtime.util.MuleConfigUtils;

import java.util.List;

import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public class ConfigRefPsiReference extends PsiReferenceBase<XmlAttributeValue> {
    public ConfigRefPsiReference(@NotNull XmlAttributeValue element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        final String value = getElementValue();
        final XmlTag globalElement = MuleConfigUtils.findGlobalElement(myElement, value);
        if (globalElement != null) {
            final XmlAttribute attribute = globalElement.getAttribute(MuleConfigUtils.NAME_ATTRIBUTE);
            if (attribute != null) return attribute.getValueElement();
        }
        return null;
    }

    private String getElementValue() {
        return myElement.getValue();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        final List<XmlTag> flow = MuleConfigUtils.getGlobalElements(getElement().getProject());
        return mapNotNull(flow,
                (Function<XmlTag, Object>) domElement -> domElement.getAttributeValue(MuleConfigUtils.NAME_ATTRIBUTE)).toArray();
    }
}
