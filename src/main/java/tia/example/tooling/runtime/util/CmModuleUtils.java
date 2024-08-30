package tia.example.tooling.runtime.util;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateActionBase;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.XmlElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import tia.example.tooling.runtime.templates.Af5FilesTemplateManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CmModuleUtils {
    public static final String CM_MODULE_XML_PATH = "META-INF/cm-module.xml";
    private static final int PROLOG_SIZE = 512;

    private static final XmlElementPattern.XmlTextPattern REGISTERED_FILES_PATTERN = XmlPatterns.xmlText()
            .withParent(XmlPatterns.xmlTag().withLocalName(ConfigXmlUtils.TAG_CONFIGURATION_PATH));



    private static final UserDataCache<CachedValue<Set<String>>, XmlFile, Void> cache
            = new UserDataCache<>("tia.cache.xml.af5.module.files") {
        @Override
        protected CachedValue<Set<String>> compute(final XmlFile xmlFile, Void p) {
            CachedValuesManager manager = CachedValuesManager.getManager(xmlFile.getProject());
            return manager.createCachedValue(
            () -> CachedValueProvider.Result.create(doCompute(xmlFile), xmlFile), false);
        }
    };

    private CmModuleUtils(){};

    public static VirtualFile getCmModuleFile(Module module) {

        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        List<VirtualFile> resourceRoots = rootManager.getSourceRoots(JavaResourceRootType.RESOURCE);
        for (VirtualFile root : resourceRoots) {
            VirtualFile moduleXml = root.findFileByRelativePath(CM_MODULE_XML_PATH);
            if (moduleXml != null) return moduleXml;
        }
        return null;
    }
    public static boolean isCmModuleFile(@NotNull VirtualFile file) {

        if (!file.getPath().endsWith(CM_MODULE_XML_PATH)) {
            return false;
        }
        int fileLength = (int) file.getLength();
        if (fileLength <= 0) return false;
        String prolog =  LoadTextUtil.loadText(file, PROLOG_SIZE).toString();
        return prolog.contains(ConfigXmlUtils.NS_AF5_MODULE);
    }

    public static boolean isDependsOnAF5(Module module) {
        return JavaLibraryUtils.hasClass(module, "ru.intertrust.cm.core.config.DomainObjectConfig");


        /*final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

        final OrderEntry[] declaredDependencies = moduleRootManager.getOrderEntries();

        for (final OrderEntry entry : declaredDependencies) {
            if (entry instanceof LibraryOrderEntry) {
                final LibraryOrderEntry dependency = (LibraryOrderEntry) entry;
                String dependencyName = dependency.getLibraryName();
                if (dependencyName.contains("ru.intertrust.cm-sochi:model")) return true;
            }
        }
        return false;*/
    }

    public static boolean isDependsOnAF5(Project project) {
        return JavaLibraryUtils.hasClass(project, "ru.intertrust.cm.core.config.DomainObjectConfig");
    }


    public static XmlFile getCmModulePsiFile(Module module) {
        VirtualFile cmModuleFile = getCmModuleFile(module);
        if (cmModuleFile == null) return null;
        PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(cmModuleFile);
        if (!(psiFile instanceof XmlFile)) return null;
        return (XmlFile) psiFile;
    }
    public static PsiFile createCmModuleFile(Module module, boolean openFile){
        final FileTemplate template = FileTemplateManager.getInstance(module.getProject()).getInternalTemplate(Af5FilesTemplateManager.AF_MODULE_FILE);
        if (template == null) return null;

        PsiDirectory rootDir = getResourceRoot(module);
        if (rootDir == null) return null;

        PsiFile cmModuleFile = CreateFileFromTemplateAction.createFileFromTemplate(CmModuleUtils.CM_MODULE_XML_PATH, template, rootDir, null, openFile);
        return cmModuleFile;
    }

    public static PsiFile createCmModuleFile(Module module, Collection<VirtualFile> af5ConfigXmlFiles, boolean openFile){


        PsiFile cmModuleFile = createCmModuleFile(module, false);
        if (cmModuleFile == null) return null;

        WriteCommandAction.writeCommandAction(cmModuleFile)
                .withName("'Add AF5 config files to cm-module.xml'")
                .run(() -> {

            for (VirtualFile af5ConfigXmlFile : af5ConfigXmlFiles) {
                addToCmModule(af5ConfigXmlFile, module);
            }

            if (openFile) {
                final FileTemplate template = FileTemplateManager.getInstance(module.getProject()).getInternalTemplate(Af5FilesTemplateManager.AF_MODULE_FILE);
                if (template.isLiveTemplateEnabled()) {
                    CreateFromTemplateActionBase.startLiveTemplate(cmModuleFile, Collections.emptyMap());
                } else {
                    FileEditorManager.getInstance(module.getProject()).openFile(cmModuleFile.getVirtualFile(), true);
                }
            }
        });

        return cmModuleFile;
    }

    public static void addToCmModule(VirtualFile virtualFile, Module module) {

        /*Project project = file.getProject();
        VirtualFile virtualFile = file.getVirtualFile();*/
        //Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        if (module == null) return;
        Project project = module.getProject();

        XmlFile xml = CmModuleUtils.getCmModulePsiFile(module);
        if (xml == null) return;
        XmlTag rootTag = xml.getRootTag();
        if (rootTag == null) return;
        //XmlDocument xmlDoc = xml.getDocument();
        if (!ConfigXmlUtils.NS_AF5_MODULE.equals(rootTag.getNamespace())) return;

        WriteCommandAction.writeCommandAction(xml)
                .withName("Add file '" + virtualFile.getName() + "' to cm-module.xml")
                .run(() -> {
                    setPath(virtualFile, rootTag, project);

                    XmlTag nameTag = rootTag.findFirstSubTag(ConfigXmlUtils.TAG_CONFIGURATION_NAME);
                    if (nameTag == null) return;
                    nameTag.getValue().setText(module.getName());
                });
    }

    private static void setPath(VirtualFile af5ConfigFile, XmlTag rootTag, Project project) {
        XmlTag[] subTags = rootTag.findSubTags(ConfigXmlUtils.TAG_CONFIGURATION_PATHS, ConfigXmlUtils.NS_AF5_MODULE);
        XmlTag configPaths;
        if (subTags.length == 0) {
            configPaths = rootTag.createChildTag(ConfigXmlUtils.TAG_CONFIGURATION_PATHS, ConfigXmlUtils.NS_AF5_MODULE, null, false);
            configPaths = rootTag.addSubTag(configPaths, false);
        } else {
            configPaths = subTags[0];
        }

                    /*ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
                    List<VirtualFile> resourceRoots = rootManager.getSourceRoots(JavaResourceRootType.RESOURCE);*/
        //String body = IfsUtil.getReferencePath(project, file.getVirtualFile());
        //body = body.substring(1);

        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        VirtualFile sourceRootForFile = projectRootManager.getFileIndex().getSourceRootForFile(af5ConfigFile);
        String body = VfsUtilCore.findRelativePath(sourceRootForFile, af5ConfigFile, '/');

        XmlTag[] paths = configPaths.findSubTags(ConfigXmlUtils.TAG_CONFIGURATION_PATH, ConfigXmlUtils.NS_AF5_MODULE);

        XmlTag targetPath = null;
        for (XmlTag path : paths) {
            XmlTagValue value = path.getValue();
            if (value.getTrimmedText().isEmpty()) {
                targetPath = path;
                break;
            }
        }

        if (targetPath == null) {
            targetPath = configPaths.createChildTag(ConfigXmlUtils.TAG_CONFIGURATION_PATH, ConfigXmlUtils.NS_AF5_MODULE, body, false);
            targetPath = configPaths.addSubTag(targetPath, false);
        } else {
            targetPath.getValue().setText(body);
        }
    }

    public static boolean isRegistered(VirtualFile virtualFile, Module module) {

        if (virtualFile == null) return false;

        Project project = module.getProject();
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

        VirtualFile sourceRootForFile = projectRootManager.getFileIndex().getSourceRootForFile(virtualFile);
        if (sourceRootForFile == null) return false;
        String body = VfsUtilCore.findRelativePath(sourceRootForFile, virtualFile, '/');

        //Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        //if (module == null) return false;

        XmlFile cmModuleXml = CmModuleUtils.getCmModulePsiFile(module);
        if (cmModuleXml == null) return false;
        Set<String> registeredFiles = cache.get(cmModuleXml, null).getValue();
        return registeredFiles.contains(body.toLowerCase());
    }
    private static PsiDirectory getResourceRoot(Module module) {

        PsiDirectory psiDirectory = null;

        Project project = module.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        List<VirtualFile> resourceRoots = rootManager.getSourceRoots(JavaResourceRootType.RESOURCE);

        if (resourceRoots.isEmpty()) return null;
        if (resourceRoots.size() == 1) {
            psiDirectory = psiManager.findDirectory(resourceRoots.get(0));
        } else {
            PsiDirectory[] dirs = new PsiDirectory[resourceRoots.size()];
            for (int i = 0; i < resourceRoots.size(); i++) {
                VirtualFile root = resourceRoots.get(i);
                dirs[i] = psiManager.findDirectory(root);
            }
            psiDirectory = DirectoryChooserUtil.selectDirectory(project, dirs, null, "");
        }
        return psiDirectory;
    }

    private static Set<String> doCompute(XmlFile xmlFile) {
        Set<String> result = new HashSet<>();
        xmlFile.accept(new XmlRecursiveElementVisitor(true) {
            @Override
            public void visitXmlText(XmlText text) {
                if (REGISTERED_FILES_PATTERN.accepts(text)) {
                    result.add(text.getValue().toLowerCase());
                }
                super.visitXmlText(text);
            }
        });
        return result;
    }
}
