package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterDisplay;
import com.potcounter.config.CounterMode;
import com.potcounter.render.MarkerHelper;
import com.potcounter.render.NametagHelper;
import com.potcounter.tracker.PotTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void onUpdateRenderState(AbstractClientPlayerEntity entity, PlayerEntityRenderState state, float tickProgress, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod == null) return;

        CounterConfig config = mod.getConfig();
        if (config.getCounterDisplay() != CounterDisplay.NAMETAG) {
            return;
        }

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || player.getUuid().equals(client.player.getUuid())) {
            return;
        }

        PotTracker tracker = mod.getPotTracker();
        boolean isEligible = false;
        if (config.getCounterMode() == CounterMode.SINGLE) {
            isEligible = player.getUuid().equals(tracker.getCurrentActiveOpponent()) || mod.getCombatManager().isOpponent(player.getUuid());
        } else {
            PotTracker.CachedPlayerData data = tracker.getCachedData(player.getUuid());
            isEligible = (data != null && data.shouldDisplay()) || mod.getCombatManager().isOpponent(player.getUuid());
        }

        if (!isEligible) {
            return;
        }

        Entity serverNametagEntity = NametagHelper.findServerNametagEntity(player);
        if (serverNametagEntity != null) {

            state.playerName = null;
            state.displayName = null;
            return;
        }

        if (state.squaredDistanceToCamera > 65536.0) {
            return;
        }

        Text serverNametag = resolveServerDisplayName(player);
        PotTracker.CachedPlayerData data = tracker.getCachedData(player.getUuid());
        MutableText suffix = (data != null && data.getCount() > 0) ? data.getSuffixText() : null;

        MutableText customNametag;
        if (MarkerHelper.hasMarker(serverNametag)) {
            customNametag = serverNametag.copy();
        } else if (suffix != null) {
            customNametag = serverNametag.copy().append(suffix);
        } else {
            customNametag = serverNametag.copy();
        }

        state.displayName = customNametag;

        if (state.nameLabelPos == null) {
            Vec3d pos = player.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, player.getLerpedYaw(tickProgress));
            if (pos == null) {
                pos = new Vec3d(0.0, player.getEyeHeight(player.getPose()) + 0.5, 0.0);
            }
            state.nameLabelPos = pos;
        }
    }

    @Unique
    private static Text resolveServerDisplayName(PlayerEntity player) {
        Text displayName = player.getDisplayName();
        if (displayName != null && !displayName.getString().trim().isEmpty() && !MarkerHelper.hasMarker(displayName)) {
            return displayName;
        }
        return player.getName();
    }
}
