package tia.example.tooling.runtime.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

import java.util.List;
import java.util.Set;

import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public class ConfigXmlPsiReference extends PsiReferenceBase<XmlAttributeValue> {

    private final String toTag;
    private final String idAttribute;

    public ConfigXmlPsiReference(@NotNull XmlAttributeValue ref, String toTag, String idAttribute) {
        super(ref);
        this.toTag = toTag;
        this.idAttribute = idAttribute;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        final String ref = getRef();
        final XmlTag tag = ConfigXmlUtils.findGlobalTag(ref, toTag, idAttribute, myElement);
        if (tag != null) {
            final XmlAttribute attribute = tag.getAttribute(idAttribute);
            if (attribute != null) return attribute.getValueElement();
        }
        return null;
    }

    private String getRef() {
        return myElement.getValue();
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        XmlAttributeValue value = getElement();
        Module module = ModuleUtil.findModuleForPsiElement(value);
        final Set<XmlTag> tags = ConfigXmlUtils.getGlobalTags(toTag, module);

        List<LookupElementBuilder> lookupElementBuilders = mapNotNull(tags, tag -> {
            String showName = tag.getAttributeValue(idAttribute);
            PsiFile file = tag.getContainingFile();
            String fileName = file.getName();
            return LookupElementBuilder.create(showName)
                    .withIcon(AllIcons.Nodes.Property)
                    .withTailText(" (" + fileName + ')')
                    .withPsiElement(tag);
        });
        return lookupElementBuilders.toArray();
    }
}
