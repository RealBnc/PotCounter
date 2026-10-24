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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderLabelIfPresent(AbstractClientPlayerEntity player, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod == null) return;
        if (mod.getConfig().getCounterDisplay() != CounterDisplay.NAMETAG) return;

        Entity serverNametag = NametagHelper.findServerNametagEntity(player);
        if (serverNametag != null) {

            ci.cancel();
        }
    }

    @ModifyVariable(
        method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private Text onModifyNametagText(
            Text text,
            AbstractClientPlayerEntity player,
            Text textArg,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta
    ) {
        if (!PotCounterMod.isModActive()) return text;
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod == null) return text;

        CounterConfig config = mod.getConfig();
        if (config.getCounterDisplay() != CounterDisplay.NAMETAG) return text;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || player.getUuid().equals(client.player.getUuid())) {
            return text;
        }

        PotTracker tracker = mod.getPotTracker();
        boolean isEligible = false;
        if (config.getCounterMode() == CounterMode.SINGLE) {
            isEligible = player.getUuid().equals(tracker.getCurrentActiveOpponent()) || mod.getCombatManager().isOpponent(player.getUuid());
        } else {
            PotTracker.CachedPlayerData data = tracker.getCachedData(player.getUuid());
            isEligible = (data != null && data.shouldDisplay()) || mod.getCombatManager().isOpponent(player.getUuid());
        }

        if (!isEligible) return text;

        PotTracker.CachedPlayerData data = tracker.getCachedData(player.getUuid());
        if (data == null || data.getCount() <= 0 || data.getSuffixText() == null) {
            return text;
        }

        if (MarkerHelper.hasMarker(text)) return text;
        return MarkerHelper.mark(text.copy().append(data.getSuffixText()));
    }
}
