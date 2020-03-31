package tia.example.tooling.runtime.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import tia.example.tooling.runtime.templates.Af5FilesTemplateManager;

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
        return file;
    }
}

