package com.potcounter.test;

import com.potcounter.combat.CombatManager;
import com.potcounter.combat.CombatSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class CombatSystemTest {
    private CombatManager combatManager;
    private UUID playerA;
    private UUID playerB;
    private UUID playerC;

    @BeforeEach
    public void setup() {
        combatManager = new CombatManager();
        playerA = UUID.randomUUID(); // Local player
        playerB = UUID.randomUUID(); // Opponent 1
        playerC = UUID.randomUUID(); // Opponent 2
    }

    @Test
    public void testCounterDoesNotAppearUntilOpponentHitsUs() {
        // Step 1: Local player A hits opponent B
        combatManager.recordHit(playerA, playerB, playerA);
        CombatSession session = combatManager.getSession(playerA, playerB);
        Assertions.assertNotNull(session);
        // Opponent must hit back for confirmed mutual combat
        Assertions.assertFalse(session.isConfirmed(), "Combat must NOT be confirmed when only local player hit opponent");
        Assertions.assertFalse(combatManager.isInCombatWith(playerB, playerA), "Counter must NOT show yet");

        // Step 2: Opponent B hits local player A
        combatManager.recordHit(playerB, playerA, playerA);
        Assertions.assertTrue(session.isConfirmed(), "Combat MUST be confirmed once opponent hits us back");
        Assertions.assertEquals(playerB, combatManager.getCurrentActiveOpponent(), "Active opponent must now be B");
        Assertions.assertTrue(combatManager.isInCombatWith(playerB, playerA), "Counter should now be active");

        // Persistent: no 10-second timeout
        Assertions.assertTrue(combatManager.isInCombatWith(playerB, playerA));
    }

    @Test
    public void testIndependentCombatPairs() {
        // Mutual combat with B
        combatManager.recordHit(playerA, playerB, playerA);
        combatManager.recordHit(playerB, playerA, playerA);
        CombatSession sessionAB = combatManager.getSession(playerA, playerB);
        Assertions.assertTrue(sessionAB.isConfirmed());
        sessionAB.setPotCount(playerB, 7);

        // Mutual combat with C
        combatManager.recordHit(playerA, playerC, playerA);
        combatManager.recordHit(playerC, playerA, playerA);
        CombatSession sessionAC = combatManager.getSession(playerA, playerC);
        Assertions.assertTrue(sessionAC.isConfirmed());
        sessionAC.setPotCount(playerC, 3);

        // Active opponent switched to C
        Assertions.assertEquals(playerC, combatManager.getCurrentActiveOpponent());

        // B's pot count must remain 7
        Assertions.assertEquals(7, sessionAB.getPotCount(playerB), "B's pot count must remain 7");
        Assertions.assertEquals(3, sessionAC.getPotCount(playerC), "C's pot count must be 3");

        // When A and B trade hits again, active opponent switches back to B
        combatManager.recordHit(playerA, playerB, playerA);
        combatManager.recordHit(playerB, playerA, playerA);
        Assertions.assertEquals(playerB, combatManager.getCurrentActiveOpponent());
        Assertions.assertEquals(7, sessionAB.getPotCount(playerB));
    }

    @Test
    public void testPlayerDeathResetsOnlyRelevantPair() {
        // Engage A <-> B
        combatManager.recordHit(playerA, playerB, playerA);
        combatManager.recordHit(playerB, playerA, playerA);
        CombatSession sessionAB = combatManager.getSession(playerA, playerB);
        sessionAB.setPotCount(playerB, 12);

        // Engage A <-> C
        combatManager.recordHit(playerA, playerC, playerA);
        combatManager.recordHit(playerC, playerA, playerA);
        CombatSession sessionAC = combatManager.getSession(playerA, playerC);
        sessionAC.setPotCount(playerC, 5);

        // Player B dies!
        combatManager.handlePlayerDeath(playerB);

        // A-B pair must be removed/reset
        Assertions.assertEquals(0, sessionAB.getPotCount(playerB), "Dead player B's pots must reset to 0");
        Assertions.assertFalse(sessionAB.isConfirmed(), "Session for dead player B should not remain confirmed");
        Assertions.assertFalse(combatManager.isInCombatWith(playerB, playerA));

        // A-C pair MUST remain intact!
        Assertions.assertTrue(sessionAC.isConfirmed(), "A-C session must still be confirmed");
        Assertions.assertEquals(5, sessionAC.getPotCount(playerC), "C's pots must still be 5");
        Assertions.assertTrue(combatManager.isInCombatWith(playerC, playerA));
    }

    @Test
    public void testPlayerNameLineMatching() {
        String playerName = "xadem1_123";
        String serverLine = "2177 | xadem1_123 | -1";
        String healthLine = "20 ❤";

        Assertions.assertTrue(serverLine.toLowerCase().contains(playerName.toLowerCase()), "Server nametag line must match player name");
        Assertions.assertFalse(healthLine.toLowerCase().contains(playerName.toLowerCase()), "Health bar line must not match player name");
    }

    @Test
    public void testArenaAndCombatEntryUnification() {
        combatManager.setCurrentActiveOpponent(playerB);

        // Immediate opponent status
        Assertions.assertTrue(combatManager.isOpponent(playerB));
        Assertions.assertFalse(combatManager.isOpponent(playerC));

        // When mutual hits occur, combat is also confirmed
        combatManager.recordHit(playerA, playerC, playerA);
        combatManager.recordHit(playerC, playerA, playerA);
        Assertions.assertTrue(combatManager.isOpponent(playerC));
    }
}
