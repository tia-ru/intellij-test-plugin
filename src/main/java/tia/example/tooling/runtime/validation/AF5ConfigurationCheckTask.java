package tia.example.tooling.runtime.validation;

import com.intellij.diagnostic.PerformanceWatcher;
import com.intellij.diagnostic.PerformanceWatcher.Snapshot;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener.Adapter;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tia.example.tooling.runtime.inspections.AF5Bundle;
import tia.example.tooling.runtime.util.CmModuleUtils;

import javax.swing.event.HyperlinkEvent;
import java.util.Collections;
import java.util.Set;

class AF5ConfigurationCheckTask extends Backgroundable {
    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("AF5", NotificationDisplayType.STICKY_BALLOON, true);
    private volatile Set<Module> myUnmappedConfigurations;

    AF5ConfigurationCheckTask(Project project) {
        super(project, AF5Bundle.message("af5.config.check", new Object[0]));
    }

    public void run(@NotNull ProgressIndicator indicator) {
        ReadAction.nonBlocking(() -> {
            this.runCollectors(ProgressIndicatorProvider.getGlobalProgressIndicator());
        }).inSmartMode(this.myProject).wrapProgress(indicator).executeSynchronously();
    }

    private void runCollectors(ProgressIndicator indicator) {
        Snapshot snapshot = PerformanceWatcher.takeSnapshot();
        Module[] modules = ModuleManager.getInstance(this.getProject()).getModules();
        if (modules.length != 0) {
            AFCmModuleAbsentCollector unmappedCollector = new AFCmModuleAbsentCollector(modules);
            if (unmappedCollector.isEnabledInProject()) {
                unmappedCollector.collect(indicator);
                this.myUnmappedConfigurations = unmappedCollector.getResults();
            }
            if (ApplicationManager.getApplication().isInternal()) {
                snapshot.logResponsivenessSinceCreation("AF5 Config Check [" + modules.length + " modules]");
            }
        }
    }

    public void onSuccess() {

        if (this.getProject().isDisposed()) {
            return;
        }
        Set<Module> unmappedResults = this.myUnmappedConfigurations == null ? Collections.emptySet() : this.myUnmappedConfigurations;
        if (!unmappedResults.isEmpty()) {
            StringBuilder notification = new StringBuilder();
            if (!unmappedResults.isEmpty()) {

                notification
                        .append(AF5Bundle.message("af5.config.check.cmmodule.absent"))
                        .append("<br/>");
                for (Module module : unmappedResults) {
                    String moduleName = module.getName();
                    notification.append("<br/>");
                    notification.append("<a href=\"config#").append(moduleName).append("\">").append(moduleName).append("</a>");
                }
            }

            this.createNotification(notification.toString(), unmappedResults)
                    /*.addAction(new NotificationAction(AF5Bundle.message("spring.facet.validation.disable.action", new Object[0])) {
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {

                            int result = Messages.showYesNoDialog(AF5ConfigurationCheckTask.this.getProject(),
                                    AF5Bundle.message("spring.facet.detection.will.be.disabled.for.whole.project", new Object[0]),
                                    AF5Bundle.message("spring.facet.config.detection", new Object[0]),
                                    AF5Bundle.message("spring.facet/detection.disable.detection", new Object[0]),
                                    CommonBundle.getCancelButtonText(),
                                    Messages.getWarningIcon());
                            if (result == 0) {
                                //DetectionExcludesConfiguration detectionExcludesConfiguration = DetectionExcludesConfiguration.getInstance(getProject());
                                //detectionExcludesConfiguration.addExcludedFramework(SpringFrameworkDetector.getSpringFrameworkType());
                                notification.hideBalloon();
                            }

                        }
                    })*/
                    .setIcon(AllIcons.Ide.ConfigFile).notify(this.getProject());
        }
    }

