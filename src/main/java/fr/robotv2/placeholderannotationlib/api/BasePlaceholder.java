package fr.robotv2.placeholderannotationlib.api;

import fr.robotv2.placeholderannotationlib.annotations.Placeholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public interface BasePlaceholder {

    @Nullable("May return null if the placeholder is the default placeholder. Check isDefault() first.")
    Placeholder getPlaceholder();

    boolean isDefault();

    default boolean isDirect() {
        return false;
    }

    boolean requiresOnlinePlayer();

    String process(OfflinePlayer player, String[] params);
}
