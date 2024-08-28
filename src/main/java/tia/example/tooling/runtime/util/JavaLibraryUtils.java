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

    public static boolean hasClass(@Nullable Project project, @NotNull String classFqn) {
        //return project != null && !project.isDisposed() ? getLibraryClassMap(project).getOrDefault(classFqn, false) : false;
        if (project == null || project.isDisposed()) {
            return false;
        }
        Map<String, Boolean> lazyMap = getClassExistenceLazyMap(project);
        Boolean hasClass = lazyMap.get(classFqn);
        if (hasClass == null) {
            lazyMap.remove(classFqn); //Заставляет перевычислить кэшированное значений
            hasClass = lazyMap.getOrDefault(classFqn, false);
        }
        return hasClass;
    }

    public static boolean hasClass(@Nullable Module module, @NotNull String classFqn) {
        if (module == null || module.isDisposed()) {
            return false;
        }
        Map<String, Boolean> lazyMap = getClassExistenceLazyMap(module);
        Boolean hasClass = lazyMap.get(classFqn);
        if (hasClass == null) {
            lazyMap.remove(classFqn); //Заставляет перевычислить кэшированное значений
            hasClass = lazyMap.getOrDefault(classFqn, false);
        }
        return hasClass;
    }

    private static Map<String, Boolean> getClassExistenceLazyMap(@NotNull Project project) {
        if (DumbService.isDumb(project)) return Collections.emptyMap();

        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ConcurrentMap<String, Boolean> map = ConcurrentFactoryMap.createMap( classFqn -> {
                if (DumbService.isDumb(project)) {
                    return null;
                } else {
                    return null != JavaPsiFacade.getInstance(project).findClass(classFqn, GlobalSearchScope.allScope(project));
                }
            });
            return Result.create(map, PsiModificationTracker.MODIFICATION_COUNT, ProjectRootManager.getInstance(project));
        });
    }

    private static Map<String, Boolean> getClassExistenceLazyMap(@NotNull Module module) {
        if (DumbService.isDumb(module.getProject())) return Collections.emptyMap();
        return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> {
            ConcurrentMap<String, Boolean> map = ConcurrentFactoryMap.createMap(classFqn -> {
                if (DumbService.isDumb(module.getProject())) {
                    return null;
                }
                return null != JavaPsiFacade.getInstance(module.getProject())
                        .findClass(classFqn, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
            });
            return Result.create(map, PsiModificationTracker.MODIFICATION_COUNT, ProjectRootManager.getInstance(module.getProject()));
        });
    }
}
