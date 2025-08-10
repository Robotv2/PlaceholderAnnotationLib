# PlaceholderAnnotationLib

A lightweight Java library for creating PlaceholderAPI expansions using annotations. This library simplifies the process of creating placeholders by using method and field annotations instead of manual string parsing.

## How It Works

The library's main goal is to replace verbose, hard-to-maintain `onPlaceholderRequest` methods with clean, dedicated, and self-documenting annotated methods.

### **Before:** The Traditional Approach

Without this library, you typically end up with a single large method full of `if/else` statements and manual string splitting to handle different placeholders.

```java
@Override
public String onPlaceholderRequest(Player player, String params) {
    if (params.equalsIgnoreCase("player_name")) {
        return player.getName();
    }
    
    if (params.equalsIgnoreCase("player_balance")) {
        // Balance logic...
        return "100.42";
    }
    
    if (params.startsWith("player_stats_")) {
        String[] parts = params.split("_");
        if (parts.length == 3) {
            String statType = parts[2];
            // Logic to get stats...
        }
    }
    
    return null;
}
````

### **After:** The Annotation-Based Approach

With this library, each placeholder is a separate, clearly defined method. The code is organized, readable, and easier to debug.

```java
// Handles %myexpansion_player_name%
@Placeholder({"player", "name"})
public String getPlayerName(PlaceholderActor actor) {
    return actor.getPlayer().getName();
}

// Handles %myexpansion_player_balance%
@Placeholder({"player", "balance"})
public double getBalance(PlaceholderActor actor) {
    // Your balance logic here
    return 100.42;
}

// Handles %myexpansion_player_stats_<stat_type>%
@Placeholder({"player", "stats"})
public int getStats(PlaceholderActor actor, String statType) {
    // Logic to get stats...
}
```

-----

## Features

  - **🏷️ Annotation-based placeholders:** Define placeholders using `@Placeholder` on methods and fields.
  - **🔧 Type-safe parameter resolution:** Automatic conversion of string parameters from the placeholder to Java types like `int`, `boolean`, `enum`, etc.
  - **🎯 Optional parameters:** Support for optional placeholder parts with default values using `@Optional`.
  - **📦 Varargs support:** Handle a variable number of arguments in your placeholder methods.
  - **⚡ Caching system:** Built-in caching with Time-To-Live (TTL) support via the `@Cache` annotation.
  - **🔒 Online player requirements:** Restrict placeholders to online players using `@RequireOnlinePlayer`.
  - **🎨 Flexible configuration:** Customize separators, logging, and register custom value resolvers.

-----

## Installation

Add the JitPack repository and the library dependency to your build configuration.

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>[https://jitpack.io](https://jitpack.io)</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>fr.robotv2</groupId>
        <artifactId>PlaceholderAnnotationLib</artifactId>
        <version>VERSION</version> </dependency>
</dependencies>
```

### Gradle

```gradle
repositories {
    // Other repositories...
    maven { url = '[https://jitpack.io](https://jitpack.io)' }
}

dependencies {
    // Other dependencies...
    implementation 'fr.robotv2:PlaceholderAnnotationLib:VERSION' // Replace with the latest version
}
```

-----

## Quick Start

### 1\. Create Your Expansion Class

Extend `BasePlaceholderExpansion` and use annotations to define your placeholders.

```java
public class MyExpansion extends BasePlaceholderExpansion {
    
    public MyExpansion(PlaceholderAnnotationProcessor processor) {
        super(processor);
    }
    
    // This field handles %myexpansion_max_balance%
    @Placeholder({"max", "balance"})
    private final int maxBalance = 1000;
    
    // This method handles %myexpansion_player_name%
    @Placeholder({"player", "name"})
    public String getPlayerName(PlaceholderActor actor) {
        return actor.getPlayer().getName();
    }
    
    // This method handles %myexpansion_player_balance% and requires the player to be online
    @Placeholder({"player", "balance"})
    @RequireOnlinePlayer
    public double getBalance(PlaceholderActor actor) {
        // Your balance logic here
        return 100.42;
    }
    
    // --- Standard PlaceholderAPI Methods ---
    
    @Override
    public String getIdentifier() {
        return "myexpansion";
    }

    @Override
    public String getAuthor() {
        return "YourName";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
```

### 2\. Register Your Expansion

In your main plugin class, instantiate the `PlaceholderAnnotationProcessor` and your expansion, then register it.

```java
public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Create the processor with desired configuration
        PlaceholderAnnotationProcessor processor = new PlaceholderAnnotationProcessor.Builder()
            .separator('_')
            .debug(false)
            .build();
            
        // Register your expansion
        MyExpansion expansion = new MyExpansion(processor);
        expansion.register(); // Registers the expansion with PlaceholderAPI
    }
}
```

