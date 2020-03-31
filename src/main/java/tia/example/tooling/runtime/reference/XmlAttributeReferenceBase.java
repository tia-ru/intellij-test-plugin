package tia.example.tooling.runtime.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
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

public abstract class XmlAttributeReferenceBase<T extends PsiElement> extends PsiReferenceBase<T> {
    protected final String toTag;
    protected final String idAttribute;

    public XmlAttributeReferenceBase(T element, String toTag, String idAttribute) {
        super(element);
        this.toTag = toTag;
        this.idAttribute = idAttribute;
    }

    @Nullable
    @Override
    public XmlAttributeValue resolve() {
        final String ref = getRef();
        if (StringUtil.isEmpty(ref)) return null;

        final XmlTag tag = ConfigXmlUtils.findGlobalTag(ref, toTag, idAttribute, myElement);
        if (tag != null) {
            final XmlAttribute attribute = tag.getAttribute(idAttribute);
            if (attribute != null) return attribute.getValueElement();
        }
        return null;
    }

    protected abstract String getRef();

    @NotNull
    @Override
    public Object[] getVariants() {

        T value = getElement();
        Module module = ModuleUtil.findModuleForPsiElement(value);

        final Set<XmlTag> tags;
        if (module == null){
            //inside external library
            tags = ConfigXmlUtils.getGlobalTags(toTag, value.getProject());
        } else {
            tags = ConfigXmlUtils.getGlobalTags(toTag, module);
        }

        List<LookupElementBuilder> lookupElementBuilders = mapNotNull(tags, tag -> {
            String showName = tag.getAttributeValue(idAttribute);
            PsiFile file = tag.getContainingFile();
            String fileName = file.getName();
            return LookupElementBuilder.create(showName)
                    .withIcon(AllIcons.Nodes.Tag)
                    //.withTailText(" (" + fileName + ')')
                    .withTypeText(" (" + fileName + ')', true)
                    .withPsiElement(tag);
        });
        return lookupElementBuilders.toArray();
    }
}
