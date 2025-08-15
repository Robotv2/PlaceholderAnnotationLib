package fr.robotv2.placeholderannotationlib.api;

import fr.robotv2.placeholderannotationlib.annotations.Expansion;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class BasePlaceholderExpansion extends PlaceholderExpansion {

    private final PlaceholderAnnotationProcessor processor;

    private final Expansion expansion;

    protected BasePlaceholderExpansion(PlaceholderAnnotationProcessor processor) {
        this.processor = processor;
        this.expansion = getClass().getAnnotation(Expansion.class);
        processor.registerExpansion(this);
    }

    @Override
    public @NotNull String getIdentifier() {
        assert expansion != null : "Please override getIdentifier() in your expansion class or add @Expansion annotation with identifier to your expansion class";
        assert expansion.identifier() != null : "Please add a valid identifier to your expansion class";
        return expansion.identifier();
    }

    @Override
    public @NotNull String getVersion() {
        assert expansion != null : "Please override getVersion() in your expansion class or add @Expansion annotation with version to your expansion class";
        assert expansion.version() != null : "Please add a valid version to your expansion class";
        return expansion.version();
    }

    @Override
    public @NotNull String getAuthor() {
        assert expansion != null : "Please override getAuthor() in your expansion class or add @Expansion annotation with author to your expansion class";
        return String.join(", ", expansion.author());
    }

    @Override
    public boolean persist() {
        return expansion.persist();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return processor.process(player, params);
    }
}
