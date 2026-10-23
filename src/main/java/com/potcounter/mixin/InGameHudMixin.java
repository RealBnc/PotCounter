package com.potcounter.mixin;

import com.potcounter.PotCounterMod;
import com.potcounter.render.CounterHudRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"), require = 1)
    private void onRenderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!PotCounterMod.isModActive()) return;
        PotCounterMod mod = PotCounterMod.getInstance();
        if (mod != null) {
            CounterHudRenderer.render(context, mod.getPotTracker(), mod.getConfig());
        }
    }
}
