package tia.example.tooling.runtime.validation;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.profile.ProfileChangeAdapter;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import tia.example.tooling.runtime.util.CmModuleUtils;

public class AF5ConfigurationCheckStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        /*MyFileEditorManagerListener listener = new MyFileEditorManagerListener();
        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, listener);*/

        Application application = ApplicationManager.getApplication();
        if (!application.isUnitTestMode() && !application.isHeadlessEnvironment()) {
            ReadAction.nonBlocking(() -> {
                //if (!DetectionExcludesConfiguration.getInstance(project).isExcludedFromDetection(SpringFrameworkDetector.getSpringFrameworkType())) {
                if (CmModuleUtils.isDependsOnAF5(project)) {
                    if (ProjectInspectionProfileManager.getInstance(project).isCurrentProfileInitialized()) {
                        queueTask(project);
                    } else {
                        project.getMessageBus().connect().subscribe(ProfileChangeAdapter.TOPIC, new ProfileChangeAdapter() {
                            public void profilesInitialized() {
                                queueTask(project);
                            }
                        });
                    }
                }

            }).inSmartMode(project).submit(AppExecutorUtil.getAppExecutorService());
        }
    }

    private static void queueTask(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            (new AF5ConfigurationCheckTask(project)).queue();
        }, project.getDisposed());
    }
}
