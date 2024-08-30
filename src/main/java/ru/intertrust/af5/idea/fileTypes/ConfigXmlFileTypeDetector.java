package ru.intertrust.af5.idea.fileTypes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.intertrust.af5.idea.util.ConfigXmlUtils;

public class ConfigXmlFileTypeDetector implements FileTypeRegistry.FileTypeDetector {
    @Nullable
    @Override
    public FileType detect(@NotNull VirtualFile file, @NotNull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText) {
        if (firstCharsIfText == null) return null; //"xml".equals(psiFile.getVirtualFile().getExtension())
        String str = firstCharsIfText.toString();
        if (!str.contains(ConfigXmlUtils.NS_AF5_CONFIG)) return null;
        return ConfigXmlFileType.INSTANCE;
    }

}
