package tia.example.tooling.runtime.balloon;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import tia.example.tooling.runtime.util.CmModuleUtils;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

public class MyFileEditorManagerListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        Project project = source.getProject();
        if (!ConfigXmlUtils.isAF5ConfigFile(file)) return;
        Module module = ModuleUtil.findModuleForFile(file, project);
        if (module == null || CmModuleUtils.isRegistered(file, module)) return;

        String msg = "AF5 configuration file '" + file.getName() + "' is not registered in cm-module.xml";
        Notification notification = new Notification("AF5", "AF5 Configuration", msg, NotificationType.INFORMATION);
        NotificationAction action = NotificationAction.createSimple("Add to cm-module.xml", () -> {
            notification.expire();
            CmModuleUtils.addToCmModule(file, module);
        });
        notification.addAction(action);
        notification.notify(project);

        /*Editor editor = source.getSelectedTextEditor();
        HintManager.getInstance().showInformationHint(editor,"hint");*/

        /*StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder("Do you want to add this file to AF5 Module config? <a href=\"add\">Add</a>",
                        MessageType.WARNING , null)
                .setHideOnLinkClick(true)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);*/

    }
}
