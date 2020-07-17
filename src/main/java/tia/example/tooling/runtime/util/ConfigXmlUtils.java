package tia.example.tooling.runtime.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.pom.NonNavigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.XmlFileHeader;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.*;

public final class ConfigXmlUtils {

    private ConfigXmlUtils(){}


    public static final String NS_AF5_CONFIG = "https://cm5.intertrust.ru/config";
    public static final String NS_AF5_ACTION = "https://cm5.intertrust.ru/config/action";
    public static final String NS_AF5_EVENT = "https://cm5.intertrust.ru/config/event";
    public static final String NS_AF5_RULES = "https://cm5.intertrust.ru/config/rules";
    public static final String NS_AF5_MODULE = "https://cm5.intertrust.ru/config/module";

    public static final String TAG_AF5_CONFIG_ROOT = "configuration";
    public static final String TAG_DOP = "domain-object-type";
    public static final String TAG_REF = "reference";
    public static final String TAG_FIELD_GROUP = "field-group";
    public static final String TAG_INCLUDE_GROUP = "include-group";
    public static final String TAG_CONFIGURATION_PATHS = "configuration-paths";
    public static final String TAG_CONFIGURATION_PATH = "configuration-path";

    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_EXTENDS = "extends";



    public static boolean isAF5ConfigFile(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }
        if (!StdFileTypes.XML.equals(psiFile.getFileType()) || "xsd".equalsIgnoreCase(psiFile.getVirtualFile().getExtension())) {
            return false;
        }
        final XmlFile xmlFile = (XmlFile) psiFile;
        XmlFileHeader header = DomService.getInstance().getXmlFileHeader(xmlFile);
        if (header == null) return false;
        String namespace = header.getRootTagNamespace();
        if (NS_AF5_CONFIG.equals(namespace) || NS_AF5_ACTION.equals(namespace)) {
            return true;
        }
        final XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) return false;
        
        return isAF5ConfigRootTag(rootTag);
    }


    public static boolean isAF5ConfigRootTag(@Nonnull XmlTag rootTag) {
        return TAG_AF5_CONFIG_ROOT.equals(rootTag.getLocalName());
    }

    public static Set<XmlTag> findTags(String tagPath, @Nonnull Module module) {

        GlobalSearchScope scope = GlobalSearchScope.moduleRuntimeScope(module, false);
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.XML);
        Set<XmlTag> tags = findTagsInScope(tagPath, scope);
        return tags;

    }
    public static Set<XmlTag> findTags(String tagPath, @Nonnull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.XML);
        Set<XmlTag> tags = findTagsInScope(tagPath, scope);
        return tags;

    }

    @Nonnull
    private static Set<XmlTag> findTagsInScope(String tagPath, GlobalSearchScope searchScope) {

        String[] split = tagPath.split("/");

        Project project = searchScope.getProject();
        Set<XmlTag> result = new HashSet<>(512);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);
            if (isAF5ConfigFile(psiFile)) {
                XmlFile xmlFile = (XmlFile) psiFile;
                final XmlTag rootElement = xmlFile.getRootTag();
                List<XmlTag> subTags = findSubTags(rootElement, split, 0);
                result.addAll(subTags);
            }
        }
        return result;
    }

    @Nullable
    public static XmlTag getTag(String id, String tagPath, String idAttribute, PsiElement refElement) {

        final Project project = refElement.getProject();
        //Search in the current file at first else we search globally
        final PsiFile psiFile = refElement.getContainingFile();
        XmlTag xmlTag = getTagInFile(id, tagPath, idAttribute, psiFile);
        if (xmlTag != null) {
            return xmlTag;
        }

        Module module = ModuleUtil.findModuleForPsiElement(refElement);
        GlobalSearchScope scope;
        if (module == null) {
            //refElement is inside external library
            scope = GlobalSearchScope.allScope(project);
        } else {
            scope = GlobalSearchScope.moduleRuntimeScope(module, false);//getModuleWithDependenciesAndLibrariesScope
        }
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.XML);
        xmlTag = getTagInScope(id, tagPath, idAttribute, scope);
        return xmlTag;
    }
    @Nullable
    private static XmlTag getTagInScope(String id, String tagPath, String idAttribute, GlobalSearchScope searchScope) {

        Project project = searchScope.getProject();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);

        for (VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);
            if (isAF5ConfigFile(psiFile)) {
                XmlTag tag = getTagInFile(id, tagPath, idAttribute, psiFile);
                if (tag != null) return tag;
            }
        }
        return null;
    }

    @Nullable
    private static XmlTag getTagInFile(String id, String tagPath, String idAttribute, PsiFile psiFile) {

        if (!isAF5ConfigFile(psiFile)) {
            return null;
        }

        XmlFile xmlFile = (XmlFile) psiFile;

        final XmlTag rootElement = xmlFile.getRootTag();
        if (rootElement == null) return null;
        String[] split = tagPath.split("/");
        final List<XmlTag> subTags = findSubTags(rootElement, split, 0);
        for (XmlTag tag : subTags) {
            if (id.equals(tag.getAttributeValue(idAttribute))) {
                return tag;
            }
        }
        return null;
    }

    private static List<XmlTag> findSubTags(XmlTag parent, String[] tagPathItems, int curIdx){
        String name = tagPathItems[curIdx];
        XmlTag[] tags = parent.findSubTags(name, parent.getNamespace());
        if (curIdx == tagPathItems.length - 1){
            return Arrays.asList(tags);
        } else {
            List<XmlTag> result = new ArrayList<>(tags.length * 2);
            for (XmlTag subTag : tags) {
                List<XmlTag> subTags = findSubTags(subTag, tagPathItems, curIdx + 1);
                result.addAll(subTags);
            }
            return result;
        }
    }

    //=================================================================================================================

    public static QName getQName(XmlTag xmlTag) {
        return new QName(xmlTag.getNamespace(), xmlTag.getLocalName());
    }

    @Nullable
    public static XSourcePosition createPositionByElement(PsiElement element) {
        if (element == null)
            return null;

        PsiFile psiFile = element.getContainingFile();
        if (psiFile == null)
            return null;

        final VirtualFile file = psiFile.getVirtualFile();
        if (file == null)
            return null;

        final SmartPsiElementPointer<PsiElement> pointer =
                SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);

        return new XSourcePosition() {
            private volatile XSourcePosition myDelegate;

            private XSourcePosition getDelegate() {
                if (myDelegate == null) {
                    myDelegate = ApplicationManager.getApplication().runReadAction(new Computable<XSourcePosition>() {
                        @Override
                        public XSourcePosition compute() {
                            PsiElement elem = pointer.getElement();
                            return XSourcePositionImpl.createByOffset(pointer.getVirtualFile(), elem != null ? elem.getTextOffset() : -1);
                        }
                    });
                }
                return myDelegate;
            }

            @Override
            public int getLine() {
                return getDelegate().getLine();
            }

            @Override
            public int getOffset() {
                return getDelegate().getOffset();
            }

            @Nonnull
            @Override
            public VirtualFile getFile() {
                return file;
            }

            @Nonnull
            @Override
            public Navigatable createNavigatable(@Nonnull Project project) {
                // no need to create delegate here, it may be expensive
                if (myDelegate != null) {
                    return myDelegate.createNavigatable(project);
                }
                PsiElement elem = pointer.getElement();
                if (elem instanceof Navigatable) {
                    return ((Navigatable) elem);
                }
                return NonNavigatable.INSTANCE;
            }
        };
    }

    @Nullable
    public static XmlTag getXmlTagAt(Project project, XSourcePosition sourcePosition) {
        final VirtualFile file = sourcePosition.getFile();
        final XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(file);
        final XmlTag rootTag = xmlFile.getRootTag();
        return getXmlTagAtPosition(sourcePosition, rootTag);
    }

    private static XmlTag getXmlTagAtPosition(XSourcePosition sourcePosition, XmlTag rootTag) {
        final XmlTag[] subTags = rootTag.getSubTags();
        for (int i = 0; i < subTags.length; i++) {
            XmlTag subTag = subTags[i];
            final int subTagLineNumber = getLineNumber(sourcePosition.getFile(), subTag);
            if (subTagLineNumber == sourcePosition.getLine()) {
                return subTag;
            } else if (subTagLineNumber > sourcePosition.getLine() && i > 0 && subTags[i - 1].getSubTags().length > 0) {
                return getXmlTagAtPosition(sourcePosition, subTags[i - 1]);
            }
        }
        if (subTags.length > 0) {
            final XmlTag lastElement = subTags[subTags.length - 1];
            return getXmlTagAtPosition(sourcePosition, lastElement);
        } else {
            return null;
        }
    }

    public static int getLineNumber(VirtualFile file, XmlTag tag) {
        final int offset = tag.getTextOffset();
        final Document document = FileDocumentManager.getInstance().getDocument(file);
        return offset < document.getTextLength() ? document.getLineNumber(offset) : -1;
    }

}
