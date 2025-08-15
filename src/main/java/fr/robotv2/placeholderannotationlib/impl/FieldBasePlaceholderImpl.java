package fr.robotv2.placeholderannotationlib.impl;

import fr.robotv2.placeholderannotationlib.annotations.Placeholder;
import fr.robotv2.placeholderannotationlib.annotations.RequireOnlinePlayer;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholder;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class FieldBasePlaceholderImpl implements BasePlaceholder {

    private final BasePlaceholderExpansion expansion;
    private final Field field;
    private final Placeholder placeholder;
    private final boolean isDefault;
    private final boolean requiresOnline;

    public FieldBasePlaceholderImpl(
            @NotNull BasePlaceholderExpansion expansion,
            @NotNull Field field,
            @Nullable Placeholder placeholder,
            boolean isDefault)
    {
        this.expansion = expansion;
        this.field = field;
        this.placeholder = placeholder;
        this.isDefault = isDefault;
        this.requiresOnline = field.isAnnotationPresent(RequireOnlinePlayer.class);
    }

    @Override
    public Placeholder getPlaceholder() {
        return placeholder;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean requiresOnlinePlayer() {
        return requiresOnline;
    }

    @Override
    public String process(OfflinePlayer player, String[] params) {
        try {
            final Object value = field.get(expansion);
            return value instanceof String ? (String) value : String.valueOf(value);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}
