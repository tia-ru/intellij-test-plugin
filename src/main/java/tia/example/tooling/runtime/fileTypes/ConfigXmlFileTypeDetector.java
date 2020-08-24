package tia.example.tooling.runtime.fileTypes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

public class ConfigXmlFileTypeDetector implements FileTypeRegistry.FileTypeDetector {
    @Nullable
    @Override
    public FileType detect(@NotNull VirtualFile file, @NotNull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText) {
        if (firstCharsIfText == null) return null;
        String str = firstCharsIfText.toString();
        if (!str.contains(ConfigXmlUtils.NS_AF5_CONFIG)) return null;
        return ConfigXmlFileType.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

}
