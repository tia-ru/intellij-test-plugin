package tia.example.tooling.runtime.util;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import tia.example.tooling.runtime.templates.Af5FilesTemplateManager;

import java.util.List;

public final class CmModuleUtils {
    public static final String CM_MODULE_XML_PATH = "META-INF/cm-module.xml";

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
    public static PsiFile getCmModulePsiFile(Module module) {
        VirtualFile cmModuleFile = getCmModuleFile(module);
        if (cmModuleFile == null) return null;
        PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(cmModuleFile);
        return psiFile;
    }
    public static PsiFile createCmModuleFile(Module module, boolean openFile){
        final FileTemplate template = FileTemplateManager.getInstance(module.getProject()).getInternalTemplate(Af5FilesTemplateManager.AF_MODULE_FILE);
        if (template == null) return null;

        PsiDirectory rootDir = getResourceRoot(module);
        if (rootDir == null) return null;

        PsiFile cmModuleFile = CreateFileFromTemplateAction.createFileFromTemplate(CmModuleUtils.CM_MODULE_XML_PATH, template, rootDir, null, openFile);
        return cmModuleFile;
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
}
