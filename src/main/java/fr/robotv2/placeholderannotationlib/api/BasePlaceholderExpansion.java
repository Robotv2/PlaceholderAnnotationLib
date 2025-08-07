package fr.robotv2.placeholderannotationlib.api;

import org.bukkit.OfflinePlayer;

public abstract class BasePlaceholderExpansion {

    private final PlaceholderAnnotationProcessor processor;

    protected BasePlaceholderExpansion(PlaceholderAnnotationProcessor processor) {
        this.processor = processor;
    }

    public String onRequest(OfflinePlayer player, String params) {
        return processor.process(player, params);
    }
}
