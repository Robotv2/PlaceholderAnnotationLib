# PlaceholderAnnotationLib

A lightweight Java library for creating PlaceholderAPI expansions using annotations. This library simplifies the process of creating placeholders by using method and field annotations instead of manual string parsing.

## Features

- 🏷️ **Annotation-based placeholders** - Define placeholders using `@Placeholder` on methods and fields
- 🔧 **Type-safe parameter resolution** - Automatic conversion of string parameters to Java types
- 🎯 **Optional parameters** - Support for optional parameters with default values
- 📦 **Varargs support** - Handle variable number of arguments
- ⚡ **Caching system** - Built-in caching with TTL support using `@Cache`
- 🔒 **Online player requirements** - Restrict placeholders to online players only
- 🎨 **Flexible configuration** - Customizable separators, logging, and value resolvers
- 🧪 **Comprehensive testing** - Full test coverage with examples

## Installation

### Maven
```xml
<dependency>
    <groupId>fr.robotv2</groupId>
    <artifactId>PlaceholderAnnotationLib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'fr.robotv2:PlaceholderAnnotationLib:1.0.0'
```

## Quick Start

### 1. Create Your Expansion Class

```java
public class MyExpansion extends BasePlaceholderExpansion {
    
    public MyExpansion(PlaceholderAnnotationProcessor processor) {
        super(processor);
    }
    
    @Placeholder({"max", "balance"}) // for %myexpansion_max_balance%
    private final int maxBalance = 1000; // works with field for consistent value
    
    @Placeholder({"player", "name"}) // for %myexpansion_player_name%
    public String getPlayerName(PlaceholderActor actor) {
        return actor.getPlayer().getName();
    }
    
    @Placeholder({"player", "balance"}) // for %myexpansion_player_balance%
    @RequireOnlinePlayer
    public double getBalance(PlaceholderActor actor) {
        // Your balance logic here
        return 100.42;
    }
    
    // PlaceholderAPI related methods ...
    
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

### 2. Register Your Expansion

```java
public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Create processor
        PlaceholderAnnotationProcessor processor = new PlaceholderAnnotationProcessorImpl.Builder()
            .separator("_")
            .debug(false)
            .build();
        
        // Register your expansion
        processor.registerExpansion(new MyExpansion(processor));
        
        // Register with PlaceholderAPI
        new MyExpansion(processor).register();
    }
}
```

### 3. Use Your Placeholders

Your placeholders are now available in PlaceholderAPI:
- `%myexpansion_max_balance%` → Returns the maximum balance
- `%myexpansion_player_name%` → Returns player name
- `%myexpansion_player_balance%` → Returns player balance (online players only)

## Annotation Reference

### @Placeholder
Defines a placeholder on a method or field.

```java
// String array syntax
@Placeholder({"player", "stats", "kills"})
public String getKills(PlaceholderActor actor) { ... }

// Single element array
@Placeholder({"player", "stats", "deaths"})
public String getDeaths(PlaceholderActor actor) { ... }
```

**Usage**: `%myexpansion_player_stats_kills%`
**Usage**: `%myexpansion_player_stats_deaths%`


### @RequireOnlinePlayer
Restricts placeholder to online players only.

```java
@Placeholder({"player", "location"})
@RequireOnlinePlayer
public String getLocation(PlaceholderActor actor) {
    Player player = actor.requireOnlinePlayer(); // will not throw an exception
    Location location = player.getLocation();
    return "X: " + location.getX() + ", Y: " + location.getY() + ", Z: " + location.getZ();
}
```

### @Optional
Marks method parameters as optional with default values.

```java
@Placeholder({"player", "stats"})
public String getStats(PlaceholderActor actor, 
                      String statType, 
                      @Optional(defaultParameter = "kills") String format) {
    // statType is required, format defaults to "kills"
    return statType + ": " + format;
}
```

**Usage**: `%myexpansion_player_stats_kills%`
**Usage**: `%myexpansion_player_stats%` (Same thing as `%myexpansion_player_stats_kills%`)

### @Cache
Enables caching for expensive operations.

```java
@Placeholder({"server", "tps"})
@Cache(value = 30, unit = TimeUnit.SECONDS)
public double getTPS(PlaceholderActor actor) {
    // Expensive TPS calculation cached for 30 seconds
    return calculateTPS();
}
```

### @DefaultPlaceholder
Handles unknown placeholders as a fallback.

```java
@DefaultPlaceholder
public String handleUnknown(PlaceholderActor actor) {
    return "Unknown placeholder. Please provide a valid placeholder.";
}
```

## Parameter Types

The library automatically converts string parameters to Java types:

```java
@Placeholder({"player", "give_item"})
public String giveItem(PlaceholderActor actor, 
                      String itemName,      // String
                      int amount,           // Integer
                      boolean silent,       // Boolean
                      double price,         // Double
                      Material material,    // Enum
                      String... lore) {     // Varargs
    // Implementation
    return "Gave " + amount + " " + itemName;
}
```

**Usage**: `%myexpansion_player_give_item_diamond_64_true_100.5_DIAMOND_Shiny_Precious%`

## Field Placeholders

You can also use fields as placeholders:

```java
public class MyExpansion extends BasePlaceholderExpansion {
    
    @Placeholder({"server", "version"})
    private final String serverVersion = "1.20.1";
    
    @Placeholder({"plugin", "name"})
    @RequireOnlinePlayer
    private String pluginName = "MyAwesomePlugin";
}
```

## Advanced Features

### Custom Value Resolvers

Register custom type converters:

```java
processor.registerValueResolver(UUID.class, (actor, value) -> {
    try {
        return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
        return actor.getPlayer().getUniqueId(); // Fallback to player UUID
    }
});
```

## Requirements

- Java 8+
- Bukkit/Spigot/Paper
- PlaceholderAPI

## Support

If you encounter any issues or have questions:
- Open an issue on GitHub
- Check the examples in the test directory
- Enable debug mode for detailed logging
