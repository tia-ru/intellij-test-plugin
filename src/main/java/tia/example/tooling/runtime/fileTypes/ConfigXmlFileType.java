package tia.example.tooling.runtime.fileTypes;

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConfigXmlFileType extends XmlLikeFileType implements FileTypeIdentifiableByVirtualFile {

    // is used in plugin.xml
    public static final ConfigXmlFileType INSTANCE = new ConfigXmlFileType();

    @NonNls
    public static final String DEFAULT_EXTENSION = "xml";

    private ConfigXmlFileType() {
        super(ConfigXmlLanguage.INSTANCE);
    }

    @Override
    @NotNull
    public String getName() {
        return "AF5 Config";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "AF5 platform configuration";
    }

    @Override
    @NotNull
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Custom;
    }


    @Override
    public boolean isMyFileType(@NotNull VirtualFile file) {
        return false;
    /*    PsiManager psiManager = PsiManager.getInstance(project);
        final PsiFile psiFile = psiManager.findFile(file);
        return ConfigXmlUtils.isAF5ConfigFile();*/
    }
}
