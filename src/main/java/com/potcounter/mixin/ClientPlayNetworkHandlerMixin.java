package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import com.potcounter.debug.DiagnosticsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntitySpawn", at = @At("RETURN"), require = 1)
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        EntityType<?> type = packet.getEntityType();
        if (this.world != null && isPotionEntityType(type)) {
            DiagnosticsTracker.potionSpawnCalls.incrementAndGet();
            PotCounterMod mod = PotCounterMod.getInstance();
            if (mod != null) {
                mod.getPotDetectionManager().onPotionSpawnPacket(packet, this.world);
            }
        }
    }

    @Unique
    private static boolean isPotionEntityType(EntityType<?> type) {
        if (type == null) return false;
        Identifier id = Registries.ENTITY_TYPE.getId(type);
        if (id == null) return false;
        String path = id.getPath();
        return "splash_potion".equals(path) || "potion".equals(path);
    }

    @Inject(method = "onEntityTrackerUpdate", at = @At("RETURN"), require = 1)
    private void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        if (this.world != null) {
            PotCounterMod mod = PotCounterMod.getInstance();
            if (mod != null) {
                mod.getPotDetectionManager().onEntityTrackerUpdate(packet, this.world);
            }
        }
    }

    @Inject(method = "onEntityAnimation", at = @At("RETURN"), require = 1)
    private void onEntityAnimation(EntityAnimationS2CPacket packet, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        if (packet.getAnimationId() == 0 && this.world != null) { // SWING_MAIN_HAND
            PotCounterMod mod = PotCounterMod.getInstance();
            if (mod != null) {
                mod.getPotDetectionManager().onPlayerArmSwing(packet.getEntityId());
            }
        }
    }

    @Inject(method = "onEntityDamage", at = @At("RETURN"), require = 1)
    private void onEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        DiagnosticsTracker.damagePacketCalls.incrementAndGet();
        if (this.world != null) {
            Entity victim = this.world.getEntityById(packet.entityId());
            Entity attacker = this.world.getEntityById(packet.sourceCauseId());

            if (victim instanceof PlayerEntity && attacker instanceof PlayerEntity) {
                PotCounterMod mod = PotCounterMod.getInstance();
                if (mod != null) {
                    UUID localUuid = null;
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        localUuid = client.player.getUuid();
                    }
                    mod.getCombatManager().recordHit(attacker.getUuid(), victim.getUuid(), localUuid);
                }
            }
        }
    }

    @Inject(method = "onHealthUpdate", at = @At("RETURN"), require = 1)
    private void onHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        if (packet.getHealth() <= 0.0f) {
            PotCounterMod mod = PotCounterMod.getInstance();
            if (mod != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    mod.getCombatManager().handlePlayerDeath(client.player.getUuid());
                }
            }
        }
    }

    @Inject(method = "onEntityStatus", at = @At("RETURN"), require = 1)
    private void onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        if ((packet.getStatus() == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES || packet.getStatus() == EntityStatuses.ADD_DEATH_PARTICLES) && this.world != null) {
            Entity entity = packet.getEntity(this.world);
            if (entity instanceof PlayerEntity player) {
                PotCounterMod mod = PotCounterMod.getInstance();
                if (mod != null) {
                    mod.getCombatManager().handlePlayerDeath(player.getUuid());
                }
            }
        }
    }

    @Inject(method = "onGameJoin", at = @At("RETURN"), require = 1)
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        resetModState("Game join / Server connect");
    }

    @Inject(method = "onPlayerRespawn", at = @At("RETURN"), require = 1)
    private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        resetModState("Player respawn / Dimension change");
    }

    @Inject(method = "clearWorld", at = @At("RETURN"), require = 1)
    private void onClearWorld(CallbackInfo ci) {
        resetModState("World cleared / Disconnect");
    }

    private void resetModState(String reason) {
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod != null) {
            PotCounterMod.LOGGER.info("Resetting combat and pot tracker state: {}", reason);
            mod.getCombatManager().clear();
            mod.getPotTracker().resetAll();
            mod.getPotDetectionManager().clear();
        }
    }
}
