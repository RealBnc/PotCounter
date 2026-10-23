package com.potcounter.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Direct Minecraft 1.21.11 DrawContext text rendering helper.
 * Zero reflection, zero overhead.
 */
public class TextHelper {

    public static void drawText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow) {
        if (context == null || textRenderer == null || text == null) return;
        context.drawText(textRenderer, text, x, y, color, shadow);
    }

    public static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        if (context == null || textRenderer == null || text == null) return;
        context.drawText(textRenderer, text, x, y, color, shadow);
    }

    public static void drawText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color) {
        drawText(context, textRenderer, text, x, y, color, false);
    }

    public static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        drawText(context, textRenderer, text, x, y, color, false);
    }

    public static void drawTextWithShadow(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color) {
        if (context == null || textRenderer == null || text == null) return;
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    public static void drawTextWithShadow(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        if (context == null || textRenderer == null || text == null) return;
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    public static void drawCenteredTextWithShadow(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        if (context == null || textRenderer == null || text == null) return;
        context.drawCenteredTextWithShadow(textRenderer, text, centerX, y, color);
    }

    public static void drawCenteredTextWithShadow(DrawContext context, TextRenderer textRenderer, String text, int centerX, int y, int color) {
        if (context == null || textRenderer == null || text == null) return;
        context.drawCenteredTextWithShadow(textRenderer, text, centerX, y, color);
    }
}
