package fr.robotv2.placeholderannotationlib.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class BasePlaceholderExpansion extends PlaceholderExpansion {

    private final PlaceholderAnnotationProcessor processor;

    protected BasePlaceholderExpansion(PlaceholderAnnotationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return processor.process(player, params);
    }
}
