package fr.robotv2.placeholderannotationlib.api;

import fr.robotv2.placeholderannotationlib.impl.PlaceholderAnnotationProcessorImpl;
import org.bukkit.OfflinePlayer;

import java.util.function.Function;
import java.util.logging.Logger;
import java.util.Set;

public interface PlaceholderAnnotationProcessor {

    String separator();

    Logger logger();

    String process(OfflinePlayer player, String params);

    <T> void registerValueResolver(Class<? extends T> tClass, ValueResolver<? extends T> resolver);

    void registerExpansion(BasePlaceholderExpansion expansion);

    void registerDirect(String params, Function<PlaceholderActor, String> function);

    void registerDirect(String params, Function<PlaceholderActor, String> function, boolean requiresOnlinePlayer);

    Set<String> registeredPlaceholders();

    class Builder {

        private String separator = "_";
        private Logger logger = null;
        private boolean debug = false;

        public Builder separator(String sep) {
            this.separator = sep;
            return this;
        }

        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public PlaceholderAnnotationProcessor build() {
            if(logger == null) {
                logger = Logger.getLogger("PALib");
            }

            return new PlaceholderAnnotationProcessorImpl(separator, logger, debug);
        }
    }
}
