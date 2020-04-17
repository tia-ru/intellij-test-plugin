package tia.example.tooling.runtime.balloon;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DocumentManagerListener implements FileDocumentManagerListener {
    @Override
    public void beforeDocumentSaving(@NotNull Document document) {

    }

    @Override
    public void fileContentReloaded(@NotNull VirtualFile file, @NotNull Document document) {

    }

    @Override
    public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {

        /*final Project project = ProjectLocator.getInstance().guessProjectForFile(file);
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder("Do you want to add this file to AF5 Module config? <a href=\"add\">Add</a>",
                        MessageType.WARNING , null)
                .setHideOnLinkClick(true)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);*/

    }

}
