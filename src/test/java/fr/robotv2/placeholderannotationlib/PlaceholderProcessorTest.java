package fr.robotv2.placeholderannotationlib;

import fr.robotv2.placeholderannotationlib.impl.PlaceholderAnnotationProcessorImpl;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlaceholderProcessorTest {

    private PlaceholderAnnotationProcessorImpl processor;
    private OfflinePlayer mockOfflinePlayer;
    private Player mockOnlinePlayer;

    @BeforeEach
    public void setup() {
        processor = new PlaceholderAnnotationProcessorImpl("_", Logger.getLogger("Test"), false);
        new TestExpansion(processor);

        mockOfflinePlayer = mock(OfflinePlayer.class);
        when(mockOfflinePlayer.getName()).thenReturn("Steve");
        when(mockOfflinePlayer.getUniqueId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        mockOnlinePlayer = mock(Player.class);
        when(mockOnlinePlayer.getName()).thenReturn("Alex");
        when(mockOnlinePlayer.isOnline()).thenReturn(true);
        when(mockOnlinePlayer.getUniqueId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    }

    @Test
    public void testUnknownPlaceholderReturnsEmpty() {
        String result = processor.process(mockOfflinePlayer, "nonexistent_placeholder");
        assertEquals("", result);
    }

    @Test
    public void testUnknownPlaceholderUsesDefault() {
        String result = processor.process(mockOfflinePlayer, "nonexistent_placeholder_with_args");
        assertTrue(result.startsWith("Default output"));
    }

    @Test
    public void testCaseInsensitiveMatching() {
        String result = processor.process(mockOfflinePlayer, "PLAYER_STATS_KILLS");
        assertEquals("Kills: 42", result);
    }

    @Test
    public void testLongestMatchWins() {
        // If we had both "player_stats" and "player_stats_kills", kills should win
        String result = processor.process(mockOfflinePlayer, "player_stats_kills_extra");
        assertEquals("Kills: 42", result);
    }

    @Test
    public void testFieldPlaceholder() {
        String result = processor.process(mockOfflinePlayer, "field_value");
        assertEquals("Field placeholder value", result);
    }

    @Test
    public void testOptionalEmptyDefault() {
        String result = processor.process(mockOfflinePlayer, "optional_emptydefault");
        assertNull(result);
    }

    @Test
    public void testDefaultPlaceholderWithArgs() {
        String result = processor.process(mockOfflinePlayer, "some_random_placeholder_arg1_arg2");
        assertEquals("Default output arg1,arg2", result);
    }

    @Test
    public void testBadParamTypeLogsError() {
        String result = processor.process(mockOfflinePlayer, "bad_param_notanumber");
        assertNull(result); // Should fail gracefully
    }

    @Test
    public void testCacheHitAndExpiry() throws InterruptedException {
        String first = processor.process(mockOfflinePlayer, "cached_value");
        assertEquals("C1", first);

        String second = processor.process(mockOfflinePlayer, "cached_value");
        assertEquals("C1", second);

        Thread.sleep(150);
        String third = processor.process(mockOfflinePlayer, "cached_value");
        assertEquals("C2", third);
    }

    @Test
    public void testQuest() {
        String result = processor.process(mockOfflinePlayer, "quest_daily_1");
        assertEquals("ServiceId: daily, Number: 1, Param: display", result);
    }

    @Test
    public void testQuestDefaultParam() {
        String result = processor.process(mockOfflinePlayer, "quest_daily_1_pizza");
        assertEquals("ServiceId: daily, Number: 1, Param: pizza", result);
    }

    @Test
    public void testVarargsWithFixedParams() {
        String result = processor.process(mockOfflinePlayer, "varargs_test_items_5_apple_banana_cherry");
        assertEquals("items:5:apple,banana,cherry", result);
    }

    @Test
    public void testVarargsWithNoVarargs() {
        String result = processor.process(mockOfflinePlayer, "varargs_test_empty_0");
        assertEquals("empty:0:", result);
    }

    @Test
    public void testVarargsWithSingleVararg() {
        String result = processor.process(mockOfflinePlayer, "varargs_test_single_1_only");
        assertEquals("single:1:only", result);
    }

    @Test
    public void testRegisterDirectBasic() {
        processor.registerDirect("direct_test", actor -> "Direct placeholder result");
        
        String result = processor.process(mockOfflinePlayer, "direct_test");
        assertEquals("Direct placeholder result", result);
    }

    @Test
    public void testRegisterDirectWithPlayerData() {
        processor.registerDirect("direct_player", actor -> "Player: " + actor.getPlayer().getName());
        
        String result = processor.process(mockOfflinePlayer, "direct_player");
        assertEquals("Player: Steve", result);
    }

    @Test
    public void testRegisterDirectCaseInsensitive() {
        processor.registerDirect("Direct_Case_Test", actor -> "Case insensitive works");
        
        String result = processor.process(mockOfflinePlayer, "DIRECT_CASE_TEST");
        assertEquals("Case insensitive works", result);
    }

    @Test
    public void testRegisterDirectNullParams() {
        assertThrows(IllegalArgumentException.class, () -> {
            processor.registerDirect(null, actor -> "Should fail");
        });
    }

    @Test
    public void testRegisterDirectEmptyParams() {
        assertThrows(IllegalArgumentException.class, () -> {
            processor.registerDirect("", actor -> "Should fail");
        });
    }

    @Test
    public void testRegisterDirectNullFunction() {
        assertThrows(IllegalArgumentException.class, () -> {
            processor.registerDirect("test", null);
        });
    }

    @Test
    public void testRegisterDirectOverridesAnnotationPlaceholder() {
        // First register direct placeholder that overrides existing annotation-based one
        processor.registerDirect("field_value", actor -> "Direct override");
        
        String result = processor.process(mockOfflinePlayer, "field_value");
        assertEquals("Direct override", result);
    }

    @Test
    public void testDirectPlaceholderInRegisteredList() {
        processor.registerDirect("list_test", actor -> "Listed");
        
        assertTrue(processor.registeredPlaceholders().contains("list_test"));
    }

    @Test
    public void testRegisterDirectWithOnlinePlayerRequirement() {
        processor.registerDirect("online_only", actor -> "Online player: " + actor.getPlayer().getName(), true);
        
        // Test with offline player - should return empty string
        String resultOffline = processor.process(mockOfflinePlayer, "online_only");
        assertEquals("", resultOffline);
        
        // Test with online player - should work
        String resultOnline = processor.process(mockOnlinePlayer, "online_only");
        assertEquals("Online player: Alex", resultOnline);
    }
}
