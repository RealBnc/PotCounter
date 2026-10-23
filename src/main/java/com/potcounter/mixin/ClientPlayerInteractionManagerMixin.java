package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"), require = 1)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        if (player != null && target instanceof PlayerEntity opponent) {
            PotCounterMod mod = PotCounterMod.getInstance();
            if (mod != null) {
                mod.getCombatManager().recordHit(player.getUuid(), opponent.getUuid(), player.getUuid());
            }
        }
    }

    @Inject(method = "interactItem", at = @At("HEAD"), require = 1)
    private void onInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!PotCounterMod.isModActive()) return;
        if (player != null && hand != null) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack != null && stack.getItem() == Items.SPLASH_POTION) {
                PotCounterMod mod = PotCounterMod.getInstance();
                if (mod != null) {
                    mod.getPotDetectionManager().onLocalPlayerThrowPotion();
                }
            }
        }
    }
}
