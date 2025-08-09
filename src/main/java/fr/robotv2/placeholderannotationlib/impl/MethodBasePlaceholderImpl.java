package fr.robotv2.placeholderannotationlib.impl;

import fr.robotv2.placeholderannotationlib.annotations.Cache;
import fr.robotv2.placeholderannotationlib.annotations.Optional;
import fr.robotv2.placeholderannotationlib.annotations.Placeholder;
import fr.robotv2.placeholderannotationlib.annotations.RequireOnlinePlayer;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholder;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholderExpansion;
import fr.robotv2.placeholderannotationlib.api.PlaceholderActor;
import fr.robotv2.placeholderannotationlib.util.CacheSystem;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;


public class MethodBasePlaceholderImpl implements BasePlaceholder {

    private final PlaceholderAnnotationProcessorImpl processor;
    private final BasePlaceholderExpansion expansion;
    private final Method method;
    private final Placeholder placeholder;
    private final Cache cacheAnnotation;
    private final boolean isDefault;
    private final boolean requiresOnline;

    public MethodBasePlaceholderImpl(
            @NotNull PlaceholderAnnotationProcessorImpl processor,
            @NotNull BasePlaceholderExpansion expansion,
            @NotNull Method method,
            @Nullable Placeholder placeholder,
            boolean isDefault) {
        this.processor = processor;
        this.expansion = expansion;
        this.method = method;
        this.placeholder = placeholder;
        this.cacheAnnotation = method.getAnnotation(Cache.class);
        this.isDefault = isDefault;
        this.requiresOnline = method.isAnnotationPresent(RequireOnlinePlayer.class);
    }

    @Override
    public boolean requiresOnlinePlayer() {
        return requiresOnline;
    }

    @Override
    @Nullable
    public Placeholder getPlaceholder() {
        return placeholder;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String process(OfflinePlayer player, String[] params) {
        PlaceholderActor actor = PlaceholderActor.of(player);

        String cached = null;
        String cacheKey = null;
        if (cacheAnnotation != null && player != null) {
            cacheKey = buildCacheKey(params);
            cached = CacheSystem.INSTANCE.getCache(player.getUniqueId(), cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        Object[] resolvedParams = resolveParameters(actor, params);
        if (resolvedParams == null) {
            return null;
        }

        String result = invoke(resolvedParams);

        if (result != null && cacheAnnotation != null && player != null) {
            CacheSystem.INSTANCE.cache(player.getUniqueId(), cacheKey, result, cacheAnnotation);
        }

        return result;
    }

    private String buildCacheKey(String[] params) {
        String id = (placeholder != null && placeholder.value().length > 0)
                ? String.join(processor.separator(), placeholder.value())
                : method.getName();
        return id + ":" + String.join(processor.separator(), params);
    }

    private Object[] resolveParameters(PlaceholderActor actor, String[] params) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] resolved = new Object[paramTypes.length];
        int argIndex = 0;

        if (paramTypes.length > 0 && PlaceholderActor.class.isAssignableFrom(paramTypes[0])) {
            resolved[0] = actor;
            paramTypes = Arrays.copyOfRange(paramTypes, 1, paramTypes.length);
            argIndex = 1;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];

            // Varargs handling
            if (method.isVarArgs() && i == paramTypes.length - 1) {
                Object varargArray = resolveVarargs(actor, params, i, type.getComponentType());
                if (varargArray == null) return null;
                resolved[i + argIndex] = varargArray;
                break;
            }

            // Normal parameter
            String value = (i < params.length) ? params[i] : null;
            Object resolvedValue = resolveSingleParam(actor, value, type, i + argIndex);
            if (resolvedValue == null && !isOptionalWithDefault(i + argIndex)) {
                return null;
            }
            resolved[i + argIndex] = resolvedValue;
        }

        return resolved;
    }

    private Object resolveSingleParam(PlaceholderActor actor, String value, Class<?> type, int methodParamIndex) {
        if (value != null) {
            return safeParse(actor, value, type);
        }

        Optional opt = getAnnotation(method, methodParamIndex, Optional.class);
        if (opt != null) {
            if (!opt.defaultParameter().isEmpty()) {
                return safeParse(actor, opt.defaultParameter(), type);
            } else {
                return null;
            }
        }

        processor.debug("Missing required parameter in " + method.getName());
        return null;
    }

    private Object resolveVarargs(PlaceholderActor actor, String[] params, int startIndex, Class<?> componentType) {
        String[] remaining = Arrays.copyOfRange(params, startIndex, params.length);
        Object array = Array.newInstance(componentType, remaining.length);

        for (int j = 0; j < remaining.length; j++) {
            Object parsed = safeParse(actor, remaining[j], componentType);
            if (parsed == null) return null;
            Array.set(array, j, parsed);
        }
        return array;
    }

    private Object safeParse(PlaceholderActor actor, String value, Class<?> type) {
        try {
            return type.isEnum()
                    ? Enum.valueOf(type.asSubclass(Enum.class), value.toUpperCase())
                    : Objects.requireNonNull(processor.getValueResolver(type)).resolve(actor, value);
        } catch (Exception e) {
            processor.logger().log(Level.WARNING, "Failed to parse '" + value + "' as " + type.getSimpleName() + " in method " + method.getName(), e);
            return null;
        }
    }

    private String invoke(Object[] params) {
        try {
            Object result = method.invoke(expansion, params);
            return (result == null) ? null : result.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            processor.logger().log(Level.SEVERE, "Error invoking placeholder method: " + method.getName(), e);
            return null;
        }
    }

    private boolean isOptionalWithDefault(int methodParamIndex) {
        Optional opt = getAnnotation(method, methodParamIndex, Optional.class);
        return opt != null && !opt.defaultParameter().isEmpty();
    }

    @Nullable
    private static <T extends Annotation> T getAnnotation(Method method, int paramIndex, Class<T> clazz) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (paramIndex < 0 || paramIndex >= parameterAnnotations.length) {
            return null;
        }
        for (Annotation annotation : parameterAnnotations[paramIndex]) {
            if (clazz.isInstance(annotation)) {
                return clazz.cast(annotation);
            }
        }
        return null;
    }
}
