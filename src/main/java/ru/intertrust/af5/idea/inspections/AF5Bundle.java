package ru.intertrust.af5.idea.inspections;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class AF5Bundle extends AbstractBundle {
    @NonNls public static final String BUNDLE = "messages.InspectionsBundle";
    private static final AF5Bundle INSTANCE = new AF5Bundle();

    private AF5Bundle() { super(BUNDLE); }

    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    /*@NotNull
    public static Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }*/
}