package ru.intertrust.af5.idea.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import ru.intertrust.af5.idea.templates.Af5FilesTemplateManager;
import ru.intertrust.af5.idea.util.CmModuleUtils;

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
        Module module = ModuleUtil.findModuleForFile(file.getVirtualFile(), file.getProject());
        CmModuleUtils.addToCmModule(file.getVirtualFile(), module);
        return file;
    }


    public static VirtualFile getAnyRoot(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        ProjectFileIndex index = ProjectFileIndex.SERVICE.getInstance(project);
        VirtualFile root = index.getContentRootForFile(virtualFile);
        if (root == null) root = index.getClassRootForFile(virtualFile);
        if (root == null) root = index.getSourceRootForFile(virtualFile);
        return root;
    }
}

