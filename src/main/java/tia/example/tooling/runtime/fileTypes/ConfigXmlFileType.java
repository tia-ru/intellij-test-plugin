package tia.example.tooling.runtime.fileTypes;

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.FileSystemInterface;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

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


    /** based on code in {@code com.intellij.openapi.fileTypes.impl.FileTypeManagerImpl#detectFromContent}
    */
    @Override
    public boolean isMyFileType(@NotNull VirtualFile file) {

        /** Does NOT strip the BOM from the beginning of the stream, unlike the {@link VirtualFile#getInputStream()} */
        try (InputStream inputStream = ((FileSystemInterface) file.getFileSystem()).getInputStream(file)) {
            int fileLength = (int) file.getLength();
            if (fileLength <= 0) return false;

            byte[] buffer = fileLength <= FileUtilRt.THREAD_LOCAL_BUFFER_LENGTH
                    ? FileUtilRt.getThreadLocalBuffer()
                    : new byte[Math.min(fileLength, FileUtilRt.getUserContentLoadLimit())];

            int n = readSafely(inputStream, buffer, 0, buffer.length);
            if (n < ConfigXmlUtils.NS_AF5_CONFIG.length() ) return false;
            // use PlainTextFileType because it doesn't supply its own charset detector
            // help set charset in the process to avoid double charset detection from content
            FileType ft =  LoadTextUtil.processTextFromBinaryPresentationOrNull(buffer, n,
                    file, true, true, PlainTextFileType.INSTANCE,
                    (@Nullable CharSequence text) -> {
                        if (text == null) return UnknownFileType.INSTANCE;
                        String str = text.toString();
                        if (!str.contains(ConfigXmlUtils.NS_AF5_CONFIG)) return UnknownFileType.INSTANCE;

                        return INSTANCE;
                    }
            );
            return ft == INSTANCE;

        } catch (IOException e) {
        }
        return false;
    }


    private int readSafely(@NotNull InputStream stream, @NotNull byte[] buffer, int offset, int length) throws IOException {
        int n = stream.read(buffer, offset, length);
        if (n <= 0) {
            // maybe locked because someone else is writing to it
            // repeat inside read action to guarantee all writes are finished
            n = ReadAction.compute(() -> stream.read(buffer, offset, length));
        }
        return n;
    }
}
