package com.potcounter.render;

import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterDisplay;
import com.potcounter.tracker.PotTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;

public class CounterHudRenderer {

    public static void render(DrawContext context, PotTracker tracker, CounterConfig config) {
        if (config == null || tracker == null || config.getCounterDisplay() != CounterDisplay.MENU) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        UUID localUuid = client.player.getUuid();
        Map<UUID, Integer> entries = tracker.getActiveEntries(localUuid);
        if (entries == null || entries.isEmpty()) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        float scale = config.getMenuScale() <= 0.1f ? 1.0f : config.getMenuScale();
        int baseWidth = Math.max(90, config.getMenuWidth());
        int baseHeight = Math.max(30, config.getMenuHeight());

        int x = config.getMenuX();
        int y = config.getMenuY();

        if (x < 0) {

            x = screenWidth - (int)(baseWidth * scale) - 10;
        }
        if (y < 0) {
            y = 10;
        }

        TextRenderer tr = client.textRenderer;
        int padding = Math.max(2, config.getMenuPadding());
        int lineSpacing = Math.max(1, config.getMenuLineSpacing());

        context.getMatrices().push();
        context.getMatrices().translate((double) x, (double) y, 0.0);
        context.getMatrices().scale(scale, scale, 1.0f);

        int w = baseWidth;
        int h = baseHeight;

        if (config.isMenuBackgroundEnabled()) {
            int bg = ColorHelper.applyAlpha(config.getMenuBackgroundColor(), config.getMenuBackgroundOpacity());
            context.fill(0, 0, w, h, bg);
        }

        if (config.isMenuBorderEnabled()) {
            int border = config.getMenuBorderColor();

            context.fill(0, 0, w, 2, border);

            context.fill(0, h - 1, w, h, border);
            context.fill(0, 0, 1, h, border);
            context.fill(w - 1, 0, w, h, border);
        }

        int contentY = padding + 2;

        if (config.isMenuShowTitle()) {
            String title = "⚡ POTS (" + config.getCounterMode().getDisplayName() + ")";
            TextHelper.drawText(context, tr, Text.literal(title), padding, contentY, config.getMenuBorderColor());
            contentY += tr.fontHeight + 3;

            context.fill(padding, contentY - 1, w - padding, contentY, 0x44FFD54F);
            contentY += 2;
        }

        int rowHeight = tr.fontHeight + lineSpacing;

        for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
            if (contentY + tr.fontHeight > h - padding) {
                break;
            }

            UUID uuid = entry.getKey();
            int count = entry.getValue();

            String name = tracker.getPlayerName(uuid);
            if (name == null || name.trim().isEmpty()) {
                name = "Opponent";
            }
            String countStr = "-" + count;

            int potColor = ColorHelper.getPotColor(count, config.isMenuAutoColor(), config.getMenuCustomColor());

            TextHelper.drawTextWithShadow(context, tr, Text.literal(name), padding, contentY, 0xFFEDEDED);

            int countWidth = tr.getWidth(countStr);
            int countX = w - padding - countWidth;
            TextHelper.drawTextWithShadow(context, tr, Text.literal(countStr), countX, contentY, potColor | 0xFF000000);

            contentY += rowHeight;
        }

        context.getMatrices().pop();
    }
}
