package tia.example.tooling.runtime.validation;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VirtualFile;
import tia.example.tooling.runtime.inspections.AF5Bundle;
import tia.example.tooling.runtime.util.CmModuleUtils;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class AFCmModuleAbsentCollector {
    private static final Logger LOG = Logger.getInstance("#com.intellij.spring.facet.validation.AFCmModuleAbsentCollector");
    //private static final LocalInspectionTool XML_CONFIG_INSPECTION = new AF5ConfigIsNotRegisteredInCmModule();
    //private final DetectionExcludesConfiguration myDetectionExcludesConfiguration;
    //private final InspectionProfile myProfile;
    //private final boolean myCheckXml;
    private final Module[] myModules;
    //private final Project myProject;
    private final Map<Module, Collection<VirtualFile>> myNotConfiguredStorage = new HashMap<>();

    AFCmModuleAbsentCollector(Module... modules) {
        this.myModules = modules;

        assert modules.length != 0;

        //this.myProject = modules[0].getProject();
        //this.myDetectionExcludesConfiguration = DetectionExcludesConfiguration.getInstance(this.myProject);

        /*InspectionProjectProfileManager profileManager = InspectionProjectProfileManager.getInstance(this.myProject);
        this.myProfile = profileManager.getCurrentProfile();
        this.myCheckXml = this.myProfile.isToolEnabled(HighlightDisplayKey.find(XML_CONFIG_INSPECTION.getID()));*/
    }

/*    boolean isEnabledInProject() {
        return this.myCheckXml;
    }*/

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
            indicator.setFraction((double)(i) / (double)this.myModules.length);
            //if (CmModuleUtils.isDependsOnAF5(module) && null == CmModuleUtils.getCmModuleFile(module)) {
            if (null == CmModuleUtils.getCmModuleFile(module)) {
                Collection<VirtualFile> af5ConfigFiles = ConfigXmlUtils.searchAF5ConfigFiles(module);
                if (!af5ConfigFiles.isEmpty()) {
                    this.myNotConfiguredStorage.put(module, af5ConfigFiles);
                }
            }
        }

        LOG.debug("================= END ============  total time: " + (System.currentTimeMillis() - start));
    }

    Map<Module, Collection<VirtualFile>> getResults() {
        return this.myNotConfiguredStorage;
    }

    /*private boolean skipConfigInspectionFor(PsiElement place, LocalInspectionTool tool) {
        HighlightDisplayKey toolHighlightDisplayKey = HighlightDisplayKey.find(tool.getID());
        return !this.myProfile.isToolEnabled(toolHighlightDisplayKey, place) || SuppressionUtil.inspectionResultSuppressed(place, tool);
    }*/

   /* static NullableFunction<VirtualFilePointer, PsiFile> getVirtualFileMapper(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        return (pointer) -> pointer.isValid() && pointer.getFile() != null ? psiManager.findFile(pointer.getFile()) : null;
    }*/
}
