package tia.example.tooling.runtime.validation;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.SuppressionUtil;
import com.intellij.framework.detection.DetectionExcludesConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.NullableFunction;
import org.jetbrains.annotations.NotNull;
import tia.example.tooling.runtime.inspections.AF5Bundle;
import tia.example.tooling.runtime.inspections.AF5ConfigIsNotRegisteredInCmModule;
import tia.example.tooling.runtime.util.CmModuleUtils;

import java.util.HashSet;
import java.util.Set;

class AFCmModuleAbsentCollector {
    private static final Logger LOG = Logger.getInstance("#com.intellij.spring.facet.validation.AFCmModuleAbsentCollector");
    private static final LocalInspectionTool XML_CONFIG_INSPECTION = new AF5ConfigIsNotRegisteredInCmModule();
    private final DetectionExcludesConfiguration myDetectionExcludesConfiguration;
    private final InspectionProfile myProfile;
    private final boolean myCheckXml;
    private final Module[] myModules;
    private final Project myProject;
    private final Set<Module> myNotConfiguredStorage = new HashSet<>();

    AFCmModuleAbsentCollector(Module... modules) {
        this.myModules = modules;

        assert modules.length != 0;

        this.myProject = modules[0].getProject();
        this.myDetectionExcludesConfiguration = DetectionExcludesConfiguration.getInstance(this.myProject);
        InspectionProjectProfileManager profileManager = InspectionProjectProfileManager.getInstance(this.myProject);
        this.myProfile = profileManager.getCurrentProfile();
        this.myCheckXml = this.myProfile.isToolEnabled(HighlightDisplayKey.find(XML_CONFIG_INSPECTION.getID()));
    }

    boolean isEnabledInProject() {
        return this.myCheckXml;
    }

    void collect() {
        this.collect(new EmptyProgressIndicator());
    }

    void collect(ProgressIndicator indicator) {
        indicator.setText(AF5Bundle.message("af5.config.check", new Object[0]));
        long start = System.currentTimeMillis();
        LOG.debug("================= START ============ total modules #" + this.myModules.length);
        indicator.setIndeterminate(false);
        indicator.setText2("Searching for modules without 'cm-module.xml'");

        for(int i = 0; i < myModules.length; i++) {
            Module module = myModules[i];
            indicator.checkCanceled();
            if (this.myCheckXml && CmModuleUtils.isDependsOnAF5(module) && null == CmModuleUtils.getCmModuleFile(module)) {
                this.myNotConfiguredStorage.add(module);
            }
            indicator.setFraction((double)(i) / (double)this.myModules.length);
        }

        LOG.debug("================= END ============  total time: " + (System.currentTimeMillis() - start));
    }

    Set<Module> getResults() {
        return this.myNotConfiguredStorage;
    }

    private boolean skipConfigInspectionFor(PsiElement place, LocalInspectionTool tool) {
        HighlightDisplayKey toolHighlightDisplayKey = HighlightDisplayKey.find(tool.getID());
        return !this.myProfile.isToolEnabled(toolHighlightDisplayKey, place) || SuppressionUtil.inspectionResultSuppressed(place, tool);
    }

    static NullableFunction<VirtualFilePointer, PsiFile> getVirtualFileMapper(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        return (pointer) -> pointer.isValid() && pointer.getFile() != null ? psiManager.findFile(pointer.getFile()) : null;
    }
}
