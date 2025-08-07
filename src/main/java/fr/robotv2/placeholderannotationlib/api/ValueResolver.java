package fr.robotv2.placeholderannotationlib.api;

@FunctionalInterface
public interface ValueResolver<T> {
    T resolver(PlaceholderActor actor, String param);
}
