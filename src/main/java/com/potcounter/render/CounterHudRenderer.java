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
    private static int scrollOffset = 0;

    public static void render(DrawContext context, PotTracker tracker, CounterConfig config) {
        if (config.getCounterDisplay() != CounterDisplay.MENU) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        UUID localUuid = client.player.getUuid();
        Map<UUID, Integer> entries = tracker.getActiveEntries(localUuid);
        if (entries.isEmpty()) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        float scale = config.getMenuScale();
        int baseWidth = config.getMenuWidth();
        int baseHeight = config.getMenuHeight();

        int x = config.getMenuX();
        int y = config.getMenuY();

        if (x < 0) {

            x = screenWidth - (int)(baseWidth * scale) - 10;
        }

        TextRenderer tr = client.textRenderer;
        int padding = config.getMenuPadding();
        int lineSpacing = config.getMenuLineSpacing();

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
        int availableHeight = h - contentY - padding;

        boolean scissorActive = false;
        try {
            int screenX = (int)(x);
            int screenY = (int)(y + contentY * scale);
            int screenW = (int)(w * scale);
            int screenH = (int)(availableHeight * scale);
            context.enableScissor(screenX, screenY, screenX + screenW, screenY + screenH);
            scissorActive = true;
        } catch (Throwable ignored) {}

        int currentY = contentY - scrollOffset;

        for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
            UUID uuid = entry.getKey();
            int count = entry.getValue();

            String name = tracker.getPlayerName(uuid);
            String countStr = "-" + count;

            int potColor = ColorHelper.getPotColor(count, config.isMenuAutoColor(), config.getMenuCustomColor());

            if (currentY + tr.fontHeight >= contentY && currentY <= h - padding) {

                TextHelper.drawTextWithShadow(context, tr, Text.literal(name), padding, currentY, 0xFFEDEDED);

                int countWidth = tr.getWidth(countStr);
                int countX = w - padding - countWidth;
                TextHelper.drawTextWithShadow(context, tr, Text.literal(countStr), countX, currentY, potColor | 0xFF000000);
            }

            currentY += rowHeight;
        }

        if (scissorActive) {
            try {
                context.disableScissor();
            } catch (Throwable ignored) {}
        }

        context.getMatrices().pop();
    }

    public static void handleScroll(double amount, int totalEntries, int availableHeight, int rowHeight) {
        int totalHeight = totalEntries * rowHeight;
        int maxScroll = Math.max(0, totalHeight - availableHeight);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - (amount * 10)));
    }
}
