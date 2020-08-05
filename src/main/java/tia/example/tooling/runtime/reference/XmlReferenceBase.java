package tia.example.tooling.runtime.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.testFramework.LightVirtualFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public abstract class XmlReferenceBase<T extends PsiElement> extends PsiReferenceBase<T> {

    private final XmlIdCache cache;
    private final boolean isInContainingFile;
    private final Function<T, XmlAttributeValuePattern> patternGenerator;

    public XmlReferenceBase(T element,
                            boolean isInContainingFile,
                            String namespace,
                            String toTag,
                            String idAttribute,
                            XmlIdCache cache) {

        super(element);
        this.isInContainingFile = isInContainingFile;
        this.cache = cache;
        XmlAttributeValuePattern pattern = XmlPatterns.xmlAttributeValue().withLocalName(idAttribute)
                .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(toTag).withNamespace(namespace));
        patternGenerator = (T e) -> pattern;
    }

    public XmlReferenceBase(T element,
                            boolean isInContainingFile,
                            Function<T, XmlAttributeValuePattern> patternGenerator,
                            XmlIdCache cache) {

        super(element);
        this.isInContainingFile = isInContainingFile;
        this.patternGenerator = patternGenerator;
        this.cache = cache;
    }


    @Nullable
    @Override
    public XmlAttributeValue resolve() {
        final String ref = getRef();
        if (StringUtil.isEmpty(ref)) return null;

        T element = getElement();
        XmlAttributeValuePattern pattern = patternGenerator.apply(element);

        GlobalSearchScope scope = createScope(element);
        final XmlAttributeValue value = cache.getIdDeclaration(ref, pattern, scope);
        return value;
    }

    protected abstract String getRef();

    @NotNull
    @Override
    public LookupElementBuilder[] getVariants() {

        T element = getElement();
        List<LookupElementBuilder> lookupElementBuilders = new ArrayList<>(512);
        XmlAttributeValuePattern pattern = patternGenerator.apply(element);
        final Set<XmlAttributeValue> values;
        GlobalSearchScope scope = createScope(element);
        values = cache.findIdDeclarations(pattern, scope);

        List<LookupElementBuilder> leb = mapNotNull(values, value -> {
            String showName = value.getValue();
            if (showName == null) return null;
            PsiFile file = value.getContainingFile();
            String fileName = file.getName();
            return LookupElementBuilder.create(showName)
                    .withIcon(AllIcons.Nodes.Tag)
                    //.withTailText(" (" + fileName + ')')
                    .withTypeText(" (" + fileName + ')', true)
                    .withPsiElement(value);
        });
        lookupElementBuilders.addAll(leb);


        LookupElementBuilder[] arr = new LookupElementBuilder[lookupElementBuilders.size()];
        return lookupElementBuilders.toArray(arr);
    }

    @NotNull
    private GlobalSearchScope createScope(T element) {
        GlobalSearchScope scope;

        if (isInContainingFile) {
            //Workaround element.getContainingFile().getVirtualFile() returns null
            VirtualFile virtualFile = element.getContainingFile().getViewProvider().getVirtualFile();
            if (virtualFile instanceof LightVirtualFile) {
                virtualFile = ((LightVirtualFile) virtualFile).getOriginalFile();
            }
            scope = GlobalSearchScope.fileScope(element.getProject(), virtualFile);
            return scope;
        }

        Module module = ModuleUtil.findModuleForPsiElement(element);
        if (module != null) {
            scope = GlobalSearchScope.moduleRuntimeScope(module, false);
        } else {
            //ref is inside external library
            scope = GlobalSearchScope.allScope(element.getProject());
        }
        return scope;
    }
}
