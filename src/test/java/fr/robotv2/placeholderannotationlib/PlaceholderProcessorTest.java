package fr.robotv2.placeholderannotationlib;

import fr.robotv2.placeholderannotationlib.impl.PlaceholderAnnotationProcessorImpl;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlaceholderProcessorTest {

    private PlaceholderAnnotationProcessorImpl processor;
    private OfflinePlayer mockOfflinePlayer;
    private Player mockOnlinePlayer;

    @BeforeEach
    public void setup() {
        processor = new PlaceholderAnnotationProcessorImpl("_", Logger.getLogger("Test"), true);
        processor.registerExpansion(new TestExpansion(processor));

        mockOfflinePlayer = mock(OfflinePlayer.class);
        when(mockOfflinePlayer.getName()).thenReturn("Steve");

        mockOnlinePlayer = mock(Player.class);
        when(mockOnlinePlayer.getName()).thenReturn("Alex");
        when(mockOnlinePlayer.isOnline()).thenReturn(true);
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
        assertEquals("null", result);
    }

    @Test
    public void testDefaultPlaceholderWithArgs() {
        String result = processor.process(mockOfflinePlayer, "some_random_placeholder_arg1_arg2");
        assertEquals("Default output arg1,arg2", result);
    }

    @Test
    public void testBadParamTypeLogsError() {
        // This will try to parse "notanumber" into an int
        String result = processor.process(mockOfflinePlayer, "bad_param_notanumber");
        assertNull(result); // Should fail gracefully
    }
}
