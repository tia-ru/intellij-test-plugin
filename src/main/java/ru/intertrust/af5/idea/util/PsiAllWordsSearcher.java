package ru.intertrust.af5.idea.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Process project files.  Does not search in library files.
 */
public class PsiAllWordsSearcher {
    private final GlobalSearchScope searchScope;
    private final boolean caseSensitively;
    private final PsiSearchHelper searchHelper;

    /**
     * @param searchScope     the scope in which occurrences are searched.
     * @param caseSensitively if words differing in the case only should not be considered equal
     */
    public PsiAllWordsSearcher(GlobalSearchScope searchScope, boolean caseSensitively) {
        this.searchScope = searchScope;
        this.caseSensitively = caseSensitively;
        Project project = searchScope.getProject();
        searchHelper = PsiSearchHelper.getInstance(project);
    }

    /**
     * Passes all files containing the all words is specified string in {@link UsageSearchContext#IN_PLAIN_TEXT plain
     * text} context to the specified processor. Additional check is required to search for {@code stringWithWords}
     * exactly. Does not search in library files.
     *
     * @param stringWithWords a string to split to words to search.
     * @param processor       the processor which accepts the references.
     * @return false if the search is interrupted by {@code processor}
     */
    boolean processFilesWithAllWordsInText(@NotNull String stringWithWords,
                                           @NotNull Predicate<PsiFile> fileFilter,
                                           @NotNull Processor<PsiFile> processor
    ) {



        final List<String> words = StringUtil.getWordsIn(stringWithWords);
        if (words.isEmpty()) {
            return false;
        }
        Collections.sort(words, (o1, o2) -> o2.length() - o1.length());

        final Set<PsiFile> files = new HashSet<>(128);
        boolean[] isFirstWord = new boolean[]{true};

        for (String word : words) {
            
            searchHelper.processAllFilesWithWordInText(word, searchScope, psiFile -> {
                if (!isFirstWord[0] && !files.contains(psiFile)) {
                    return true;
                }
                if (!fileFilter.test(psiFile)) {
                    return true;
                }
                files.add(psiFile);
                return true;
            }, caseSensitively);
            isFirstWord[0] = false;
        }
        for (PsiFile psiFile : files) {
            if (!processor.process(psiFile)) {
                return false;
            }
        }
        return true;
    }
}
