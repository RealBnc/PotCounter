package com.potcounter.tracker;

import com.potcounter.combat.CombatManager;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterMode;
import com.potcounter.render.ColorHelper;
import com.potcounter.render.MarkerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PotTracker {
    public static class CachedPlayerData {
        private final int count;
        private final boolean inCombat;
        private final boolean shouldDisplay;
        private final int color;
        private final MutableText suffixText;

        public CachedPlayerData(int count, boolean inCombat, boolean shouldDisplay, int color, MutableText suffixText) {
            this.count = count;
            this.inCombat = inCombat;
            this.shouldDisplay = shouldDisplay;
            this.color = color;
            this.suffixText = suffixText;
        }

        public int getCount() { return count; }
        public boolean isInCombat() { return inCombat; }
        public boolean shouldDisplay() { return shouldDisplay; }
        public int getColor() { return color; }
        public MutableText getSuffixText() { return suffixText; }
    }

    private final CombatManager combatManager;
    private final CounterConfig config;

    // Single source of truth for all opponent/player pot counts
    private final Map<UUID, Integer> playerPotCounts = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerNames = new ConcurrentHashMap<>();
    private final Map<UUID, Text> serverCustomNames = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> serverNametagEntityIds = new ConcurrentHashMap<>();
    private final Map<UUID, CachedPlayerData> renderCache = new ConcurrentHashMap<>();

    public PotTracker(CombatManager combatManager, CounterConfig config) {
        this.combatManager = combatManager;
        this.config = config;
    }

    public void updatePlayerName(UUID uuid, String name) {
        if (uuid != null && name != null && !name.isEmpty()) {
            playerNames.put(uuid, name);
        }
    }

    public String getPlayerName(UUID uuid) {
        if (uuid == null) {
            return "Player";
        }

        String cached = playerNames.get(uuid);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            if (client.world != null) {
                PlayerEntity player = client.world.getPlayerByUuid(uuid);
                if (player != null) {
                    String name = player.getGameProfile() != null && player.getGameProfile().name() != null
                            ? player.getGameProfile().name()
                            : player.getName().getString();
                    if (name != null && !name.trim().isEmpty()) {
                        playerNames.put(uuid, name.trim());
                        return name.trim();
                    }
                }
            }
            if (client.getNetworkHandler() != null) {
                PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(uuid);
                if (entry != null && entry.getProfile() != null && entry.getProfile().name() != null) {
                    String name = entry.getProfile().name();
                    if (name != null && !name.trim().isEmpty()) {
                        playerNames.put(uuid, name.trim());
                        return name.trim();
                    }
                }
            }
        }

        return "Player";
    }

    public void setServerCustomName(UUID uuid, Text name) {
        if (uuid != null && name != null) {
            serverCustomNames.put(uuid, name);
        }
    }

    public Text getServerCustomName(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return serverCustomNames.get(uuid);
    }

    public void setServerNametagEntityId(UUID uuid, int entityId) {
        if (uuid != null) {
            serverNametagEntityIds.put(uuid, entityId);
        }
    }

    public int getServerNametagEntityId(UUID uuid) {
        if (uuid == null) {
            return -1;
        }
        return serverNametagEntityIds.getOrDefault(uuid, -1);
    }

    public UUID getCurrentActiveOpponent() {
        return combatManager.getCurrentActiveOpponent();
    }

    public boolean isInCombatWith(UUID opponentUuid, UUID localPlayerUuid) {
        return combatManager.isInCombatWith(opponentUuid, localPlayerUuid);
    }

    public void recordPot(UUID throwerUuid, UUID localPlayerUuid) {
        if (throwerUuid == null) return;

        getPlayerName(throwerUuid);

        if (localPlayerUuid != null && throwerUuid.equals(localPlayerUuid)) {
            return;
        }

        // Single source of truth: merge directly into playerPotCounts
        playerPotCounts.merge(throwerUuid, 1, Integer::sum);
        renderCache.remove(throwerUuid);
    }

    public void resetCombatPotCount(UUID playerUuid) {
        if (playerUuid != null) {
            playerPotCounts.put(playerUuid, 0);
            renderCache.remove(playerUuid);
        }
    }

    public void resetPlayer(UUID uuid) {
        if (uuid != null) {
            playerPotCounts.remove(uuid);
            renderCache.remove(uuid);
        }
    }

    /**
     * Resets pot counts when a round finishes (player reaches 0 HP).
     */
    public void onRoundEnd() {
        UUID opp = combatManager.getCurrentActiveOpponent();
        if (opp != null) {
            playerPotCounts.put(opp, 0);
        }
        renderCache.clear();
    }

    public int getCountForPlayer(UUID targetUuid, UUID localPlayerUuid) {
        if (targetUuid == null) return -1;

        CounterMode mode = config.getCounterMode();

        if (mode == CounterMode.ALL_PLAYERS) {
            return playerPotCounts.getOrDefault(targetUuid, 0);
        }

        // SINGLE mode: only show for active opponent
        if (Objects.equals(combatManager.getCurrentActiveOpponent(), targetUuid)) {
            return playerPotCounts.getOrDefault(targetUuid, 0);
        }

        return -1;
    }

    public Map<UUID, Integer> getActiveEntries(UUID localPlayerUuid) {
        Map<UUID, Integer> result = new LinkedHashMap<>();
        CounterMode mode = config.getCounterMode();

        if (mode == CounterMode.ALL_PLAYERS) {
            for (Map.Entry<UUID, Integer> entry : playerPotCounts.entrySet()) {
                if (localPlayerUuid == null || !entry.getKey().equals(localPlayerUuid)) {
                    if (entry.getValue() > 0) {
                        getPlayerName(entry.getKey());
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            return result;
        }

        // SINGLE mode:
        UUID activeOpponent = combatManager.getCurrentActiveOpponent();
        if (activeOpponent == null) {
            // Fallback 1: check confirmed sessions
            List<com.potcounter.combat.CombatSession> sessions = combatManager.getConfirmedSessionsForPlayer(localPlayerUuid);
            for (com.potcounter.combat.CombatSession session : sessions) {
                UUID opp = session.getPair().getOther(localPlayerUuid);
                if (opp != null) {
                    activeOpponent = opp;
                    break;
                }
            }
        }
        if (activeOpponent == null) {
            // Fallback 2: check playerPotCounts
            for (UUID u : playerPotCounts.keySet()) {
                if (localPlayerUuid == null || !u.equals(localPlayerUuid)) {
                    activeOpponent = u;
                    break;
                }
            }
        }
        if (activeOpponent == null) {
            // Fallback 3: check other players in world (if duel arena has 1 other player)
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.world != null) {
                for (PlayerEntity p : client.world.getPlayers()) {
                    if (localPlayerUuid != null && p.getUuid().equals(localPlayerUuid)) continue;
                    if (p.isSpectator()) continue;
                    activeOpponent = p.getUuid();
                    break;
                }
            }
        }

        if (activeOpponent != null) {
            int count = playerPotCounts.getOrDefault(activeOpponent, 0);
            if (count > 0) {
                getPlayerName(activeOpponent);
                result.put(activeOpponent, count);
            }
        }

        return result;
    }

    public CachedPlayerData getCachedData(UUID uuid) {
        return renderCache.get(uuid);
    }

    public void updateCache(ClientWorld world, UUID localPlayerUuid) {
        if (world == null) return;

        for (PlayerEntity player : world.getPlayers()) {
            UUID uuid = player.getUuid();
            if (localPlayerUuid != null && uuid.equals(localPlayerUuid)) {
                continue;
            }

            updatePlayerName(uuid, player.getName().getString());

            int count = getCountForPlayer(uuid, localPlayerUuid);
            boolean inCombat = (localPlayerUuid != null) && combatManager.isInCombatWith(uuid, localPlayerUuid);

            // Display counter ONLY if mutual combat has occurred and player has thrown at least 1 pot
            boolean shouldDisplay = (count > 0) && inCombat;

            int color = ColorHelper.getPotColor(count, config.isAutoColor(), config.getCustomColor());
            MutableText suffix = (count > 0)
                    ? MarkerHelper.mark(Text.literal(" -" + count).styled(s -> s.withColor(color)))
                    : null;

            renderCache.put(uuid, new CachedPlayerData(count, inCombat, shouldDisplay, color, suffix));
        }
    }

    public void tick(UUID localPlayerUuid, ClientWorld world) {
        updateCache(world, localPlayerUuid);
    }

    public void resetAll() {
        playerPotCounts.clear();
        playerNames.clear();
        serverCustomNames.clear();
        serverNametagEntityIds.clear();
        renderCache.clear();
    }
}
