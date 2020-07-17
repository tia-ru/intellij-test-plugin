package tia.example.tooling.runtime.reference;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class XmlIdCache {

    private final UserDataCache<CachedValue<Set<XmlTag>>, XmlFile, Void> cache
        = new UserDataCache<CachedValue<Set<XmlTag>>, XmlFile, Void>("tia.cache.xml.id") {
        @Override
        protected CachedValue<Set<XmlTag>> compute(final XmlFile xmlFile, Void p) {
            CachedValuesManager manager = CachedValuesManager.getManager(xmlFile.getProject());
            return manager.createCachedValue(
                    () -> CachedValueProvider.Result.create(doCompute(xmlFile), xmlFile), false);
        }
    };



    private final ElementPattern<? extends PsiElement> attributeValueFilter;

    public XmlIdCache(ElementPattern<? extends PsiElement> attributeValueFilter){
        this.attributeValueFilter = attributeValueFilter;
    }

    private Set<XmlTag> doCompute(XmlFile xmlFile) {
        Set<XmlTag> result = new HashSet<>();
        xmlFile.accept(new XmlRecursiveElementVisitor(true) {
            @Override
            public void visitXmlAttributeValue(XmlAttributeValue value) {
                if (attributeValueFilter.accepts(value)){
                    result.add((XmlTag) value.getParent().getParent());
                }
                super.visitXmlAttributeValue(value);
            }
        });
        return result;
    }

    public void put(Set<XmlTag> value, XmlFile owner) {
        CachedValuesManager manager = CachedValuesManager.getManager(owner.getProject());
        cache.put(owner, manager.createCachedValue(() -> CachedValueProvider.Result.create(value)));
    }

    private Set<XmlTag> get(XmlFile owner){
        return cache.get(owner, null).getValue();
    }

    public XmlTag getTag(String id, XmlIdPath tagPath, PsiElement refElement) {

        final Project project = refElement.getProject();
        //Search in the current file at first else we search globally
        final PsiFile psiFile = refElement.getContainingFile();
        if (ConfigXmlUtils.isAF5ConfigFile(psiFile)) {
            XmlFile xmlFile = (XmlFile) psiFile;
            XmlTag xmlTag = getTagInFile(id, tagPath, xmlFile);
            if (xmlTag != null) {
                return xmlTag;
            }
        }

        Module module = ModuleUtil.findModuleForPsiElement(refElement);
        GlobalSearchScope scope;
        if (module == null) {
            //refElement is inside external library
            scope = GlobalSearchScope.allScope(project);
        } else {
            scope = GlobalSearchScope.moduleRuntimeScope(module, false);//getModuleWithDependenciesAndLibrariesScope
        }

        XmlTag xmlTag = getTagInScope(id, tagPath, scope);
        return xmlTag;
    }

    private XmlTag getTagInScope(String id, XmlIdPath tagPath, GlobalSearchScope searchScope) {

        Project project = searchScope.getProject();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);

        for (VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);
            if (ConfigXmlUtils.isAF5ConfigFile(psiFile)) {
                XmlFile xmlFile = (XmlFile) psiFile;
                XmlTag tag = getTagInFile(id, tagPath, xmlFile);
                if (tag != null) return tag;
            }
        }
        return null;
    }

    private XmlTag getTagInFile(String id, XmlIdPath tagPath, XmlFile xmlFile) {

        String[] split = tagPath.getToTag().split("/");
        Set<XmlTag> values = get(xmlFile);
        for (XmlTag tag : values) {
            String attValue = tag.getAttributeValue(tagPath.getIdAttribute());
            if (id.equals(attValue) && isMatchPath(tag, tagPath.getNamespace(), split)) return tag;
        }
        return null;
    }

    public Set<XmlTag> findTags(XmlIdPath tagPath, @Nonnull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.XML);
        Set<XmlTag> tags = findTagsInScope(tagPath, scope);
        return tags;

    }

    public Set<XmlTag> findTags(XmlIdPath tagPath, @Nonnull Module module) {

        GlobalSearchScope scope = GlobalSearchScope.moduleRuntimeScope(module, false);
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.XML);
        Set<XmlTag> tags = findTagsInScope(tagPath, scope);
        return tags;

    }
    private Set<XmlTag> findTagsInScope(XmlIdPath tagPath, GlobalSearchScope searchScope) {

        String[] split = tagPath.getToTag().split("/");

        Project project = searchScope.getProject();
        Set<XmlTag> result = new HashSet<>(512);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);
            if (ConfigXmlUtils.isAF5ConfigFile(psiFile)) {
                XmlFile xmlFile = (XmlFile) psiFile;
                Set<XmlTag> tags = get(xmlFile);
                for (XmlTag tag : tags) {
                    if (isMatchPath(tag, tagPath.getNamespace(), split)){
                        result.add(tag);
                    }
                }
            }
        }
        return result;
    }

    private boolean isMatchPath(XmlTag tag, String ns, String[] split) {
        XmlTag curTag = tag;
        int i;
        for(i = split.length - 1; i >= 0 && curTag != null; i--){
            if (! (curTag.getLocalName().equals(split[i]) && curTag.getNamespace().equals(ns))) break;
            curTag = (XmlTag) curTag.getParent();
        }
        if (i < 0) {
            return true;
        }
        return false;
    }
}
