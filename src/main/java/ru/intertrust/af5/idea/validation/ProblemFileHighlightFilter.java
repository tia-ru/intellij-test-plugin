package ru.intertrust.af5.idea.validation;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import ru.intertrust.af5.idea.util.ConfigXmlUtils;

/**
 * Возвращает false если файл точно не следует подсвечивать.
 * Чтобы файл подсветился как содержащий ошибку, метод должен вернуть true
 * и файл должен содержать Highlight с уровнем ERROR
 */
public class ProblemFileHighlightFilter implements Condition<VirtualFile> {
    private final Project myProject;

    public ProblemFileHighlightFilter(Project project) {
        this.myProject = project;
    }

    @Override
    public boolean value(VirtualFile file) {
        return ReadAction.compute(() -> ConfigXmlUtils.isAF5ConfigFile(file));

    }
}
