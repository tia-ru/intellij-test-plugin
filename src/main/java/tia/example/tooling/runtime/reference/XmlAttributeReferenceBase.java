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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public abstract class XmlAttributeReferenceBase<T extends PsiElement> extends PsiReferenceBase<T> {

    protected final List<ConfigXmlAttribute> pathsToAttributes;

    public XmlAttributeReferenceBase(T element, String toTag, String idAttribute) {
        super(element);
        this.pathsToAttributes = new ArrayList<>(1);
        this.pathsToAttributes.add(new ConfigXmlAttribute(toTag, idAttribute));
    }

    public XmlAttributeReferenceBase(T element, List<ConfigXmlAttribute> pathsToAttributes){
        super(element);
        this.pathsToAttributes = new ArrayList<>(pathsToAttributes);
    }


    @Nullable
    @Override
    public XmlAttributeValue resolve() {
        final String ref = getRef();
        if (StringUtil.isEmpty(ref)) return null;

        for (ConfigXmlAttribute path : pathsToAttributes) {
            final XmlTag tag = ConfigXmlUtils.getTag(ref, path.getToTag(), path.getIdAttribute(), myElement);
            if (tag != null) {
                final XmlAttribute attribute = tag.getAttribute(path.getIdAttribute());
                if (attribute != null) return attribute.getValueElement();
            }
        }
        return null;
    }

    protected abstract String getRef();

    @NotNull
    @Override
    public Object[] getVariants() {

        T value = getElement();
        Module module = ModuleUtil.findModuleForPsiElement(value);

        List<LookupElementBuilder> lookupElementBuilders = new ArrayList<>(512);
        for (ConfigXmlAttribute attributeRef : pathsToAttributes) {
            final Set<XmlTag> tags;
            if (module == null){
                //inside external library
                tags = ConfigXmlUtils.findTags(attributeRef.getToTag(), value.getProject());
            } else {
                tags = ConfigXmlUtils.findTags(attributeRef.getToTag(), module);
            }

            List<LookupElementBuilder> leb = mapNotNull(tags, tag -> {
                String showName = tag.getAttributeValue(attributeRef.getIdAttribute());
                if (showName == null) return null;
                PsiFile file = tag.getContainingFile();
                String fileName = file.getName();
                return LookupElementBuilder.create(showName)
                        .withIcon(AllIcons.Nodes.Tag)
                        //.withTailText(" (" + fileName + ')')
                        .withTypeText(" (" + fileName + ')', true)
                        .withPsiElement(tag);
            });
            lookupElementBuilders.addAll(leb);
        }


        return lookupElementBuilders.toArray();
    }

}
