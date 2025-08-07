package fr.robotv2.placeholderannotationlib.impl;

import fr.robotv2.placeholderannotationlib.annotations.DefaultPlaceholder;
import fr.robotv2.placeholderannotationlib.annotations.Placeholder;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholder;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholderExpansion;
import fr.robotv2.placeholderannotationlib.api.PlaceholderAnnotationProcessor;
import fr.robotv2.placeholderannotationlib.api.ValueResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PlaceholderAnnotationProcessorImpl implements PlaceholderAnnotationProcessor {

    private final String separator;
    private final boolean debug;
    private final Logger logger;

    private BasePlaceholder defaultPlaceholder;
    private final Map<Class<?>, ValueResolver<?>> resolvers;
    private final Map<String, BasePlaceholder> placeholders;

    public PlaceholderAnnotationProcessorImpl(String separator, Logger logger, boolean debug) {
        this.separator = separator;
        this.debug = debug;
        this.logger = logger;
        this.defaultPlaceholder = null;
        this.resolvers = new HashMap<>();
        this.placeholders = new HashMap<>();
        registerDefaultValueResolvers();
    }

    @Override
    public String separator() {
        return separator;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    public void debug(String message) {
        if (debug) {
            logger.info("[DEBUG] " + message);
        }
    }

    @Override
    public String process(OfflinePlayer player, String params) {
        if (params == null || params.isEmpty()) {
            if (defaultPlaceholder != null) {
                debug("Using default placeholder for empty params.");
                if (defaultPlaceholder.requiresOnlinePlayer()) {
                    if (!(player instanceof Player) || !player.isOnline()) {
                        debug("Default placeholder requires online player but player is offline.");
                        return "";
                    }
                }
                return defaultPlaceholder.process(player, new String[0]);
            }
            return "";
        }

        String[] parts = params.split(Pattern.quote(separator()));
        String matchedId = null;
        BasePlaceholder matchedPlaceholder = null;
        int matchedLength = 0;

        // Longest match search
        for (int i = parts.length; i > 0; i--) {
            String candidateId = String.join(separator(), Arrays.copyOfRange(parts, 0, i)).toLowerCase();
            if (placeholders.containsKey(candidateId)) {
                matchedId = candidateId;
                matchedPlaceholder = placeholders.get(candidateId);
                matchedLength = i;
                break;
            }
        }

        if (matchedPlaceholder == null) {
            debug("No placeholder found for: " + params);
            if (defaultPlaceholder != null) {
                return defaultPlaceholder.process(player, parts); // ✅ Always call with all parts
            }
            return "";
        }

        // RequireOnlinePlayer check
        if (matchedPlaceholder.requiresOnlinePlayer()) {
            if (!(player instanceof Player) || !((Player) player).isOnline()) {
                debug("Placeholder requires online player: " + matchedId);
                return "";
            }
        }

        String[] args = Arrays.copyOfRange(parts, matchedLength, parts.length);
        return matchedPlaceholder.process(player, args);
    }

    @Override
    public <T> void registerValueResolver(Class<? extends T> tClass, ValueResolver<? extends T> resolver) {
        resolvers.put(tClass, resolver);
    }

    @Override
    public void registerExpansion(BasePlaceholderExpansion expansion) {
        debug("Registering expansion: " + expansion.getClass().getName());

        for (Method method : expansion.getClass().getDeclaredMethods()) {
            processAccessible(expansion, method);
        }
        for (Field field : expansion.getClass().getDeclaredFields()) {
            processAccessible(expansion, field);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ValueResolver<T> getValueResolver(Class<T> clazz) {
        return (ValueResolver<T>) resolvers.get(clazz);
    }

    private void processAccessible(BasePlaceholderExpansion expansion, AccessibleObject accessible) {
        final Placeholder placeholder = accessible.getAnnotation(Placeholder.class);
        final boolean isDefault = accessible.isAnnotationPresent(DefaultPlaceholder.class);

        if (placeholder == null && !isDefault) {
            return; // Skip if neither @Placeholder nor @DefaultPlaceholder
        }

        BasePlaceholder placeholderImpl;

        if (accessible instanceof Field) {
            Field field = (Field) accessible;
            field.setAccessible(true);
            placeholderImpl = new FieldBasePlaceholderImpl(expansion, field, placeholder, isDefault);
        } else if (accessible instanceof Method) {
            Method method = (Method) accessible;
            method.setAccessible(true);
            placeholderImpl = new MethodBasePlaceholderImpl(this, expansion, method, placeholder, isDefault);
        } else {
            return;
        }

        if (isDefault) {
            if (this.defaultPlaceholder != null) {
                throw new IllegalStateException("Only one @DefaultPlaceholder allowed per processor.");
            }
            this.defaultPlaceholder = placeholderImpl;
            debug("Registered default placeholder: " + accessible);
        }

        if (placeholder != null) {
            String joinedId = String.join(separator(), placeholder.value()).toLowerCase();
            if (placeholders.containsKey(joinedId)) {
                logger.warning("Duplicate placeholder id: " + joinedId);
            }
            placeholders.put(joinedId, placeholderImpl);
            debug("Registered placeholder: " + joinedId);
        }
    }

    private void registerDefaultValueResolvers() {
        registerValueResolver(String.class, (issuer, param) -> param);
        registerValueResolver(Integer.class, (issuer, param) -> Integer.parseInt(param));
        registerValueResolver(int.class, (issuer, param) -> Integer.parseInt(param));
        registerValueResolver(Long.class, (issuer, param) -> Long.parseLong(param));
        registerValueResolver(long.class, (issuer, param) -> Long.parseLong(param));
        registerValueResolver(Double.class, (issuer, param) -> Double.parseDouble(param));
        registerValueResolver(double.class, (issuer, param) -> Double.parseDouble(param));
        registerValueResolver(Float.class, (issuer, param) -> Float.parseFloat(param));
        registerValueResolver(float.class, (issuer, param) -> Float.parseFloat(param));
        registerValueResolver(Byte.class, (issuer, param) -> Byte.parseByte(param));
        registerValueResolver(byte.class, (issuer, param) -> Byte.parseByte(param));
        registerValueResolver(Boolean.class, (issuer, param) -> Boolean.parseBoolean(param));
        registerValueResolver(boolean.class, (issuer, param) -> Boolean.parseBoolean(param));
        registerValueResolver(Character.class, (issuer, param) -> param.charAt(0));
        registerValueResolver(char.class, (issuer, param) -> param.charAt(0));
        registerValueResolver(Short.class, (issuer, param) -> Short.parseShort(param));
        registerValueResolver(short.class, (issuer, param) -> Short.parseShort(param));
        registerValueResolver(Player.class, (issuer, param) -> Bukkit.getPlayer(param));
        registerValueResolver(OfflinePlayer.class, (issuer, param) -> {
            final Player player = Bukkit.getPlayer(param);
            if (player != null && player.isOnline()) {
                return player;
            } else {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(param);
                return offlinePlayer.hasPlayedBefore() ? offlinePlayer : null;
            }
        });
        registerValueResolver(World.class, (issuer, param) -> Bukkit.getWorld(param));
    }
}
