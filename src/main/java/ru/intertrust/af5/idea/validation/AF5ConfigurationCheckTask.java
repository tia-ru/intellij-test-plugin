package ru.intertrust.af5.idea.validation;

import com.intellij.diagnostic.PerformanceWatcher;
import com.intellij.diagnostic.PerformanceWatcher.Snapshot;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationListener.Adapter;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.intertrust.af5.idea.inspections.AF5Bundle;
import ru.intertrust.af5.idea.util.CmModuleUtils;

import javax.swing.event.HyperlinkEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

class AF5ConfigurationCheckTask extends Backgroundable {
    //private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("AF5", NotificationDisplayType.STICKY_BALLOON, true);

    AF5ConfigurationCheckTask(Project project) {
        super(project, AF5Bundle.message("af5.config.check", new Object[0]));
    }

    public void run(@NotNull ProgressIndicator indicator) {
        /*ReadAction.nonBlocking(() -> this.runCollectors(ProgressIndicatorProvider.getGlobalProgressIndicator()))
                .inSmartMode(this.myProject)
                //.wrapProgress(indicator).executeSynchronously()
        ;*/
        //ProgressIndicatorProvider.getGlobalProgressIndicator();
        ReadAction.run(() -> this.runCollectors(indicator));
    }

    private void runCollectors(ProgressIndicator indicator) {
        Map<Module, Collection<VirtualFile>> modulesWithoutCmModule;
        Snapshot snapshot = null;
        
        if (ApplicationManager.getApplication().isInternal()) {
            snapshot = PerformanceWatcher.takeSnapshot();
        }
        Module[] modules = ModuleManager.getInstance(this.getProject()).getModules();
        if (modules.length != 0) {
            AFCmModuleAbsentCollector modulesCollector = new AFCmModuleAbsentCollector(modules);
            modulesCollector.collect(indicator);
            modulesWithoutCmModule = modulesCollector.getResults();

            if (this.getProject().isDisposed()) {
                return;
            }
            if (!modulesWithoutCmModule.isEmpty()) {
                createNotification(modulesWithoutCmModule).notify(this.getProject());
            }
        }

        if (snapshot != null) {
            snapshot.logResponsivenessSinceCreation("AF5 Config Check [" + modules.length + " modules]");
        }
    }

/*    public void onSuccess() {

        if (this.getProject().isDisposed()) {
            return;
        }
        if (!modulesWithoutCmModule.isEmpty()) {
            createNotification(new HashSet<>(modulesWithoutCmModule)).notify(this.getProject());
        }
    }*/

    @NotNull
    private Notification createNotification(Map<Module, Collection<VirtualFile>> modulesWithoutCmModule) {

        NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("AF5");
        Notification notification = notificationGroup.createNotification(
                //"AF5 Configuration Check",
                AF5Bundle.message("af5.config.check.cmmodule.absent.title"),
                AF5Bundle.message("af5.config.check.cmmodule.absent"),
                NotificationType.INFORMATION)
            //.setIcon(AllIcons.Ide.Notification.WarningEvents)
            .setDropDownText("Ещё...");

        if (modulesWithoutCmModule.size() > 1) {
            notification.addAction(NotificationAction.createSimple("All", () -> {
                for (Map.Entry<Module, Collection<VirtualFile>> entry : modulesWithoutCmModule.entrySet()) {
                    Module module = entry.getKey();
                    Collection<VirtualFile> af5ConfigXmlFiles = entry.getValue();
                    CmModuleUtils.createCmModuleFile(module, af5ConfigXmlFiles, false);
                }
                notification.expire();
            }));
        }

        for (Map.Entry<Module, Collection<VirtualFile>> entry : modulesWithoutCmModule.entrySet()) {
            Module module = entry.getKey();
            Collection<VirtualFile> af5ConfigXmlFiles = entry.getValue();

            NotificationAction action = NotificationAction.createSimple(module.getName(), () -> {
                CmModuleUtils.createCmModuleFile(module, af5ConfigXmlFiles, true);
                updateNotification(notification);
            });
            notification.addAction(action);
        }

        return notification;
    }

    private void updateNotification(@NotNull Notification notification) {

        notification.expire();
        ApplicationManager.getApplication().invokeLater(() -> {
            (new AF5ConfigurationCheckTask(this.myProject)).queue();
        }, this.myProject.getDisposed());
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
