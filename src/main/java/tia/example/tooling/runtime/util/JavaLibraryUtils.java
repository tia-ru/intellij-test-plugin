package tia.example.tooling.runtime.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ConcurrentFactoryMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public final class JavaLibraryUtils {

    public static boolean hasLibraryClass(@Nullable Project project, @NotNull String classFqn) {
        return project != null && !project.isDisposed() ? getLibraryClassMap(project).getOrDefault(classFqn, false) : false;
    }

    public static boolean hasLibraryClass(@Nullable Module module, @NotNull String classFqn) {
        return module != null && !module.isDisposed() ? getLibraryClassMap(module).getOrDefault(classFqn, false) : false;
    }

    private static Map<String, Boolean> getLibraryClassMap(@NotNull Project project) {
        return DumbService.isDumb(project) ? Collections.emptyMap() : CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            DumbService dumbService = DumbService.getInstance(project);
            if (dumbService.isDumb()) {
                return Result.createSingleDependency(Collections.emptyMap(), dumbService.getModificationTracker());
            } else {
                ConcurrentMap<String, Boolean> map = ConcurrentFactoryMap.createMap((classFqn) -> JavaPsiFacade.getInstance(project).findClass(classFqn, GlobalSearchScope.allScope(project)) != null);
                return Result.create(map, new Object[]{PsiModificationTracker.MODIFICATION_COUNT, ProjectRootManager.getInstance(project)});
            }
        });
    }

    private static Map<String, Boolean> getLibraryClassMap(@NotNull Module module) {
        DumbService dumbService = DumbService.getInstance(module.getProject());
        return dumbService.isDumb()
                ? Collections.emptyMap()
                : CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> {
            if (dumbService.isDumb()) {
                return Result.createSingleDependency(Collections.emptyMap(), dumbService.getModificationTracker());
            } else {
                ConcurrentMap<String, Boolean> map = ConcurrentFactoryMap.createMap(classFqn ->
                        null != JavaPsiFacade.getInstance(module.getProject())
                                .findClass(classFqn, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
                return Result.create(map, new Object[]{PsiModificationTracker.MODIFICATION_COUNT, ProjectRootManager.getInstance(module.getProject())});
            }
        });
    }
}
