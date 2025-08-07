package fr.robotv2.placeholderannotationlib.api;

import org.bukkit.OfflinePlayer;

public interface PlaceholderAnnotationProcessor {

    String separator();

    String process(OfflinePlayer player, String params);

    <T> void registerValueResolver(Class<? extends T> tClass, ValueResolver<? extends T> resolver);

    void registerExpansion(BasePlaceholderExpansion expansion);

    class Builder {

        private String separator;

        public Builder separator(String sep) {
            this.separator = sep;
            return this;
        }

        public PlaceholderAnnotationProcessor build() {
            return null; // TODO
        }
    }
}
