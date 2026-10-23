package com.potcounter.test;

import com.potcounter.combat.CombatManager;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterDisplay;
import com.potcounter.config.CounterMode;
import com.potcounter.tracker.PotTracker;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class PotTrackerModeTest {
    private CombatManager combatManager;
    private CounterConfig config;
    private PotTracker potTracker;

    private UUID localPlayer;
    private UUID opponentB;
    private UUID opponentC;
    private UUID neutralPlayer;

    @BeforeEach
    public void setup() {
        combatManager = new CombatManager();
        config = new CounterConfig();
        potTracker = new PotTracker(combatManager, config);

        localPlayer = UUID.randomUUID();
        opponentB = UUID.randomUUID();
        opponentC = UUID.randomUUID();
        neutralPlayer = UUID.randomUUID();

        // Mutual combat with B (opponent B hits localPlayer back)
        combatManager.recordHit(localPlayer, opponentB, localPlayer);
        combatManager.recordHit(opponentB, localPlayer, localPlayer);

        // Record pots
        potTracker.recordPot(opponentB, localPlayer);
        potTracker.recordPot(opponentB, localPlayer); // 2 pots

        potTracker.recordPot(opponentC, localPlayer); // 1 pot
        potTracker.recordPot(neutralPlayer, localPlayer); // 1 pot
    }

    @Test
    public void testSingleMode() {
        config.setCounterMode(CounterMode.SINGLE);

        // B is the confirmed active opponent
        Assertions.assertEquals(2, potTracker.getCountForPlayer(opponentB, localPlayer));

        // C is not the active opponent -> must return -1 (suppressed)
        Assertions.assertEquals(-1, potTracker.getCountForPlayer(opponentC, localPlayer));

        // Neutral player -> -1
        Assertions.assertEquals(-1, potTracker.getCountForPlayer(neutralPlayer, localPlayer));
    }

    @Test
    public void testAllPlayersMode() {
        config.setCounterMode(CounterMode.ALL_PLAYERS);

        // All players are tracked regardless of combat confirmation
        Assertions.assertEquals(2, potTracker.getCountForPlayer(opponentB, localPlayer));
        Assertions.assertEquals(1, potTracker.getCountForPlayer(opponentC, localPlayer));
        Assertions.assertEquals(1, potTracker.getCountForPlayer(neutralPlayer, localPlayer));
    }

    @Test
    public void testNametagDisplayCyclingAndCaching() {
        // Test enum cycle: NAMETAG -> MENU -> NAMETAG
        CounterDisplay display = CounterDisplay.NAMETAG;
        display = display.next();
        Assertions.assertEquals(CounterDisplay.MENU, display);
        Assertions.assertEquals("Menu HUD", display.getDisplayName());
        display = display.next();
        Assertions.assertEquals(CounterDisplay.NAMETAG, display);
        Assertions.assertEquals("Nametag", display.getDisplayName());

        // Test server custom name caching
        Text serverStyle = Text.literal("§bRealBnc §7[Babalar] §a19ms");
        potTracker.setServerCustomName(opponentB, serverStyle);

        Assertions.assertEquals(serverStyle, potTracker.getServerCustomName(opponentB));
        Assertions.assertNull(potTracker.getServerCustomName(opponentC));

        // Test active opponent getter
        Assertions.assertEquals(opponentB, potTracker.getCurrentActiveOpponent());
        Assertions.assertTrue(potTracker.isInCombatWith(opponentB, localPlayer));
        Assertions.assertFalse(potTracker.isInCombatWith(neutralPlayer, localPlayer));
    }

    @Test
    public void testCounterStartsAtMinusOneWhenPotThrown() {
        UUID freshOpponent = UUID.randomUUID();
        combatManager.setCurrentActiveOpponent(freshOpponent);

        // Before any pots are thrown: count is 0
        Assertions.assertEquals(0, potTracker.getCountForPlayer(freshOpponent, localPlayer));
        // HUD entries should not contain opponent with 0 pots
        Assertions.assertFalse(potTracker.getActiveEntries(localPlayer).containsKey(freshOpponent));

        // When opponent throws first pot -> count becomes 1
        potTracker.recordPot(freshOpponent, localPlayer);
        Assertions.assertEquals(1, potTracker.getCountForPlayer(freshOpponent, localPlayer));
        Assertions.assertEquals(1, potTracker.getActiveEntries(localPlayer).get(freshOpponent));
    }
}
