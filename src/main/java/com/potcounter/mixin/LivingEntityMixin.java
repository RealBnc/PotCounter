package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"), require = 1)
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        handleDeath();
    }

    @Inject(method = "setHealth", at = @At("HEAD"), require = 1)
    private void onSetHealth(float health, CallbackInfo ci) {
        if (health <= 0.0f) {
            handleDeath();
        }
    }

    private void handleDeath() {
        if (!PotCounterMod.isModActive()) return;
        if ((Object) this instanceof PlayerEntity player) {
            PotCounterMod mod = PotCounterMod.getInstance();
            if (mod != null) {
                mod.getCombatManager().handlePlayerDeath(player.getUuid());
            }
        }
    }
}
