package tia.example.tooling.runtime.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import tia.example.tooling.runtime.templates.Af5FilesTemplateManager;

import java.util.List;

public class CreateModuleFileAction extends AnAction implements WriteActionAware, DumbAware {

    private static final String CM_MODULE_XML_PATH = "META-INF/cm-module.xml";

    public CreateModuleFileAction() {
        super("AF5 Module", "Create new AF5 module configuration file.", AllIcons.FileTypes.Xml);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        final DataContext dataContext = e.getDataContext();

        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return;
        }

        final Project project = CommonDataKeys.PROJECT.getData(dataContext);

        final PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null || project == null) return;

        createCmModuleFile(dir);
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {

        final DataContext dataContext = e.getDataContext();
        final boolean enabled = isAvailable(dataContext);
        final Presentation presentation = e.getPresentation();
        presentation.setEnabledAndVisible(enabled);
    }

    private void createCmModuleFile(PsiDirectory dir) {
        Project project = dir.getProject();
        Module module = ModuleUtil.findModuleForFile(dir.getVirtualFile(), project);
        if (module == null) return;

        final FileTemplate template = FileTemplateManager.getInstance(project).getInternalTemplate(Af5FilesTemplateManager.AF_MODULE_FILE);
        if (template == null) return;

        PsiDirectory rootDir = getResourceRoot(module);
        if (rootDir == null) return;

        PsiFile cmModuleFile = CreateFileFromTemplateAction.createFileFromTemplate(CM_MODULE_XML_PATH, template, rootDir, null, true);
        //Messages.showMessageDialog(myProject, errorMessage, myErrorTitle, Messages.getErrorIcon());
    }

    private PsiDirectory getResourceRoot(Module module) {

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

    @Override
    public int hashCode() {
        return 12312392;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CreateModuleFileAction;
    }

    protected boolean isAvailable(DataContext dataContext) {

        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return false;
        }
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null || project == null) return false;

        Module module = ModuleUtil.findModuleForFile(dir.getVirtualFile(), project);
        if (module == null) return false;

        VirtualFile cmModuleFile = getCmModuleFile(module);
        return (null == cmModuleFile);
    }

    private static VirtualFile getCmModuleFile(Module module) {

        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        List<VirtualFile> resourceRoots = rootManager.getSourceRoots(JavaResourceRootType.RESOURCE);
        for (VirtualFile root : resourceRoots) {
            VirtualFile moduleXml = root.findFileByRelativePath(CM_MODULE_XML_PATH);
            if (moduleXml != null) return moduleXml;
        }
        return null;
    }


}

