package tia.example.tooling.runtime.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import tia.example.tooling.runtime.templates.Af5FilesTemplateManager;
import tia.example.tooling.runtime.util.CmModuleUtils;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

public class CreateDopFileAction extends CreateFileFromTemplateAction implements DumbAware {

    public CreateDopFileAction() {
        super("AF5 Config", "Create new AF5 configuration file.", AllIcons.FileTypes.Xml);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle("AF5 Config")
                .addKind("AF5 Config", AllIcons.FileTypes.Xml, Af5FilesTemplateManager.AF5_CONFIG_FILE);
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName) {
        return "Create " + newName;
    }

    @Override
    public int hashCode() {
        return 523213345;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CreateDopFileAction;
    }

    @Override
    protected PsiFile createFile(String name, String templateName, PsiDirectory dir) {
        PsiFile file = super.createFile(name, templateName, dir);
        addToCmModule(file);
        return file;
    }

    private void addToCmModule(PsiFile file) {

        Project project = file.getProject();
        Module module = ModuleUtil.findModuleForFile(file.getVirtualFile(), project);
        if (module == null) return;

        PsiFile cmModuleFile = CmModuleUtils.getCmModulePsiFile(module);
        if (!(cmModuleFile instanceof XmlFile)) return;
        XmlFile xml = (XmlFile) cmModuleFile;
        XmlTag rootTag = xml.getRootTag();
        //XmlDocument xmlDoc = xml.getDocument();
        if (!ConfigXmlUtils.NS_AF5_MODULE.equals(rootTag.getNamespace())) return;

        XmlTag[] subTags = rootTag.findSubTags(ConfigXmlUtils.TAG_CONFIGURATION_PATHS, ConfigXmlUtils.NS_AF5_MODULE);
        XmlTag configPaths;
        if (subTags.length == 0 ){
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
        VirtualFile sourceRootForFile = projectRootManager.getFileIndex().getSourceRootForFile(file.getVirtualFile());
        String body = VfsUtilCore.findRelativePath(sourceRootForFile, file.getVirtualFile(), '/');

        XmlTag path = configPaths.createChildTag(ConfigXmlUtils.TAG_CONFIGURATION_PATH, ConfigXmlUtils.NS_AF5_MODULE, body, false);
        path = configPaths.addSubTag(path, false);

    }

    public static VirtualFile getAnyRoot(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        ProjectFileIndex index = ProjectFileIndex.SERVICE.getInstance(project);
        VirtualFile root = index.getContentRootForFile(virtualFile);
        if (root == null) root = index.getClassRootForFile(virtualFile);
        if (root == null) root = index.getSourceRootForFile(virtualFile);
        return root;
    }
}

