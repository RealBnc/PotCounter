package com.potcounter.combat;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PotCounter/Combat");

    private final Map<CombatPair, CombatSession> sessions = new ConcurrentHashMap<>();
    private UUID currentActiveOpponent = null;

    // Track previous position for arena teleportation detection
    private double lastPosX = Double.NaN;
    private double lastPosY = Double.NaN;
    private double lastPosZ = Double.NaN;

    /**
     * Records a hit between two players.
     * Karşıdaki oyuncu bize vurmadan sayaç belirmesin kuralı:
     * Combat is confirmed once mutual hits have taken place.
     */
    public void recordHit(UUID attacker, UUID victim, UUID localPlayerUuid) {
        if (attacker == null || victim == null || attacker.equals(victim)) {
            return;
        }

        CombatPair pair = new CombatPair(attacker, victim);
        CombatSession session = sessions.computeIfAbsent(pair, CombatSession::new);
        session.refreshCombatTime();

        if (localPlayerUuid != null && pair.contains(localPlayerUuid)) {
            UUID opponent = pair.getOther(localPlayerUuid);

            if (attacker.equals(localPlayerUuid)) {
                session.setLocalHitOpponent(true);
            } else if (victim.equals(localPlayerUuid)) {
                session.setOpponentHitLocal(true);
            }

            // Both have traded hits -> confirmed combat
            if (session.isLocalHitOpponent() && session.isOpponentHitLocal()) {
                boolean wasConfirmed = session.isConfirmed();
                session.setConfirmed(true);
                currentActiveOpponent = opponent;

                if (!wasConfirmed) {
                    LOGGER.info("Combat confirmed between local player and opponent {}", opponent);
                }
            }
        } else {
            session.setConfirmed(true);
        }
    }

    public boolean isInCombatWith(UUID opponentUuid, UUID localPlayerUuid) {
        if (opponentUuid == null || localPlayerUuid == null || opponentUuid.equals(localPlayerUuid)) {
            return false;
        }

        CombatSession session = getSession(opponentUuid, localPlayerUuid);
        if (session == null) {
            return false;
        }

        return session.isConfirmed();
    }

    public CombatSession getSession(UUID a, UUID b) {
        if (a == null || b == null) return null;
        return sessions.get(new CombatPair(a, b));
    }

    public CombatSession getOrCreateSession(UUID a, UUID b) {
        if (a == null || b == null) return null;
        return sessions.computeIfAbsent(new CombatPair(a, b), CombatSession::new);
    }

    public List<CombatSession> getConfirmedSessionsForPlayer(UUID playerUuid) {
        if (playerUuid == null) return Collections.emptyList();
        List<CombatSession> result = new ArrayList<>();
        for (CombatSession session : sessions.values()) {
            if (session.isConfirmed() && session.getPair().contains(playerUuid)) {
                result.add(session);
            }
        }
        return result;
    }

    /**
     * Resolves opponent in duels with 100% accuracy and monitors round/teleport resets.
     */
    public void checkArenaDuel(MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) {
            return;
        }

        UUID localUuid = client.player.getUuid();
        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();

        // 1. Teleportation check: if player teleported across world (> 25 blocks in 1 tick)
        if (!Double.isNaN(lastPosX)) {
            double dx = px - lastPosX;
            double dy = py - lastPosY;
            double dz = pz - lastPosZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > 625.0) { // > 25 blocks in 1 tick
                LOGGER.info("Arena teleport detected! Resetting combat and pot tracker state.");
                clear();
                PotCounterMod mod = PotCounterMod.getInstance();
                if (mod != null) {
                    mod.getPotTracker().resetAll();
                }
            }
        }
        lastPosX = px;
        lastPosY = py;
        lastPosZ = pz;

        // 2. Verify active opponent status (alive and not 0 health)
        if (currentActiveOpponent != null) {
            PlayerEntity opp = client.world.getPlayerByUuid(currentActiveOpponent);
            if (opp == null || opp.isDead() || opp.getHealth() <= 0.0f) {
                LOGGER.info("Active opponent {} died or left, resetting round.", currentActiveOpponent);
                handlePlayerDeath(currentActiveOpponent);
            } else {
                // Keep combat session continuously active throughout the arena duel
                CombatSession session = getOrCreateSession(currentActiveOpponent, localUuid);
                session.setConfirmed(true);
                session.setLocalHitOpponent(true);
                session.setOpponentHitLocal(true);
                session.refreshCombatTime();
            }
            return;
        }

        // 3. Scoreboard Scan (McPvp Duel Sidebar)
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard != null) {
            ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            if (sidebar != null) {
                for (ScoreHolder holder : scoreboard.getKnownScoreHolders()) {
                    String line = holder.getNameForScoreboard();
                    if (line == null) continue;
                    String clean = line.replaceAll("§[0-9a-fk-or]", "").trim();
                    if (clean.toLowerCase().contains("rakip:") || clean.toLowerCase().contains("opponent:")) {
                        String[] parts = clean.split(":");
                        if (parts.length >= 2) {
                            String targetName = parts[1].trim();
                            for (PlayerEntity p : client.world.getPlayers()) {
                                if (p.getName().getString().equalsIgnoreCase(targetName) && !p.getUuid().equals(localUuid)) {
                                    setNewOpponent(p.getUuid(), p.getName().getString());
                                    LOGGER.info("100% Match via Scoreboard: Opponent is {} ({})", targetName, p.getUuid());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. World Scan: 1 other non-spectator player in the world instance
        List<PlayerEntity> otherSurvivalPlayers = new ArrayList<>();
        for (PlayerEntity p : client.world.getPlayers()) {
            if (p.getUuid().equals(localUuid)) continue;
            if (p.isSpectator()) continue;
            if (p.isDead() || p.getHealth() <= 0.0f) continue;
            otherSurvivalPlayers.add(p);
        }

        if (otherSurvivalPlayers.size() == 1) {
            PlayerEntity singleOpp = otherSurvivalPlayers.get(0);
            setNewOpponent(singleOpp.getUuid(), singleOpp.getName().getString());
            LOGGER.info("100% Match via World Instance: Opponent is {} ({})", singleOpp.getName().getString(), singleOpp.getUuid());
        }
    }

    private void setNewOpponent(UUID oppUuid, String name) {
        if (!Objects.equals(currentActiveOpponent, oppUuid)) {
            currentActiveOpponent = oppUuid;
            PotCounterMod mod = PotCounterMod.getInstance();
            if (mod != null) {
                mod.getPotTracker().updatePlayerName(oppUuid, name);
                // Clean start for new opponent
                mod.getPotTracker().resetCombatPotCount(oppUuid);
            }
        }

        // Immediately activate combat session so nametag and tracker activate immediately
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            CombatSession session = getOrCreateSession(oppUuid, client.player.getUuid());
            session.setConfirmed(true);
            session.setLocalHitOpponent(true);
            session.setOpponentHitLocal(true);
            session.refreshCombatTime();
        }
    }

    public boolean isOpponent(UUID playerUuid) {
        if (playerUuid == null) return false;
        if (playerUuid.equals(currentActiveOpponent)) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            return isInCombatWith(playerUuid, client.player.getUuid());
        }
        return false;
    }

    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        checkArenaDuel(client);
    }

    public UUID getCurrentActiveOpponent() {
        return currentActiveOpponent;
    }

    public void setCurrentActiveOpponent(UUID currentActiveOpponent) {
        this.currentActiveOpponent = currentActiveOpponent;
    }

    public void handlePlayerDeath(UUID deadPlayer) {
        if (deadPlayer == null) return;

        Iterator<Map.Entry<CombatPair, CombatSession>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<CombatPair, CombatSession> entry = it.next();
            if (entry.getKey().contains(deadPlayer)) {
                entry.getValue().reset();
                it.remove();
            }
        }

        if (Objects.equals(currentActiveOpponent, deadPlayer)) {
            currentActiveOpponent = null;
        }

        // Clean round reset
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod != null) {
            mod.getPotTracker().onRoundEnd();
        }
    }

    public void clear() {
        sessions.clear();
        currentActiveOpponent = null;
        lastPosX = Double.NaN;
        lastPosY = Double.NaN;
        lastPosZ = Double.NaN;
    }
}
