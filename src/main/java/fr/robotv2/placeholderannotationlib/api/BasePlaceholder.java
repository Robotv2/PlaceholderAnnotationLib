package fr.robotv2.placeholderannotationlib.api;

import fr.robotv2.placeholderannotationlib.annotations.Placeholder;
import org.bukkit.OfflinePlayer;

public interface BasePlaceholder {

    Placeholder getPlaceholder();

    String process(OfflinePlayer player, String[] params);
}
