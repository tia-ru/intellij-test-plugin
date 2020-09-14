package tia.example.tooling.runtime.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tia.example.tooling.runtime.util.CmModuleUtils;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

import java.util.ArrayList;
import java.util.List;

public class AF5ConfigIsNotRegisteredInCmModule  extends LocalInspectionTool {
    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(
            @NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {

        boolean isAf5ConfigFile = ConfigXmlUtils.isAF5ConfigFile(file);

        Project project = file.getProject();
        Module module = ModuleUtil.findModuleForFile(file.getVirtualFile(), project);
        if (module == null) return null;

        List<? super ProblemDescriptor> result = new ArrayList<>(2);

        VirtualFile cmModuleFile = CmModuleUtils.getCmModuleFile(module);
        if (cmModuleFile == null) {
            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                    file,
                    AF5Bundle.message("af5.config.cmmodule.description", module.getName()),
                    new CreateCmModuleFix(),
                    ProblemHighlightType.ERROR,
                    isOnTheFly);
            result.add(problemDescriptor);
        }

        /*VirtualFile cmModuleFile = CmModuleUtils.getCmModuleFile(module);
        if (cmModuleFile == null && (isAf5ConfigFile || CmModuleUtils.isDependsOnAF5(module))) {
            CmModuleAbsentNotification notification = CmModuleAbsentNotification.create(module);
            notification.notify(project);
        }*/

        if (!isAf5ConfigFile) return null;

        if (!CmModuleUtils.isRegistered(file.getVirtualFile(), module)) {
            //PsiElement child = file.getFirstChild().getChildren()[0];
            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                    file,
                    AF5Bundle.message("af5.config.register.description", file.getName()),
                    new AF5ConfigIsNotRegisteredInCmModuleFix(file.getName()),
                    ProblemHighlightType.ERROR,
                    isOnTheFly);
            result.add(problemDescriptor);
        }
        return result.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }

    private static class AF5ConfigIsNotRegisteredInCmModuleFix implements LocalQuickFix {

        private final String fileName;

        AF5ConfigIsNotRegisteredInCmModuleFix(String fileName){

            this.fileName = fileName;
        }

        @NotNull
        @Override
        public @IntentionFamilyName String getFamilyName() {
            return AF5Bundle.message("af5.config.register.family", "");
        }

        @NotNull
        @Override
        public @IntentionName String getName() {
            return AF5Bundle.message("af5.config.register.fix", fileName);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            if (!(descriptor.getPsiElement() instanceof XmlFile)) return;
            VirtualFile fileToRegister = descriptor.getPsiElement().getContainingFile().getVirtualFile();
            Module module = ModuleUtil.findModuleForFile(fileToRegister, project);
            VirtualFile cmModuleFile = CmModuleUtils.getCmModuleFile(module);
            if (cmModuleFile == null) {
                CmModuleUtils.createCmModuleFile(module, true);
            }
            CmModuleUtils.addToCmModule(fileToRegister, project);
        }
    }

    private static class CreateCmModuleFix implements LocalQuickFix {

        @NotNull
        @Override
        public @IntentionFamilyName String getFamilyName() {
            return "Create cm-module.xml";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            if (!(descriptor.getPsiElement() instanceof XmlFile)) return;
            VirtualFile fileToRegister = descriptor.getPsiElement().getContainingFile().getVirtualFile();
            Module module = ModuleUtil.findModuleForFile(fileToRegister, project);
            CmModuleUtils.createCmModuleFile(module, true);
        }
    }
}
