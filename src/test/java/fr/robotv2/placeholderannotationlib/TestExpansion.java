package fr.robotv2.placeholderannotationlib;

import fr.robotv2.placeholderannotationlib.annotations.Cache;
import fr.robotv2.placeholderannotationlib.annotations.DefaultPlaceholder;
import fr.robotv2.placeholderannotationlib.annotations.Optional;
import fr.robotv2.placeholderannotationlib.annotations.Placeholder;
import fr.robotv2.placeholderannotationlib.annotations.RequireOnlinePlayer;
import fr.robotv2.placeholderannotationlib.api.BasePlaceholderExpansion;
import fr.robotv2.placeholderannotationlib.api.PlaceholderActor;
import fr.robotv2.placeholderannotationlib.api.PlaceholderAnnotationProcessor;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.TimeUnit;

public class TestExpansion extends BasePlaceholderExpansion {

    private int cachedCalls = 0;

    public TestExpansion(PlaceholderAnnotationProcessor processor) {
        super(processor);
    }

    @Placeholder({"player", "stats", "kills"})
    public String kills(PlaceholderActor actor) {
        return "Kills: 42";
    }

    @Placeholder({"math", "add"})
    public String add(PlaceholderActor actor, int a, int b) {
        return String.valueOf(a + b);
    }

    @Placeholder({"optional", "test"})
    public String optionalTest(PlaceholderActor actor, @Optional(defaultParameter = "default") String value) {
        return "Value: " + value;
    }

    @Placeholder({"optional", "emptydefault"})
    public String optionalEmptyDefault(PlaceholderActor actor, @Optional String value) {
        return value == null ? "null" : value;
    }

    @DefaultPlaceholder
    public String defaultPlaceholder(PlaceholderActor actor, String... args) {
        return "Default output" + (args.length > 0 ? " " + String.join(",", args) : "");
    }

    @Placeholder({"online", "only"})
    @RequireOnlinePlayer
    public String onlineOnly(PlaceholderActor actor) {
        return "Online only placeholder";
    }

    @Placeholder({"field", "value"})
    public String fieldValue = "Field placeholder value";

    @Placeholder({"bad", "param"})
    public String badParam(PlaceholderActor actor, int number) {
        return "Number: " + number;
    }

    @Placeholder("quest")
    public String quest(PlaceholderActor actor, String serviceId, int number, @Optional(defaultParameter = "display") String param) {
        return "ServiceId: " + serviceId + ", Number: " + number + ", Param: " + param;
    }

    @Placeholder({"cached", "value"})
    @Cache(value = 100, unit = TimeUnit.MILLISECONDS)
    public String cached(PlaceholderActor actor) {
        cachedCalls++;
        return "C" + cachedCalls;
    }

    @Placeholder({"varargs", "test"})
    public String varargsTest(PlaceholderActor actor, String prefix, int count, String... items) {
        return prefix + ":" + count + ":" + String.join(",", items);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "testexpansion";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RobotV2";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
}
