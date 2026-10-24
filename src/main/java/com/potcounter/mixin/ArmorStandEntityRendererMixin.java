package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterDisplay;
import com.potcounter.config.CounterMode;
import com.potcounter.render.MarkerHelper;
import com.potcounter.render.NametagHelper;
import com.potcounter.tracker.PotTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntityRenderer.class)
public abstract class ArmorStandEntityRendererMixin {

    @Inject(
        method = "hasLabel(Lnet/minecraft/entity/decoration/ArmorStandEntity;D)Z",
        at = @At("HEAD")
    )
    private void onHasLabel(ArmorStandEntity armorStand, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (!PotCounterMod.isModActive()) return;
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod == null) return;

        CounterConfig config = mod.getConfig();
        if (config.getCounterDisplay() != CounterDisplay.NAMETAG) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        PlayerEntity player = NametagHelper.findAssociatedPlayer(armorStand, client.player);
        if (player == null) return;

        PotTracker tracker = mod.getPotTracker();
        boolean isEligible = false;
        if (config.getCounterMode() == CounterMode.SINGLE) {
            isEligible = player.getUuid().equals(tracker.getCurrentActiveOpponent()) || mod.getCombatManager().isOpponent(player.getUuid());
        } else {
            PotTracker.CachedPlayerData data = tracker.getCachedData(player.getUuid());
            isEligible = (data != null && data.shouldDisplay()) || mod.getCombatManager().isOpponent(player.getUuid());
        }

        if (!isEligible) return;

        if (!NametagHelper.containsPlayerName(armorStand, player.getName().getString())) {
            return;
        }

        Text text = armorStand.getCustomName();
        if (text == null || MarkerHelper.hasMarker(text)) {
            return;
        }

        PotTracker.CachedPlayerData data = tracker.getCachedData(player.getUuid());

        if (data == null || data.getCount() <= 0 || data.getSuffixText() == null) {
            return;
        }

        MutableText modified = text.copy().append(data.getSuffixText());
        armorStand.setCustomName(modified);
        tracker.setServerCustomName(player.getUuid(), modified);
        tracker.setServerNametagEntityId(player.getUuid(), armorStand.getId());
    }
}
