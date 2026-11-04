package com.potcounter.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatSession {
    private final CombatPair pair;
    private final Map<UUID, Integer> potCounts = new HashMap<>();

    private boolean localHitOpponent = false;
    private boolean opponentHitLocal = false;
    private boolean confirmed = false;
    private long lastCombatTime = 0;

    public CombatSession(CombatPair pair) {
        this.pair = pair;
    }

    public CombatPair getPair() {
        return pair;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isLocalHitOpponent() {
        return localHitOpponent;
    }

    public void setLocalHitOpponent(boolean localHitOpponent) {
        this.localHitOpponent = localHitOpponent;
    }

    public boolean isOpponentHitLocal() {
        return opponentHitLocal;
    }

    public void setOpponentHitLocal(boolean opponentHitLocal) {
        this.opponentHitLocal = opponentHitLocal;
    }

    public long getLastCombatTime() {
        return lastCombatTime;
    }

    public void refreshCombatTime() {
        this.lastCombatTime = System.currentTimeMillis();
    }

    public int getPotCount(UUID playerUuid) {
        return potCounts.getOrDefault(playerUuid, 0);
    }

    public void incrementPot(UUID playerUuid) {
        potCounts.put(playerUuid, getPotCount(playerUuid) + 1);
    }

    public void setPotCount(UUID playerUuid, int count) {
        potCounts.put(playerUuid, Math.max(0, count));
    }

    public void reset() {
        confirmed = false;
        localHitOpponent = false;
        opponentHitLocal = false;
        lastCombatTime = 0;
        potCounts.clear();
    }
}
