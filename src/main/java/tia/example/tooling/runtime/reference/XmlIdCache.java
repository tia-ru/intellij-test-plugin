package tia.example.tooling.runtime.reference;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
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
import tia.example.tooling.runtime.util.ConfigXmlUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class XmlIdCache {

    private final XmlAttributeValuePattern idDeclarationsPattern;

    private final UserDataCache<CachedValue<Set<XmlAttributeValue>>, XmlFile, Void> cache
            = new UserDataCache<CachedValue<Set<XmlAttributeValue>>, XmlFile, Void>("tia.cache.xml.id") {
        @Override
        protected CachedValue<Set<XmlAttributeValue>> compute(final XmlFile xmlFile, Void p) {
            CachedValuesManager manager = CachedValuesManager.getManager(xmlFile.getProject());
            return manager.createCachedValue(
                    () -> CachedValueProvider.Result.create(doCompute(xmlFile), xmlFile), false);
        }
    };


    public XmlIdCache(XmlAttributeValuePattern idDeclarationsPattern) {
        this.idDeclarationsPattern = idDeclarationsPattern;
    }

    public void put(Set<XmlAttributeValue> value, XmlFile owner) {
        CachedValuesManager manager = CachedValuesManager.getManager(owner.getProject());
        cache.put(owner, manager.createCachedValue(() -> CachedValueProvider.Result.create(value)));
    }

    public XmlAttributeValue getIdDeclaration(String id, ElementPattern<XmlAttributeValue> pattern, GlobalSearchScope searchScope) {

        Project project = searchScope.getProject();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(XmlFileType.INSTANCE, searchScope);

        for (VirtualFile file : files) {
            if (ConfigXmlUtils.isAF5ConfigFile(file)) {
                final PsiFile psiFile = psiManager.findFile(file);
                XmlFile xmlFile = (XmlFile) psiFile;
                XmlAttributeValue value = getIdDeclarationInFile(id, pattern, xmlFile);
                if (value != null) return value;
            }
        }
        return null;
    }

    public Set<XmlAttributeValue> findIdDeclarations(ElementPattern<XmlAttributeValue> pattern, GlobalSearchScope searchScope) {

        Project project = searchScope.getProject();
        Set<XmlAttributeValue> result = new HashSet<>(512);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile file : files) {
            if (ConfigXmlUtils.isAF5ConfigFile(file)) {
                final PsiFile psiFile = psiManager.findFile(file);
                XmlFile xmlFile = (XmlFile) psiFile;
                Set<XmlAttributeValue> tags = get(xmlFile);
                for (XmlAttributeValue value : tags) {
                    if (pattern.accepts(value)) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    private XmlAttributeValue getIdDeclarationInFile(String id, ElementPattern<XmlAttributeValue> pattern, XmlFile xmlFile) {

        Set<XmlAttributeValue> values = get(xmlFile);
        for (XmlAttributeValue value : values) {
            String attValue = value.getValue();
            if (id.equalsIgnoreCase(attValue) && pattern.accepts(value)) return value;
        }
        return null;
    }

    private Set<XmlAttributeValue> doCompute(XmlFile xmlFile) {
        Set<XmlAttributeValue> result = new HashSet<>();
        xmlFile.accept(new XmlRecursiveElementVisitor(true) {
            @Override
            public void visitXmlAttributeValue(XmlAttributeValue value) {
                if (idDeclarationsPattern.accepts(value)) {
                    result.add(value);
                }
                super.visitXmlAttributeValue(value);
            }
        });
        return result;
    }

    private Set<XmlAttributeValue> get(XmlFile owner) {
        return cache.get(owner, null).getValue();
    }
}