### 3\. Use Your Placeholders

Your placeholders are now available through PlaceholderAPI:

  - `%myexpansion_max_balance%` → Returns the value of the `maxBalance` field.
  - `%myexpansion_player_name%` → Returns the player's name.
  - `%myexpansion_player_balance%` → Returns the player's balance, only if they are online.

-----

## Annotation Reference

### @Placeholder

Defines a placeholder on a method or field. The string array defines the parts of the placeholder, which will be joined by the configured separator.

```java
// Handles: %myexpansion_player_stats_kills%
@Placeholder({"player", "stats", "kills"})
public String getKills(PlaceholderActor actor) { /* ... */ }

// Handles: %myexpansion_player_stats_deaths%
@Placeholder({"player", "stats", "deaths"})
public String getDeaths(PlaceholderActor actor) { /* ... */ }
```

### @RequireOnlinePlayer

Restricts a placeholder to online players. If the player is offline, the placeholder will not be processed and will appear in its raw form.

```java
@Placeholder({"player", "location"})
@RequireOnlinePlayer
public String getLocation(PlaceholderActor actor) {
    // This call is safe; it will not throw a NullPointerException.
    Player player = actor.requireOnlinePlayer();
    Location location = player.getLocation();
    return "X: " + location.getX() + ", Y: " + location.getY() + ", Z: " + location.getZ();
}
```

### @Optional

Marks a method parameter as optional and provides a default value if it is not supplied in the placeholder string.

```java
@Placeholder({"player", "stats"})
public String getStats(PlaceholderActor actor, 
                       String statType, 
                       @Optional(defaultParameter = "kills") String anotherParameter) {
    // For %myexpansion_player_stats_deaths%: statType = "deaths", anotherParameter = "kills"
    // For %myexpansion_player_stats_deaths_assists%: statType = "deaths", anotherParameter = "assists"
    return statType + ": " + anotherParameter;
}
```

### @Cache

Enables caching for the result of a method. This is useful for expensive operations that do not need to be re-calculated on every request.

```java
// The result of this method will be cached for 30 seconds.
@Placeholder({"server", "tps"})
@Cache(value = 30, unit = TimeUnit.SECONDS)
public double getTPS() {
    // Expensive TPS calculation that now runs at most once every 30 seconds.
    return calculateTPS();
}
```

### @DefaultPlaceholder

Defines a fallback method to be executed when a requested placeholder does not match any other defined placeholder.

```java
@DefaultPlaceholder
public String handleUnknown() {
    return "Unknown placeholder.";
}
```

-----

## Parameter Types

The library automatically converts string segments from the placeholder into corresponding Java types for method parameters.

Supported types include `String`, primitives (`int`, `double`, etc.), primitive wrappers (`Integer`, `Double`, etc.), `boolean`, `enum` types, and `String...` (varargs).

**Example:**
For a placeholder like `%myexpansion_player_give_item_DIAMOND_64_true_Shiny_Precious%`

```java
@Placeholder({"player", "give", "item"})
public String giveItem(PlaceholderActor actor,
                       Material material,   // "DIAMOND" is resolved to Material.DIAMOND
                       int amount,          // "64" is parsed to an int
                       boolean silent,      // "true" is parsed to a boolean
                       String... lore) {    // "Shiny", "Precious" become a String array
    
    // Implementation...
    return "Gave " + amount + " " + material.name();
}
```

## Field Placeholders

You can use `@Placeholder` on fields for values that are constant or do not require logic to compute.

```java
public class MyExpansion extends BasePlaceholderExpansion {
    
    @Placeholder({"server", "version"})
    private final String serverVersion = "1.20.1";
    
    @Placeholder({"plugin", "name"})
    private String pluginName = "MyAwesomePlugin";
    
    // ...
}
```

## Advanced Features

### Custom Value Resolvers

You can register your own converters for custom types.

```java
// In your onEnable method, configure the processor:
processor.registerValueResolver(UUID.class, (actor, value) -> {
    try {
        return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
        // Fallback to the player's UUID if the parameter is invalid or missing.
        return actor.getPlayer().getUniqueId();
    }
});
```

-----

## Requirements

  - Java 8+
  - Bukkit/Spigot/Paper
  - PlaceholderAPI

## Support

  - Open an issue on GitHub for bugs or feature requests.
  - Review the examples in the `test` directory of the project repository.
  - Enable debug mode (`.debug(true)`) in the processor builder for detailed logging.

<!-- end list -->
