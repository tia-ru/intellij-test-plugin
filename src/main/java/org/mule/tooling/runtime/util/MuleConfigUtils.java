package org.mule.tooling.runtime.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import com.mulesoft.mule.debugger.commons.*;
//import org.mule.tooling.esb.config.MuleConfigConstants;
//import org.mule.tooling.esb.config.model.Flow;
//import org.mule.tooling.esb.config.model.Mule;
//import org.mule.tooling.esb.config.model.SubFlow;
//import org.mule.tooling.esb.framework.facet.MuleFacet;
//import org.mule.tooling.esb.framework.facet.MuleFacetConfiguration;
//import org.mule.tooling.esb.framework.facet.MuleFacetType;
//import org.mule.tooling.esb.sdk.MuleSdk;

public class MuleConfigUtils {

    public static final String MULE_LOCAL_NAME = "mule";
    public static final String MULE_FLOW_LOCAL_NAME = "flow";
    public static final String MULE_FLOW_REF_LOCAL_NAME = "flow-ref";
    public static final String MULE_SUB_FLOW_LOCAL_NAME = "sub-flow";

    public static final String MUNIT_TEST_LOCAL_NAME = "test";
    public static final String MUNIT_NAMESPACE = "munit";


    public static final String NAME_ATTRIBUTE = "name";

    public static final String AF5_CONFIG_NAMESPACE = "https://cm5.intertrust.ru/config";
    public static final String AF5_CONFIG_ROOT_TAG = "configuration";
    public static final String REF_TAG = "reference";
    public static final String REF_ATTRIBUTE = "type";
    public static final String DOP_TAG = "domain-object-type";
    public static final String EXTENDS_ATTRIBUTE = "extends";

    public static boolean isMuleFile(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }
        if (psiFile.getFileType() != StdFileTypes.XML || psiFile.getVirtualFile().getExtension().equalsIgnoreCase("xsd")) {
            return false;
        }
        final XmlFile xmlFile = (XmlFile) psiFile;
        final XmlTag rootTag = xmlFile.getRootTag();
        return isMuleTag(rootTag);
    }

    public static boolean isMUnitFile(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }
        if (psiFile.getFileType() != StdFileTypes.XML) {
            return false;
        }
        final XmlFile psiFile1 = (XmlFile) psiFile;
        final XmlTag rootTag = psiFile1.getRootTag();
        if (rootTag == null || !isMuleTag(rootTag)) {
            return false;
        }
        final XmlTag[] munitTags = rootTag.findSubTags(MUNIT_TEST_LOCAL_NAME, rootTag.getNamespaceByPrefix(MUNIT_NAMESPACE));
        return munitTags.length > 0;
    }

    public static boolean isMuleTag(XmlTag rootTag) {
        return rootTag.getLocalName().equalsIgnoreCase(AF5_CONFIG_ROOT_TAG);
    }

    public static boolean isFlowTag(XmlTag rootTag) {
        return rootTag.getLocalName().equalsIgnoreCase(MULE_FLOW_LOCAL_NAME);
    }

    public static boolean isSubFlowTag(XmlTag rootTag) {
        return rootTag.getLocalName().equalsIgnoreCase(MULE_SUB_FLOW_LOCAL_NAME);
    }

    public static boolean isMUnitTestTag(XmlTag rootTag) {
        return rootTag.getLocalName().equalsIgnoreCase(MUNIT_TEST_LOCAL_NAME);
    }

    public static boolean isTopLevelTag(XmlTag tag) {
//        return isFlowTag(tag) || isSubFlowTag(tag) || isMUnitTestTag(tag) || isExceptionStrategyTag(tag);
        return isFlowTag(tag) || isSubFlowTag(tag) || isMUnitTestTag(tag);
    }

    public static boolean isInTopLevelTag(XmlTag tag) {
        boolean inTopLevel = false;
        XmlTag current = tag;

        while (!inTopLevel && current != null) {
            inTopLevel = MuleConfigUtils.isTopLevelTag(current);
            if (!inTopLevel)
                current = current.getParentTag();
        }

        return inTopLevel;
    }

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

    @NotNull
    private static String getPrefix(XmlTag weavePart) {
        final String localName = weavePart.getLocalName();
        if (localName.equals("set-payload")) {
            return "payload:";
        } else if (localName.equals("set-variable")) {
            return "flowVar:" + weavePart.getAttributeValue("variableName");
        } else if (localName.equals("set-property")) {
            return "property:" + weavePart.getAttributeValue("propertyName");
        } else if (localName.equals("set-session-variable")) {
            return "sessionVar:" + weavePart.getAttributeValue("variableName");
        }

        return "payload:";
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

    @NotNull
    public static String asMelScript(@NotNull String script) {
        return !script.startsWith("#[") ? "#[" + script + "]" : script;
    }

    public static boolean isGlobalElement(XmlTag subTag) {
        return subTag.getName().equals("domain-object-type");
    }

    @Nullable
    public static XmlTag findParentXmlTag(PsiElement element) {
        PsiElement psiElement = element;

        while (psiElement != null && !(psiElement instanceof XmlTag))
            psiElement = psiElement.getParent();

        return (XmlTag) psiElement;
    }

    public static List<XmlTag> getGlobalElements(Project project) {
        return getGlobalElementsInScope(project, GlobalSearchScope.allScope(project));
    }

    @NotNull
    private static List<XmlTag> getGlobalElementsInScope(Project project, GlobalSearchScope searchScope) {

        List<XmlTag> result = new ArrayList<>(64);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);
        for (VirtualFile file : files) {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (isMuleFile(psiFile)) {
                XmlFile xmlFile = (XmlFile) psiFile;
                final XmlTag rootElement = xmlFile.getRootTag();
                final XmlTag[] subTags = rootElement.getSubTags();
                for (XmlTag subTag : subTags) {
                    if (isGlobalElement(subTag)) {
                        result.add(subTag);
                    }
                }
            }
        }
        return result;
    }
    @Nullable
    public static XmlTag findGlobalElement(PsiElement element, String value) {
        final Project project = element.getProject();
        final PsiFile psiFile = PsiTreeUtil.getParentOfType(element, PsiFile.class);
        //Search first in the local file else we search globally
        if (psiFile != null) {
            final XmlTag xmlTag = findGlobalElementInFile(project, value, psiFile);
            if (xmlTag != null) {
                return xmlTag;
            }
        }
        final GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
        return findGlobalElementInScope(project, value, searchScope);
    }
    @Nullable
    private static XmlTag findGlobalElementInFile(Project project, String value, PsiFile psiFile) {

        if (!isMuleFile(psiFile)) {
            return null;
        }
        XmlFile xmlFile = (XmlFile) psiFile;

        final XmlTag rootElement = xmlFile.getRootTag();
        final XmlTag[] subTags = rootElement.getSubTags();
        for (XmlTag subTag : subTags) {
            if (isGlobalElement(subTag)) {
                if (value.equals(subTag.getAttributeValue(MuleConfigUtils.NAME_ATTRIBUTE))) {
                    return subTag;
                }
            }
        }

        return null;
    }
    @Nullable
    private static XmlTag findGlobalElementInScope(Project project, String value, GlobalSearchScope searchScope) {

        final Collection<VirtualFile> files = FileTypeIndex.getFiles(StdFileTypes.XML, searchScope);
        final PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);
            XmlTag tag = findGlobalElementInFile(project, value, psiFile);
            if (tag != null) return tag;
        }
        return null;
    }

}
