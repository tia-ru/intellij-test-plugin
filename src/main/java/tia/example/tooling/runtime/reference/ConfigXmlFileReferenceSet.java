package tia.example.tooling.runtime.reference;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import tia.example.tooling.runtime.util.ConfigXmlUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ConfigXmlFileReferenceSet extends FileReferenceSet {

    private final boolean soft;

    public ConfigXmlFileReferenceSet(String str,
                                     @NotNull PsiElement element, int startInElement,
                                     @Nullable PsiReferenceProvider provider, boolean isCaseSensitive, boolean soft) {
        super(str, element, startInElement, provider, isCaseSensitive, true, new FileType[]{XmlFileType.INSTANCE});
        this.soft = soft;
    }

    @Override
    protected boolean isSoft() {
        return soft;
    }

    @Override
    protected boolean useIncludingFileAsContext() {
        return false;
    }

    @Override
    public boolean isAbsolutePathReference() {
        return true;
    }
    public boolean absoluteUrlNeedsStartSlash() {
        return false;
    }

    @NotNull
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
        Module module = ModuleUtil.findModuleForPsiElement(getElement());
        if (module == null) return Collections.emptyList();
        return getModuleResourceDirectories(module);
    }

    @Override
    protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
        return item -> {
            if (item instanceof PsiDirectory) return true;
            VirtualFile virtualFile = item.getVirtualFile();
            return virtualFile != null && ConfigXmlUtils.isAF5ConfigFile(virtualFile);
        };
    }

    static private Collection<PsiFileSystemItem> getModuleResourceDirectories(@NotNull Module module) {

        List<PsiFileSystemItem> result = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(module.getProject());
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        List<VirtualFile> resourceRoots = rootManager.getSourceRoots(JavaResourceRootType.RESOURCE);
        for (VirtualFile root : resourceRoots) {
            PsiDirectory directory = psiManager.findDirectory(root);
            if (directory != null){
                result.add(directory);
            }
        }
        return result;
    }
}
