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
        playerA = UUID.randomUUID();
        playerB = UUID.randomUUID();
        playerC = UUID.randomUUID();
    }

    @Test
    public void testCounterDoesNotAppearUntilOpponentHitsUs() {

        combatManager.recordHit(playerA, playerB, playerA);
        CombatSession session = combatManager.getSession(playerA, playerB);
        Assertions.assertNotNull(session);

        Assertions.assertFalse(session.isConfirmed(), "Combat must NOT be confirmed when only local player hit opponent");
        Assertions.assertFalse(combatManager.isInCombatWith(playerB, playerA), "Counter must NOT show yet");

        combatManager.recordHit(playerB, playerA, playerA);
        Assertions.assertTrue(session.isConfirmed(), "Combat MUST be confirmed once opponent hits us back");
        Assertions.assertEquals(playerB, combatManager.getCurrentActiveOpponent(), "Active opponent must now be B");
        Assertions.assertTrue(combatManager.isInCombatWith(playerB, playerA), "Counter should now be active");

        Assertions.assertTrue(combatManager.isInCombatWith(playerB, playerA));
    }

    @Test
    public void testIndependentCombatPairs() {

        combatManager.recordHit(playerA, playerB, playerA);
        combatManager.recordHit(playerB, playerA, playerA);
        CombatSession sessionAB = combatManager.getSession(playerA, playerB);
        Assertions.assertTrue(sessionAB.isConfirmed());
        sessionAB.setPotCount(playerB, 7);

        combatManager.recordHit(playerA, playerC, playerA);
        combatManager.recordHit(playerC, playerA, playerA);
        CombatSession sessionAC = combatManager.getSession(playerA, playerC);
        Assertions.assertTrue(sessionAC.isConfirmed());
        sessionAC.setPotCount(playerC, 3);

        Assertions.assertEquals(playerC, combatManager.getCurrentActiveOpponent());

        Assertions.assertEquals(7, sessionAB.getPotCount(playerB), "B's pot count must remain 7");
        Assertions.assertEquals(3, sessionAC.getPotCount(playerC), "C's pot count must be 3");

        combatManager.recordHit(playerA, playerB, playerA);
        combatManager.recordHit(playerB, playerA, playerA);
        Assertions.assertEquals(playerB, combatManager.getCurrentActiveOpponent());
        Assertions.assertEquals(7, sessionAB.getPotCount(playerB));
    }

    @Test
    public void testPlayerDeathResetsOnlyRelevantPair() {

        combatManager.recordHit(playerA, playerB, playerA);
        combatManager.recordHit(playerB, playerA, playerA);
        CombatSession sessionAB = combatManager.getSession(playerA, playerB);
        sessionAB.setPotCount(playerB, 12);

        combatManager.recordHit(playerA, playerC, playerA);
        combatManager.recordHit(playerC, playerA, playerA);
        CombatSession sessionAC = combatManager.getSession(playerA, playerC);
        sessionAC.setPotCount(playerC, 5);

        combatManager.handlePlayerDeath(playerB);

        Assertions.assertEquals(0, sessionAB.getPotCount(playerB), "Dead player B's pots must reset to 0");
        Assertions.assertFalse(sessionAB.isConfirmed(), "Session for dead player B should not remain confirmed");
        Assertions.assertFalse(combatManager.isInCombatWith(playerB, playerA));

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

        Assertions.assertTrue(combatManager.isOpponent(playerB));
        Assertions.assertFalse(combatManager.isOpponent(playerC));

        combatManager.recordHit(playerA, playerC, playerA);
        combatManager.recordHit(playerC, playerA, playerA);
        Assertions.assertTrue(combatManager.isOpponent(playerC));
    }
}
