package com.potcounter.debug;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.render.MarkerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DiagnosticsTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger("PotCounter/Debug");

    public static final AtomicLong displayNameCalls = new AtomicLong();
    public static final AtomicLong hasLabelCalls = new AtomicLong();
    public static final AtomicLong textDisplayHookCalls = new AtomicLong();
    public static final AtomicLong damagePacketCalls = new AtomicLong();
    public static final AtomicLong potionSpawnCalls = new AtomicLong();

    private static final Map<UUID, Boolean> lastHasLabelResult = new ConcurrentHashMap<>();

    public static void recordHasLabel(UUID playerUuid, boolean result) {
        if (playerUuid != null) {
            lastHasLabelResult.put(playerUuid, result);
        }
    }

    public static Boolean getLastHasLabel(UUID playerUuid) {
        return playerUuid != null ? lastHasLabelResult.get(playerUuid) : null;
    }

    public static void runDiagnostics(MinecraftClient client, Consumer<Text> feedback) {
        if (client == null || client.world == null || client.player == null) {
            feedback.accept(Text.literal("§c[PotCounter] World or local player not available."));
            return;
        }

        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod == null) {
            feedback.accept(Text.literal("§c[PotCounter] Mod instance not loaded."));
            return;
        }

        CounterConfig config = mod.getConfig();
        ClientPlayerEntity localPlayer = client.player;

        feedback.accept(Text.literal("§6=== PotCounter Diagnostics (McPvp.club) ==="));
        feedback.accept(Text.literal("§eMode: §f" + config.getCounterMode() + " §7| §eDisplay: §f" + config.getCounterDisplay()));
        feedback.accept(Text.literal("§7Mixin stats: "
                + "§fNameMixin=" + displayNameCalls.get() + " "
                + "§fHasLabel=" + hasLabelCalls.get() + " "
                + "§fTextDisplay=" + textDisplayHookCalls.get() + " "
                + "§fDamage=" + damagePacketCalls.get() + " "
                + "§fPots=" + potionSpawnCalls.get()));

        LOGGER.info("[PotCounter Debug] Running diagnostic dump. Active config: Mode={}, Display={}",
                config.getCounterMode(), config.getCounterDisplay());

        List<? extends PlayerEntity> players = client.world.getPlayers();
        int count = 0;

        for (PlayerEntity p : players) {
            if (p.getUuid().equals(localPlayer.getUuid())) {
                continue;
            }

            double dist = Math.sqrt(localPlayer.squaredDistanceTo(p));
            if (dist > 32.0) {
                continue;
            }

            count++;
            UUID uuid = p.getUuid();
            String name = p.getName().getString();
            Text displayName = p.getDisplayName();
            String displayStr = displayName != null ? displayName.getString() : "null";
            boolean hasMarker = MarkerHelper.hasMarker(displayName);

            int potCount = mod.getPotTracker().getCountForPlayer(uuid, localPlayer.getUuid());
            boolean inCombat = mod.getCombatManager().isInCombatWith(uuid, localPlayer.getUuid());
            Boolean hasLabel = getLastHasLabel(uuid);

            // Check passengers & vehicle
            List<Entity> passengers = p.getPassengerList();
            StringBuilder passStr = new StringBuilder();
            for (Entity pass : passengers) {
                if (!passStr.isEmpty()) passStr.append(", ");
                passStr.append(pass.getType().getName().getString());
                if (pass instanceof DisplayEntity.TextDisplayEntity td) {
                    DisplayEntity.TextDisplayEntity.Data data = td.getData();
                    if (data != null && data.text() != null) {
                        passStr.append("(\"").append(data.text().getString()).append("\")");
                    }
                }
            }

            // Check nearby floating nametag entities (e.g. TAB armor stand / text display holograms)
            Box searchBox = p.getBoundingBox().expand(2.5, 3.0, 2.5);
            List<Entity> nearbyEntities = client.world.getOtherEntities(p, searchBox, e ->
                    e instanceof DisplayEntity.TextDisplayEntity || e instanceof ArmorStandEntity);

            StringBuilder nearbyStr = new StringBuilder();
            for (Entity e : nearbyEntities) {
                if (!nearbyStr.isEmpty()) nearbyStr.append(", ");
                nearbyStr.append(e.getType().getName().getString());
                if (e.hasCustomName() && e.getCustomName() != null) {
                    nearbyStr.append("(\"").append(e.getCustomName().getString()).append("\")");
                }
            }

            feedback.accept(Text.literal(String.format("§a▶ %s §7(dist: %.1fm) §ePots: §c%d §7| §eCombat: §f%s §7| §eHasLabel: §f%s",
                    name, dist, potCount, inCombat, (hasLabel != null ? hasLabel : "N/A"))));
            feedback.accept(Text.literal(String.format("   §7DisplayName: \"§f%s§7\" (Marker=%s)", displayStr, hasMarker)));

            if (!passStr.isEmpty()) {
                feedback.accept(Text.literal("   §7Passengers: §b" + passStr));
            }
            if (!nearbyStr.isEmpty()) {
                feedback.accept(Text.literal("   §7Nearby Nametag Entities: §d" + nearbyStr));
            }

            LOGGER.info("[PotCounter Debug] Player {}: uuid={}, dist={:.1f}, pots={}, combat={}, hasLabel={}, marker={}, displayName='{}', passengers=[{}], nearbyLabels=[{}]",
                    name, uuid, dist, potCount, inCombat, hasLabel, hasMarker, displayStr, passStr, nearbyStr);
        }

        if (count == 0) {
            feedback.accept(Text.literal("§7No other players found within 32 blocks."));
        }
        feedback.accept(Text.literal("§6=============================="));
    }
}
