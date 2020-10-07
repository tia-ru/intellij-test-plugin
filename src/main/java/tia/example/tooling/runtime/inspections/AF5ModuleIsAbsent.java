package tia.example.tooling.runtime.inspections;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.CommonProblemDescriptor;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.GlobalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ModuleProblemDescriptor;
import com.intellij.codeInspection.ProblemDescriptionsProcessor;
import com.intellij.codeInspection.QuickFix;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.codeInspection.reference.RefModule;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tia.example.tooling.runtime.util.CmModuleUtils;

public class AF5ModuleIsAbsent extends GlobalInspectionTool {
    @Override
    public boolean isGraphNeeded() {
        return false;
    }

    @Override
    @Nullable
    public CommonProblemDescriptor[] checkElement(@NotNull RefEntity refEntity,
                                                  @NotNull AnalysisScope scope,
                                                  @NotNull InspectionManager manager,
                                                  @NotNull GlobalInspectionContext globalContext,
                                                  @NotNull ProblemDescriptionsProcessor processor) {
        if (!(refEntity instanceof RefModule)) return null;

        Module module = ((RefModule) refEntity).getModule();
        if (module.isDisposed() || !scope.containsModule(module) || !CmModuleUtils.isDependsOnAF5(module)) return null;


        if (null != CmModuleUtils.getCmModuleFile(module)) return null;

        ModuleProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                AF5Bundle.message("af5.config.cmmodule.description", module.getName()),
                module,
                new CreateCmModuleFix()
        );

        return new CommonProblemDescriptor[]{problemDescriptor};
    }


    @Override
    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return "AF5";
    }


    private static class CreateCmModuleFix implements QuickFix<ModuleProblemDescriptor> {

        @NotNull
        @Override
        public String getFamilyName() {
            return "Create cm-module.xml";
        }

        @NotNull
        @Override
        public String getName() {
            return "Create cm-module.xml for ...";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ModuleProblemDescriptor descriptor) {
            Module module = descriptor.getModule();
            CmModuleUtils.createCmModuleFile(module, true);
        }
    }
}
