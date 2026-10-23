package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterDisplay;
import com.potcounter.config.CounterMode;
import com.potcounter.debug.DiagnosticsTracker;
import com.potcounter.render.MarkerHelper;
import com.potcounter.render.NametagHelper;
import com.potcounter.tracker.PotTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.render.entity.state.TextDisplayEntityRenderState;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayEntityRenderer.TextDisplayEntityRenderer.class)
public abstract class TextDisplayEntityRendererMixin {

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity;Lnet/minecraft/client/render/entity/state/TextDisplayEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void onUpdateRenderState(DisplayEntity.TextDisplayEntity entity, TextDisplayEntityRenderState state, float tickProgress, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        DiagnosticsTracker.textDisplayHookCalls.incrementAndGet();

        if (state.data == null || state.data.text() == null) {
            return;
        }

        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod == null) return;

        CounterConfig config = mod.getConfig();
        if (config.getCounterDisplay() != CounterDisplay.NAMETAG) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) {
            return;
        }

        PlayerEntity targetPlayer = NametagHelper.findAssociatedPlayer(entity, client.player);
        if (targetPlayer == null) {
            return;
        }

        PotTracker tracker = mod.getPotTracker();
        boolean isEligible = false;
        if (config.getCounterMode() == CounterMode.SINGLE) {
            isEligible = targetPlayer.getUuid().equals(tracker.getCurrentActiveOpponent()) || mod.getCombatManager().isOpponent(targetPlayer.getUuid());
        } else {
            PotTracker.CachedPlayerData data = tracker.getCachedData(targetPlayer.getUuid());
            isEligible = (data != null && data.shouldDisplay()) || mod.getCombatManager().isOpponent(targetPlayer.getUuid());
        }

        if (!isEligible) {
            return;
        }

        // Only target the line that directly contains the player's name
        if (!NametagHelper.containsPlayerName(entity, targetPlayer.getName().getString())) {
            return;
        }

        Text originalText = state.data.text();
        if (MarkerHelper.hasMarker(originalText)) {
            return;
        }

        PotTracker.CachedPlayerData data = tracker.getCachedData(targetPlayer.getUuid());
        // Do not display counter suffix if player has not thrown any pots yet (starts directly from -1)
        if (data == null || data.getCount() <= 0 || data.getSuffixText() == null) {
            return;
        }

        MutableText modifiedText = originalText.copy().append(data.getSuffixText());

        DisplayEntity.TextDisplayEntity.Data newData = new DisplayEntity.TextDisplayEntity.Data(
                modifiedText,
                state.data.lineWidth(),
                state.data.textOpacity(),
                state.data.backgroundColor(),
                state.data.flags()
        );
        state.data = newData;
        state.textLines = invokeGetLines(modifiedText, newData.lineWidth());

        tracker.setServerCustomName(targetPlayer.getUuid(), modifiedText);
        tracker.setServerNametagEntityId(targetPlayer.getUuid(), entity.getId());
    }

    @Invoker("getLines")
    protected abstract DisplayEntity.TextDisplayEntity.TextLines invokeGetLines(Text text, int lineWidth);
}
