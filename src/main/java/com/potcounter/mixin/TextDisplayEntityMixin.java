package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterDisplay;
import com.potcounter.config.CounterMode;
import com.potcounter.render.MarkerHelper;
import com.potcounter.render.NametagHelper;
import com.potcounter.tracker.PotTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextDisplayEntityMixin {

    @Inject(
        method = "splitLines(Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity$LineSplitter;)Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity$TextLines;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSplitLines(DisplayEntity.TextDisplayEntity.LineSplitter lineSplitter, CallbackInfoReturnable<DisplayEntity.TextDisplayEntity.TextLines> cir) {
        if (!PotCounterMod.isModActive()) return;
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod == null) return;

        CounterConfig config = mod.getConfig();
        if (config.getCounterDisplay() != CounterDisplay.NAMETAG) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) return;

        DisplayEntity.TextDisplayEntity entity = (DisplayEntity.TextDisplayEntity) (Object) this;
        PlayerEntity targetPlayer = NametagHelper.findAssociatedPlayer(entity, client.player);
        if (targetPlayer == null) return;

        PotTracker tracker = mod.getPotTracker();
        boolean isEligible = false;
        if (config.getCounterMode() == CounterMode.SINGLE) {
            isEligible = targetPlayer.getUuid().equals(tracker.getCurrentActiveOpponent()) || mod.getCombatManager().isOpponent(targetPlayer.getUuid());
        } else {
            PotTracker.CachedPlayerData cData = tracker.getCachedData(targetPlayer.getUuid());
            isEligible = (cData != null && cData.shouldDisplay()) || mod.getCombatManager().isOpponent(targetPlayer.getUuid());
        }

        if (!isEligible) return;
        if (!NametagHelper.containsPlayerName(entity, targetPlayer.getName().getString())) return;

        DisplayEntity.TextDisplayEntity.Data data = entity.getData();
        if (data == null || data.text() == null) return;

        Text originalText = data.text();
        if (MarkerHelper.hasMarker(originalText)) return;

        PotTracker.CachedPlayerData pData = tracker.getCachedData(targetPlayer.getUuid());
        if (pData == null || pData.getCount() <= 0 || pData.getSuffixText() == null) {
            return;
        }

        MutableText modifiedText = MarkerHelper.mark(originalText.copy().append(pData.getSuffixText()));
        tracker.setServerCustomName(targetPlayer.getUuid(), modifiedText);
        tracker.setServerNametagEntityId(targetPlayer.getUuid(), entity.getId());

        DisplayEntity.TextDisplayEntity.TextLines customLines = lineSplitter.split(modifiedText, data.lineWidth());
        cir.setReturnValue(customLines);
    }
}
