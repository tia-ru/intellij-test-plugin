package tia.example.tooling.runtime.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import tia.example.tooling.runtime.util.CmModuleUtils;

public class CreateModuleFileAction extends AnAction implements WriteActionAware, DumbAware {

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

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private void createCmModuleFile(PsiDirectory dir) {
        Project project = dir.getProject();
        Module module = ModuleUtil.findModuleForFile(dir.getVirtualFile(), project);
        if (module == null) return;
        PsiFile cmModuleFile = CmModuleUtils.createCmModuleFile(module, true);
        //Messages.showMessageDialog(myProject, errorMessage, myErrorTitle, Messages.getErrorIcon());
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

        VirtualFile cmModuleFile = CmModuleUtils.getCmModuleFile(module);
        return (null == cmModuleFile);
    }


}

