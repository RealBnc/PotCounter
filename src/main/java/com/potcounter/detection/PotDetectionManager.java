package com.potcounter.detection;

import com.potcounter.PotCounterMod;
import com.potcounter.combat.CombatManager;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterMode;
import com.potcounter.sound.SoundManager;
import com.potcounter.tracker.PotTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PotDetectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PotCounter/Detection");

    private final PotTracker potTracker;
    private final CombatManager combatManager;
    private final SoundManager soundManager;

    private final Set<Integer> processedPotionEntityIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private volatile long lastLocalPlayerThrowTime = 0;

    private final Map<Integer, Long> playerArmSwingTimes = new ConcurrentHashMap<>();

    private static class PendingPotionInfo {
        final double x, y, z;
        final Vec3d velocity;
        final int ownerId;
        final long timestamp;

        PendingPotionInfo(double x, double y, double z, Vec3d velocity, int ownerId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.velocity = velocity;
            this.ownerId = ownerId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final Map<Integer, PendingPotionInfo> pendingPotions = new ConcurrentHashMap<>();

    public PotDetectionManager(PotTracker potTracker, CombatManager combatManager, SoundManager soundManager) {
        this.potTracker = potTracker;
        this.combatManager = combatManager;
        this.soundManager = soundManager;
    }

    public void onLocalPlayerThrowPotion() {
        this.lastLocalPlayerThrowTime = System.currentTimeMillis();
    }

    public void onPlayerArmSwing(int entityId) {
        this.playerArmSwingTimes.put(entityId, System.currentTimeMillis());
    }

    public static boolean isHealing2Potion(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() != Items.SPLASH_POTION) {
            return false;
        }

        PotionContentsComponent comp = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (comp == null) {
            return false;
        }

        if (comp.potion().isPresent()) {
            RegistryEntry<Potion> entry = comp.potion().get();
            if (entry.matches(Potions.STRONG_HEALING)) {
                return true;
            }
        }

        for (StatusEffectInstance effect : comp.getEffects()) {
            if (effect.getEffectType().equals(StatusEffects.INSTANT_HEALTH) && effect.getAmplifier() >= 1) {
                return true;
            }
        }

        return false;
    }

    public void onPotionSpawnPacket(EntitySpawnS2CPacket packet, ClientWorld world) {
        if (packet == null || world == null) {
            return;
        }

        int entityId = packet.getEntityId();
        if (processedPotionEntityIds.contains(entityId)) {
            return;
        }

        Vec3d vel = new Vec3d(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
        if (vel == null || (vel.x == 0.0 && vel.y == 0.0 && vel.z == 0.0)) {
            return;
        }

        Entity entity = world.getEntityById(entityId);
        if (entity instanceof ThrownItemEntity thrownItem) {
            ItemStack stack = thrownItem.getStack();
            if (isHealing2Potion(stack)) {
                processedPotionEntityIds.add(entityId);
                resolveAndProcess(packet.getX(), packet.getY(), packet.getZ(), vel, packet.getEntityData(), world);
                return;
            }
        }

        pendingPotions.put(entityId, new PendingPotionInfo(packet.getX(), packet.getY(), packet.getZ(), vel, packet.getEntityData()));
    }

    public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, ClientWorld world) {
        if (packet == null || world == null) {
            return;
        }

        int entityId = packet.id();
        PendingPotionInfo pending = pendingPotions.get(entityId);
        if (pending == null || processedPotionEntityIds.contains(entityId)) {
            return;
        }

        ItemStack foundStack = null;
        for (DataTracker.SerializedEntry<?> entry : packet.trackedValues()) {
            if (entry.value() instanceof ItemStack is) {
                foundStack = is;
                break;
            }
        }

        if (foundStack == null) {
            Entity entity = world.getEntityById(entityId);
            if (entity instanceof ThrownItemEntity thrownItem) {
                foundStack = thrownItem.getStack();
            }
        }

        if (foundStack != null) {
            pendingPotions.remove(entityId);

            if (isHealing2Potion(foundStack)) {
                processedPotionEntityIds.add(entityId);
                resolveAndProcess(pending.x, pending.y, pending.z, pending.velocity, pending.ownerId, world);
            } else {
                LOGGER.info("[PotCounter] Ignored non-health-2 potion item id {}", entityId);
            }
        }
    }

    private void resolveAndProcess(double px, double py, double pz, Vec3d velocity, int ownerId, ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        PlayerEntity thrower = null;
        long now = System.currentTimeMillis();

        if (ownerId > 0) {
            Entity owner = world.getEntityById(ownerId);
            if (owner instanceof PlayerEntity player) {
                thrower = player;
            }
        }

        boolean localPlayerThrew = (now - lastLocalPlayerThrowTime < 500) && (client.player.squaredDistanceTo(px, py, pz) < 16.0);

        UUID oppUuid = combatManager.getCurrentActiveOpponent();
        PlayerEntity opp = (oppUuid != null) ? world.getPlayerByUuid(oppUuid) : null;

        if (thrower == null) {
            if (opp != null) {

                if (localPlayerThrew) {
                    thrower = client.player;
                } else {
                    Long oppSwing = playerArmSwingTimes.get(opp.getId());
                    boolean oppSwung = (oppSwing != null && (now - oppSwing < 600));

                    Vec3d lookVec = opp.getRotationVec(1.0F);
                    double dot = (velocity != null && velocity.lengthSquared() > 0.001)
                            ? velocity.normalize().dotProduct(lookVec)
                            : 0.0;

                    if (oppSwung || dot > 0.4 || opp.squaredDistanceTo(px, py, pz) < client.player.squaredDistanceTo(px, py, pz)) {
                        thrower = opp;
                    } else {
                        thrower = client.player;
                    }
                }
            } else {

                if (localPlayerThrew) {
                    thrower = client.player;
                } else {
                    PlayerEntity bestPlayer = null;
                    double bestScore = -999.0;

                    for (PlayerEntity p : world.getPlayers()) {
                        double distSq = p.squaredDistanceTo(px, py, pz);
                        if (distSq > 36.0) continue;

                        double score = 10.0 - Math.sqrt(distSq);

                        Long swing = playerArmSwingTimes.get(p.getId());
                        if (swing != null && now - swing < 500) {
                            score += 8.0;
                        }

                        if (velocity != null && velocity.lengthSquared() > 0.001) {
                            double dot = velocity.normalize().dotProduct(p.getRotationVec(1.0F));
                            score += (dot * 5.0);
                        }

                        if (score > bestScore) {
                            bestScore = score;
                            bestPlayer = p;
                        }
                    }
                    thrower = bestPlayer;
                }
            }
        }

        if (thrower == null) {
            return;
        }

        processPotionThrow(thrower, client);
    }

    private void processPotionThrow(PlayerEntity thrower, MinecraftClient client) {
        UUID throwerUuid = thrower.getUuid();
        UUID localUuid = client.player.getUuid();
        boolean isLocalPlayer = throwerUuid.equals(localUuid);
        String throwerName = thrower.getName().getString();

        CounterConfig config = PotCounterMod.getInstance().getConfig();
        CounterMode mode = config.getCounterMode();
        UUID activeOpponent = combatManager.getCurrentActiveOpponent();

        if (mode == CounterMode.SINGLE && !isLocalPlayer) {
            if (activeOpponent == null || !activeOpponent.equals(throwerUuid)) {
                return;
            }
        }

        potTracker.updatePlayerName(throwerUuid, throwerName);
        potTracker.recordPot(throwerUuid, localUuid);

        if (!isLocalPlayer && (activeOpponent != null && activeOpponent.equals(throwerUuid))) {
            soundManager.playPotSound();
        }

        LOGGER.info("[PotCounter] Verified and counted Splash Health II for {}", throwerName);
    }

    public void tick() {
        long now = System.currentTimeMillis();
        playerArmSwingTimes.entrySet().removeIf(entry -> now - entry.getValue() > 2000);

        pendingPotions.entrySet().removeIf(entry -> now - entry.getValue().timestamp > 1500);

        if (processedPotionEntityIds.size() > 500) {
            processedPotionEntityIds.clear();
        }
    }

    public void clear() {
        processedPotionEntityIds.clear();
        pendingPotions.clear();
        playerArmSwingTimes.clear();
        lastLocalPlayerThrowTime = 0;
    }
}
