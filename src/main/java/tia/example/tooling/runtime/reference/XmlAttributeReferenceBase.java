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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public abstract class XmlAttributeReferenceBase<T extends PsiElement> extends PsiReferenceBase<T> {

    protected final List<XmlIdPath> pathsToAttributes;
    private final XmlIdCache cache;

    public XmlAttributeReferenceBase(T element, String namespace, String toTag, String idAttribute, XmlIdCache cache) {
        super(element);
        this.cache = cache;
        this.pathsToAttributes = new ArrayList<>(1);
        this.pathsToAttributes.add(new XmlIdPath(namespace, toTag, idAttribute));
    }

    public XmlAttributeReferenceBase(T element, List<XmlIdPath> pathsToAttributes, XmlIdCache cache){
        super(element);
        this.pathsToAttributes = new ArrayList<>(pathsToAttributes);
        this.cache = cache;
    }


    @Nullable
    @Override
    public XmlAttributeValue resolve() {
        final String ref = getRef();
        if (StringUtil.isEmpty(ref)) return null;

        for (XmlIdPath path : pathsToAttributes) {

            //final XmlTag tag = ConfigXmlUtils.getTag(ref, path.getToTag(), path.getIdAttribute(), myElement);
            final XmlTag tag = cache.getTag(ref, path, myElement);
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
    public LookupElementBuilder[] getVariants() {

        T value = getElement();
        Module module = ModuleUtil.findModuleForPsiElement(value);

        List<LookupElementBuilder> lookupElementBuilders = new ArrayList<>(512);
        for (XmlIdPath attributePath : pathsToAttributes) {
            final Set<XmlTag> tags;
            if (module == null){
                //ref is inside external library
                //tags = ConfigXmlUtils.findTags(attributePath.getToTag(), value.getProject());
                tags = cache.findTags(attributePath, value.getProject());
            } else {
                //tags = ConfigXmlUtils.findTags(attributePath.getToTag(), module);
                tags = cache.findTags(attributePath, module);
            }

            List<LookupElementBuilder> leb = mapNotNull(tags, tag -> {
                String showName = tag.getAttributeValue(attributePath.getIdAttribute());
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

        LookupElementBuilder[] arr = new LookupElementBuilder[lookupElementBuilders.size()];
        return lookupElementBuilders.toArray(arr);
    }


}
