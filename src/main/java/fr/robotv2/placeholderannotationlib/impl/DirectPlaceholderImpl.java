package fr.robotv2.placeholderannotationlib.impl;

import fr.robotv2.placeholderannotationlib.annotations.Placeholder;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholder;
import fr.robotv2.placeholderannotationlib.api.PlaceholderActor;
import org.bukkit.OfflinePlayer;

import java.util.function.Function;

public class DirectPlaceholderImpl implements BasePlaceholder {

    private final Function<PlaceholderActor, String> function;
    private final boolean requiresOnlinePlayer;

    public DirectPlaceholderImpl(Function<PlaceholderActor, String> function) {
        this(function, false);
    }

    public DirectPlaceholderImpl(Function<PlaceholderActor, String> function, boolean requiresOnlinePlayer) {
        this.function = function;
        this.requiresOnlinePlayer = requiresOnlinePlayer;
    }

    @Override
    public Placeholder getPlaceholder() {
        return null;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public String process(OfflinePlayer player, String[] args) {
        PlaceholderActor actor = new PlaceholderActorImpl(player);
        return function.apply(actor);
    }

    @Override
    public boolean requiresOnlinePlayer() {
        return requiresOnlinePlayer;
    }
}