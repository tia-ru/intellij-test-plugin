package tia.example.tooling.runtime.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.util.Comparing;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public abstract class XmlReferenceBase<T extends PsiElement> extends PsiReferenceBase<T> {

    private final XmlIdCache cache;
    private final boolean isInContainingFile;
    private final Function<T, XmlAttributeValuePattern> patternGenerator;

    public XmlReferenceBase(T element, boolean isInContainingFile, String namespace, String toTag, String idAttribute, XmlIdCache cache) {
        super(element);
        this.isInContainingFile = isInContainingFile;
        this.cache = cache;
        XmlAttributeValuePattern pattern = XmlPatterns.xmlAttributeValue().withLocalName(idAttribute)
                .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(toTag).withNamespace(namespace));
        patternGenerator = (T e) -> pattern;
    }
    public XmlReferenceBase(T element, boolean isInContainingFile,
                            Function<T, XmlAttributeValuePattern> patternGenerator, XmlIdCache cache) {
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
            //scope = new FileScope(element.getProject(), virtualFile, null);
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

    private static class FileScope extends GlobalSearchScope implements Iterable<VirtualFile> {
        private final VirtualFile myVirtualFile; // files can be out of project roots
        @Nullable private final String myDisplayName;
        private final Module myModule;

        private FileScope(@NotNull Project project, @Nullable VirtualFile virtualFile, @Nullable String displayName) {
            super(project);
            if (virtualFile instanceof LightVirtualFile) {
                myVirtualFile = ((LightVirtualFile) virtualFile).getOriginalFile();
            } else {
                myVirtualFile = virtualFile;
            }
            myDisplayName = displayName;
            final FileIndexFacade facade = FileIndexFacade.getInstance(project);
            myModule = virtualFile == null || project.isDefault() ? null : facade.getModuleForFile(virtualFile);
            //mySearchOutsideContent = project.isDefault() || virtualFile != null && myModule == null && !facade.isInLibraryClasses(virtualFile) && !facade.isInLibrarySource(virtualFile);
        }

        @Override
        public boolean contains(@NotNull VirtualFile file) {
            return Comparing.equal(myVirtualFile.getPath(), file.getPath());
        }

        @Override
        public boolean isSearchInModuleContent(@NotNull Module aModule) {
            return aModule == myModule;
        }

        @Override
        public boolean isSearchInLibraries() {
            return myModule == null;
        }

        @Override
        public String toString() {
            return "File :"+myVirtualFile;
        }

        @NotNull
        @Override
        public Iterator<VirtualFile> iterator() {
            return Collections.singletonList(myVirtualFile).iterator();
        }


        @NotNull
        @Override
        public String getDisplayName() {
            return myDisplayName != null ? myDisplayName : super.getDisplayName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || o.getClass() != getClass()) return false;
            FileScope files = (FileScope)o;
            return Objects.equals(myVirtualFile, files.myVirtualFile) &&
                   Objects.equals(myDisplayName, files.myDisplayName) &&
                   Objects.equals(myModule, files.myModule);
        }

        @Override
        protected int calcHashCode() {
            return Objects.hash(myVirtualFile, myModule, myDisplayName);
        }
    }
}