    @NotNull
    private Notification createNotification(String notificationText, Set<Module> unmappedConfigurations) {
        Notification notification = NOTIFICATION_GROUP.createNotification("AF5 Configuration Check",
                notificationText,
                NotificationType.WARNING,
                new AF5ConfigurationCheckTask.UnmappedConfigurationsNotificationAdapter(this.getProject(),
                        unmappedConfigurations));

        return notification;
    }

   /* private static Pair<Module, Collection<VirtualFilePointer>> createPair(Entry<Module, Collection<PsiFile>> entry) {
        List<VirtualFilePointer> files = ContainerUtil.map(entry.getValue(), (file) -> {
            return VirtualFilePointerManager.getInstance().create(
                    file.getVirtualFile(),
                    SpringManager.getInstance(file.getProject()),
                    (VirtualFilePointerListener) null);
        });
        return Pair.create((Module) entry.getKey(), files);
    }

    private static MultiMap<Module, VirtualFilePointer> filterEmptyConfigurations(Project project, Pair<Module, Collection<VirtualFilePointer>>[] unmappedConfigurations) {
        NullableFunction<VirtualFilePointer, PsiFile> virtualFileMapper = AFCmModuleAbsentCollector.getVirtualFileMapper(project);
        MultiMap<Module, VirtualFilePointer> result = new MultiMap();
        Pair[] var4 = unmappedConfigurations;
        int var5 = unmappedConfigurations.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Pair<Module, Collection<VirtualFilePointer>> entry = var4[var6];
            if (!((Collection) entry.second).isEmpty()) {
                List<VirtualFilePointer> pointers = ContainerUtil.filter(entry.second, (pointer) -> {
                    return virtualFileMapper.fun(pointer) != null;
                });
                if (!pointers.isEmpty()) {
                    result.put((Module) entry.first, pointers);
                }
            }
        }

        return result;
    }*/

    private static class UnmappedConfigurationsNotificationAdapter extends Adapter {
        private final Project myProject;
        private final Set<Module> myUnmappedConfigurations;

        private UnmappedConfigurationsNotificationAdapter(Project project, Set<Module> unmappedConfigurations) {
            this.myProject = project;
            this.myUnmappedConfigurations = unmappedConfigurations;
        }

        protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
            if (this.myProject.isDisposed()) {
                return;
            }

            String description = e.getDescription();
            String navigationTarget = StringUtil.substringAfter(description, "#");

            assert navigationTarget != null;

            Module module;
            if (description.startsWith("config")) {
                module = findModuleByName(navigationTarget);
                if (module == null) {
                    return;
                }
                CmModuleUtils.createCmModuleFile(module, true);

                this.updateNotification(notification);

            } else if (description.startsWith("files")) {

                module = findModuleByName(navigationTarget);
                if (module == null) {
                    return;
                }

                /*NullableFunction<VirtualFilePointer, PsiFile> virtualFileMapper = AFCmModuleAbsentCollector.getVirtualFileMapper(module.getProject());
                Collection<PsiFile> files = ContainerUtil.mapNotNull(this.myUnmappedConfigurations.get(module), virtualFileMapper);
                if (files.isEmpty()) {
                    JBPopupFactory.getInstance().createMessage(SpringBundle.message("config.files.not.found", new Object[0])).showInFocusCenter();
                    return;
                }*/

                /*JBPopup popup = NavigationUtil.getPsiElementPopup((PsiElement[]) files.toArray(PsiFile.EMPTY_ARRAY), SpringBundle.message("config.unmapped.configs.popup.title", navigationTarget));
                Object event = e.getSource();
                if (event instanceof Component) {
                    popup.showInCenterOf((Component) event);
                } else {
                    popup.showInFocusCenter();
                }*/
            }

        }

        private void updateNotification(@NotNull Notification notification) {

            notification.expire();
            ApplicationManager.getApplication().invokeLater(() -> {
                (new AF5ConfigurationCheckTask(this.myProject)).queue();
            }, this.myProject.getDisposed());
        }

        @Nullable
        private Module findModuleByName(String navigationTarget) {
            return ModuleManager.getInstance(this.myProject).findModuleByName(navigationTarget);
        }
    }
}
