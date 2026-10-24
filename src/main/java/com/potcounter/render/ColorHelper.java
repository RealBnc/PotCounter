package com.potcounter.render;

public final class ColorHelper {
    private ColorHelper() {}

    private static final int COLOR_0 = 0x00FF00;
    private static final int COLOR_5 = 0xFFFF55;
    private static final int COLOR_10 = 0xFF5555;
    private static final int COLOR_16 = 0xFF0000;

    public static int getPotColor(int count, boolean autoColor, int customColor) {
        if (!autoColor) {
            return customColor;
        }

        if (count <= 0) {
            return COLOR_0;
        }
        if (count < 5) {
            float t = count / 5.0f;
            return interpolate(COLOR_0, COLOR_5, t);
        }
        if (count < 10) {
            float t = (count - 5) / 5.0f;
            return interpolate(COLOR_5, COLOR_10, t);
        }
        if (count < 16) {
            float t = (count - 10) / 6.0f;
            return interpolate(COLOR_10, COLOR_16, t);
        }
        return COLOR_16;
    }

    public static int interpolate(int colorA, int colorB, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));

        int rA = (colorA >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF;
        int bA = colorA & 0xFF;

        int rB = (colorB >> 16) & 0xFF;
        int gB = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;

        int r = Math.round(rA + t * (rB - rA));
        int g = Math.round(gA + t * (gB - gA));
        int b = Math.round(bA + t * (bB - bA));

        return (r << 16) | (g << 8) | b;
    }

    public static int applyAlpha(int rgb, float alpha) {
        int a = Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }
}
