package tia.example.tooling.runtime.util;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
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
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.XmlFileHeader;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;

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

    public static final String ATTR_NAME = "name";
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_EXTENDS = "extends";

    private static final int PROLOG_SIZE = 512;

    public static boolean isAF5ConfigFile(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }

        if (!XmlFileType.INSTANCE.equals(psiFile.getFileType()) || "xsd".equalsIgnoreCase(psiFile.getVirtualFile().getExtension())) {
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

    /** based on code in {@code com.intellij.openapi.fileTypes.impl.FileTypeManagerImpl#detectFromContent}
     */
    public static boolean isAF5ConfigFile(@NotNull VirtualFile file) {

        if (!XmlFileType.INSTANCE.equals(file.getFileType()) || "xsd".equalsIgnoreCase(file.getExtension())) {
            return false;
        }

        int fileLength = (int) file.getLength();
        if (fileLength <= 0) return false;
        String prolog =  LoadTextUtil.loadText(file, PROLOG_SIZE).toString();
        return prolog.contains(ConfigXmlUtils.NS_AF5_CONFIG) || prolog.contains(ConfigXmlUtils.NS_AF5_ACTION);
    }


    private int readSafely(@NotNull InputStream stream, @NotNull byte[] buffer, int offset, int length) throws IOException {
        int n = stream.read(buffer, offset, length);
        if (n <= 0) {
            // maybe locked because someone else is writing to it
            // repeat inside read action to guarantee all writes are finished
            n = ReadAction.compute(() -> stream.read(buffer, offset, length));
        }
        return n;
    }

    public static boolean isAF5ConfigRootTag(@Nonnull XmlTag rootTag) {
        return TAG_AF5_CONFIG_ROOT.equals(rootTag.getLocalName());
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
