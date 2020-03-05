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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.XmlFileHeader;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ConfigXmlUtils {

    private ConfigXmlUtils(){};


    public static final String NS_AF5_CONFIG = "https://cm5.intertrust.ru/config";
    public static final String NS_AF5_ACTION = "https://cm5.intertrust.ru/config/action";
    public static final String NS_AF5_EVENT = "https://cm5.intertrust.ru/config/event";
    public static final String NS_AF5_RULES = "https://cm5.intertrust.ru/config/rules";
    public static final String NS_AF5_MODULE = "https://cm5.intertrust.ru/config/module";

    public static final String TAG_AF5_CONFIG_ROOT = "configuration";
    public static final String TAG_DOP = "domain-object-type";
    public static final String TAG_REF = "reference";
    public static final String TAG_FIELD_GROUP = "field-group";

    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_EXTENDS = "extends";
    public static final String TAG_INCLUDE_GROUP = "include-group";


    public static boolean isAF5ConfigFile(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }
        if (psiFile.getFileType() != StdFileTypes.XML || psiFile.getVirtualFile().getExtension().equalsIgnoreCase("xsd")) {
            return false;
        }
        final XmlFile xmlFile = (XmlFile) psiFile;
        XmlFileHeader header = DomService.getInstance().getXmlFileHeader(xmlFile);
        if (header != null && ConfigXmlUtils.NS_AF5_CONFIG.equals(header.getRootTagNamespace())) {
            return true;
        }
        final XmlTag rootTag = xmlFile.getRootTag();
        return isAF5ConfigRootTag(rootTag);
    }


    public static boolean isAF5ConfigRootTag(XmlTag rootTag) {
        return rootTag.getLocalName().equals(TAG_AF5_CONFIG_ROOT);
    }

    public static Set<XmlTag> getGlobalTags(String tagName, @Nullable Module module) {
        Project project = module.getProject();
        GlobalSearchScope scope;
        if (module == null) {
            //inside extrenal library
            scope = GlobalSearchScope.allScope(project);
        } else {
            scope = GlobalSearchScope.moduleWithLibrariesScope(module);
        }
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.XML);
        return getGlobalTagsInScope(tagName, scope);
    }

    @NotNull
    private static Set<XmlTag> getGlobalTagsInScope(String tagName, GlobalSearchScope searchScope) {

        Project project = searchScope.getProject();
        Set<XmlTag> result = new HashSet<>(64);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);
            if (isAF5ConfigFile(psiFile)) {
                XmlFile xmlFile = (XmlFile) psiFile;
                final XmlTag rootElement = xmlFile.getRootTag();
                final XmlTag[] subTags = rootElement.getSubTags();
                for (XmlTag subTag : subTags) {
                    if (tagName.equals(subTag.getLocalName())){
                        result.add(subTag);
                    }
                }
            }
        }
        return result;
    }
    @Nullable
    public static XmlTag findGlobalTag(String id, String tagName, String idAttribute, PsiElement refElement) {

        final Project project = refElement.getProject();
        //Search first in the local file else we search globally
        final PsiFile psiFile = refElement.getContainingFile();
        XmlTag xmlTag = findGlobalTagInFile(id, tagName, idAttribute, psiFile);

        if (xmlTag == null) {
            Module module = ModuleUtil.findModuleForPsiElement(refElement);
            GlobalSearchScope scope;
            if (module == null) {
                //refElement is inside external library
                scope = GlobalSearchScope.allScope(project);
            } else {
                scope = GlobalSearchScope.moduleWithLibrariesScope(module);
            }
            scope = GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.XML);
            xmlTag = findGlobalTagInScope(id, tagName, idAttribute, scope);
        }
        return xmlTag;
    }
    @Nullable
    private static XmlTag findGlobalTagInScope(String id, String tagName, String idAttribute, GlobalSearchScope searchScope) {

        Project project = searchScope.getProject();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);

        for (VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);
            XmlTag tag = findGlobalTagInFile(id, tagName, idAttribute, psiFile);
            if (tag != null) return tag;
        }
        return null;
    }

    @Nullable
    private static XmlTag findGlobalTagInFile(String id, String tagName, String idAttribute, PsiFile psiFile) {

        if (!isAF5ConfigFile(psiFile)) {
            return null;
        }
        XmlFile xmlFile = (XmlFile) psiFile;

        final XmlTag rootElement = xmlFile.getRootTag();
        final XmlTag[] subTags = rootElement.getSubTags();
        for (XmlTag subTag : subTags) {
            if (id.equals(subTag.getAttributeValue(idAttribute)) && tagName.equals(subTag.getLocalName())) {
                return subTag;
            }
        }
        return null;
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

            @NotNull
            @Override
            public VirtualFile getFile() {
                return file;
            }

            @NotNull
            @Override
            public Navigatable createNavigatable(@NotNull Project project) {
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
        return findXmlTag(sourcePosition, rootTag);
    }

    private static XmlTag findXmlTag(XSourcePosition sourcePosition, XmlTag rootTag) {
        final XmlTag[] subTags = rootTag.getSubTags();
        for (int i = 0; i < subTags.length; i++) {
            XmlTag subTag = subTags[i];
            final int subTagLineNumber = getLineNumber(sourcePosition.getFile(), subTag);
            if (subTagLineNumber == sourcePosition.getLine()) {
                return subTag;
            } else if (subTagLineNumber > sourcePosition.getLine() && i > 0 && subTags[i - 1].getSubTags().length > 0) {
                return findXmlTag(sourcePosition, subTags[i - 1]);
            }
        }
        if (subTags.length > 0) {
            final XmlTag lastElement = subTags[subTags.length - 1];
            return findXmlTag(sourcePosition, lastElement);
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
