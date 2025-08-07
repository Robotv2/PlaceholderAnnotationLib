package fr.robotv2.placeholderannotationlib.api;

@FunctionalInterface
public interface ValueResolver<T> {
    T resolve(PlaceholderActor actor, String param);
}
