package tia.example.tooling.runtime.validation;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.profile.ProfileChangeAdapter;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.util.RunnableCallable;
import com.intellij.util.concurrency.AppExecutorUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tia.example.tooling.runtime.inspections.AF5ConfigIsNotRegisteredInCmModule;
import tia.example.tooling.runtime.util.CmModuleUtils;


public class AF5ConfigurationCheckStartupActivity implements ProjectActivity {

    private static final String XML_CONFIG_INSPECTION_ID = new AF5ConfigIsNotRegisteredInCmModule().getID();


    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        runActivity(project);
        return null;
    }

    private void runActivity(@NotNull Project project) {
        /*MyFileEditorManagerListener listener = new MyFileEditorManagerListener();
        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, listener);*/

        Application application = ApplicationManager.getApplication();
        if (!application.isUnitTestMode() && !application.isHeadlessEnvironment()) {
            ReadAction.nonBlocking(new RunnableCallable(() -> {
                        //if (!DetectionExcludesConfiguration.getInstance(project).isExcludedFromDetection(SpringFrameworkDetector.getSpringFrameworkType())) {
                        if (ProjectInspectionProfileManager.getInstance(project).isCurrentProfileInitialized()) {
                            queueSmartTask(project);
                        } else {
                            project.getMessageBus().connect().subscribe(ProfileChangeAdapter.TOPIC, new ProfileChangeAdapter() {
                                public void profilesInitialized() {
                                    queueSmartTask(project);
                                }
                            });
                        }

                    }))
                    .inSmartMode(project)
                    .submit(AppExecutorUtil.getAppExecutorService());
        }
    }

    private static void queueSmartTask(Project project) {
        /*ApplicationManager.getApplication().invokeLater(() -> {
            (new AF5ConfigurationCheckTask(project)).queue();
        }, project.getDisposed());*/

        if (isInspectionEnabled(project) && CmModuleUtils.isDependsOnAF5(project)) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> (new AF5ConfigurationCheckTask(project)).queue());
        }
    }

    private static boolean isInspectionEnabled(Project project) {
        InspectionProjectProfileManager profileManager = InspectionProjectProfileManager.getInstance(project);
        InspectionProfileImpl profile = profileManager.getCurrentProfile();
        return profile.isToolEnabled(HighlightDisplayKey.find(XML_CONFIG_INSPECTION_ID));
    }
}
