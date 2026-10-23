package com.potcounter.gui;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.render.ColorHelper;
import com.potcounter.render.TextHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MenuAppearanceScreen extends Screen {
    private final Screen parent;
    private final CounterConfig config;

    // Snapshot for Cancel functionality
    private final int origX, origY, origW, origH;
    private final float origScale;
    private final boolean origBg, origBorder, origAuto, origShowTitle;
    private final float origBgOpacity;
    private final int origTheme, origPad, origSpacing;

    private boolean isDragging = false;
    private boolean isResizing = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public MenuAppearanceScreen(Screen parent) {
        super(Text.literal("HUD Menu Appearance Editor"));
        this.parent = parent;
        this.config = PotCounterMod.getInstance().getConfig();

        this.origX = config.getMenuX();
        this.origY = config.getMenuY();
        this.origW = config.getMenuWidth();
        this.origH = config.getMenuHeight();
        this.origScale = config.getMenuScale();
        this.origBg = config.isMenuBackgroundEnabled();
        this.origBorder = config.isMenuBorderEnabled();
        this.origAuto = config.isMenuAutoColor();
        this.origShowTitle = config.isMenuShowTitle();
        this.origBgOpacity = config.getMenuBackgroundOpacity();
        this.origTheme = config.getMenuThemeIndex();
        this.origPad = config.getMenuPadding();
        this.origSpacing = config.getMenuLineSpacing();
    }

    @Override
    protected void init() {
        if (config.getMenuX() < 0) {
            config.setMenuX(this.width - (int)(config.getMenuWidth() * config.getMenuScale()) - 20);
        }

        int panelY = this.height - 110;
        int btnWidth = 115;
        int btnHeight = 20;

        // Row 1: Theme Preset, Scale, Width, Height
        ButtonWidget themeBtn = ButtonWidget.builder(
                getThemeButtonText(),
                button -> {
                    int next = (config.getMenuThemeIndex() + 1) % 4;
                    config.applyTheme(next);
                    button.setMessage(getThemeButtonText());
                }
        ).dimensions(20, panelY, btnWidth, btnHeight).build();
        addDrawableChild(themeBtn);

        CustomSliderWidget scaleSlider = new CustomSliderWidget(
                142, panelY, btnWidth, btnHeight,
                "Scale", "x", 0.7, 1.5, config.getMenuScale(),
                val -> config.setMenuScale(val.floatValue())
        );
        addDrawableChild(scaleSlider);

        CustomSliderWidget widthSlider = new CustomSliderWidget(
                264, panelY, btnWidth, btnHeight,
                "Width", "px", 90, 240, config.getMenuWidth(),
                val -> config.setMenuWidth(val.intValue())
        );
        addDrawableChild(widthSlider);

        CustomSliderWidget heightSlider = new CustomSliderWidget(
                386, panelY, btnWidth, btnHeight,
                "Height", "px", 60, 250, config.getMenuHeight(),
                val -> config.setMenuHeight(val.intValue())
        );
        addDrawableChild(heightSlider);

        // Row 2: Background, Border, Title Toggle, Opacity
        int row2Y = panelY + 24;

        ButtonWidget bgToggle = ButtonWidget.builder(
                Text.literal("Bg: " + (config.isMenuBackgroundEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    config.setMenuBackgroundEnabled(!config.isMenuBackgroundEnabled());
                    button.setMessage(Text.literal("Bg: " + (config.isMenuBackgroundEnabled() ? "§aON" : "§cOFF")));
                }
        ).dimensions(20, row2Y, btnWidth, btnHeight).build();
        addDrawableChild(bgToggle);

        ButtonWidget borderToggle = ButtonWidget.builder(
                Text.literal("Border: " + (config.isMenuBorderEnabled() ? "§aON" : "§cOFF")),
                button -> {
                    config.setMenuBorderEnabled(!config.isMenuBorderEnabled());
                    button.setMessage(Text.literal("Border: " + (config.isMenuBorderEnabled() ? "§aON" : "§cOFF")));
                }
        ).dimensions(142, row2Y, btnWidth, btnHeight).build();
        addDrawableChild(borderToggle);

        ButtonWidget titleToggle = ButtonWidget.builder(
                Text.literal("Title: " + (config.isMenuShowTitle() ? "§aON" : "§cOFF")),
                button -> {
                    config.setMenuShowTitle(!config.isMenuShowTitle());
                    button.setMessage(Text.literal("Title: " + (config.isMenuShowTitle() ? "§aON" : "§cOFF")));
                }
        ).dimensions(264, row2Y, btnWidth, btnHeight).build();
        addDrawableChild(titleToggle);

        CustomSliderWidget opacitySlider = new CustomSliderWidget(
                386, row2Y, btnWidth, btnHeight,
                "Opacity", "%", 10.0, 100.0, config.getMenuBackgroundOpacity() * 100.0,
                val -> config.setMenuBackgroundOpacity((float) (val / 100.0))
        );
        addDrawableChild(opacitySlider);

        // Row 3: Reset, Cancel, Save
        int row3Y = row2Y + 24;

        ButtonWidget resetBtn = ButtonWidget.builder(
                Text.literal("Reset Defaults"),
                button -> {
                    config.resetMenuToDefaults();
                    if (config.getMenuX() < 0) {
                        config.setMenuX(this.width - config.getMenuWidth() - 20);
                    }
                    clearChildren();
                    init();
                }
        ).dimensions(20, row3Y, btnWidth, btnHeight).build();
        addDrawableChild(resetBtn);

        ButtonWidget cancelBtn = ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> {
                    config.setMenuX(origX);
                    config.setMenuY(origY);
                    config.setMenuWidth(origW);
                    config.setMenuHeight(origH);
                    config.setMenuScale(origScale);
                    config.setMenuBackgroundEnabled(origBg);
                    config.setMenuBorderEnabled(origBorder);
                    config.setMenuAutoColor(origAuto);
                    config.setMenuShowTitle(origShowTitle);
                    config.setMenuBackgroundOpacity(origBgOpacity);
                    config.applyTheme(origTheme);
                    config.setMenuPadding(origPad);
                    config.setMenuLineSpacing(origSpacing);
                    close();
                }
        ).dimensions(264, row3Y, btnWidth, btnHeight).build();
        addDrawableChild(cancelBtn);

        ButtonWidget saveBtn = ButtonWidget.builder(
                Text.literal("§6✔ Save & Apply"),
                button -> {
                    PotCounterMod.getInstance().getConfigManager().save();
                    close();
                }
        ).dimensions(386, row3Y, btnWidth, btnHeight).build();
        addDrawableChild(saveBtn);
    }

    private Text getThemeButtonText() {
        return Text.literal("Palette: §e" + CounterConfig.getThemeName(config.getMenuThemeIndex()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int mx = config.getMenuX();
            int my = config.getMenuY();
            float scale = config.getMenuScale();
            int mw = (int)(config.getMenuWidth() * scale);
            int mh = (int)(config.getMenuHeight() * scale);

            // Check resize handle (14x14 corner at bottom right)
            if (mouseX >= mx + mw - 14 && mouseX <= mx + mw + 4 && mouseY >= my + mh - 14 && mouseY <= my + mh + 4) {
                isResizing = true;
                return true;
            }

            // Check drag body
            if (mouseX >= mx && mouseX <= mx + mw && mouseY >= my && mouseY <= my + mh) {
                isDragging = true;
                dragOffsetX = (int) (mouseX - mx);
                dragOffsetY = (int) (mouseY - my);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
            isResizing = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        float scale = config.getMenuScale();

        if (isDragging) {
            int newX = (int) (mouseX - dragOffsetX);
            int newY = (int) (mouseY - dragOffsetY);
            int mw = (int)(config.getMenuWidth() * scale);
            int mh = (int)(config.getMenuHeight() * scale);
            newX = Math.max(0, Math.min(this.width - mw, newX));
            newY = Math.max(0, Math.min(this.height - 120 - mh, newY));
            config.setMenuX(newX);
            config.setMenuY(newY);
            return true;
        }

        if (isResizing) {
            int newWidth = (int) ((mouseX - config.getMenuX()) / scale);
            int newHeight = (int) ((mouseY - config.getMenuY()) / scale);
            config.setMenuWidth(Math.max(90, Math.min(280, newWidth)));
            config.setMenuHeight(Math.max(60, Math.min(300, newHeight)));
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark translucent overlay
        context.fill(0, 0, this.width, this.height, 0x88000000);

        // Control Panel container at bottom (Sleek Black & Gold)
        int panelY = this.height - 116;
        context.fill(10, panelY - 4, 510, this.height - 6, 0xEE111116);
        context.fill(10, panelY - 4, 510, panelY - 2, 0xFFFFD54F); // Gold accent line

        TextHelper.drawText(context, this.textRenderer, Text.literal("§6⚡ HUD Customization Panel §7(Drag window to move, bottom-right corner to resize)"), 18, panelY - 14, 0xFFFFD54F);

        super.render(context, mouseX, mouseY, delta);

        // Render Live Preview
        renderPreviewMenu(context);
    }

    private void renderPreviewMenu(DrawContext context) {
        int x = config.getMenuX();
        int y = config.getMenuY();
        float scale = config.getMenuScale();
        int w = config.getMenuWidth();
        int h = config.getMenuHeight();
        int padding = config.getMenuPadding();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float) x, (float) y);
        context.getMatrices().scale(scale, scale);

        // Background
        if (config.isMenuBackgroundEnabled()) {
            int bg = ColorHelper.applyAlpha(config.getMenuBackgroundColor(), config.getMenuBackgroundOpacity());
            context.fill(0, 0, w, h, bg);
        }

        // Border
        if (config.isMenuBorderEnabled()) {
            int border = config.getMenuBorderColor();
            context.fill(0, 0, w, 2, border); // Gold top accent bar
            context.fill(0, h - 1, w, h, border);
            context.fill(0, 0, 1, h, border);
            context.fill(w - 1, 0, w, h, border);
        }

        int curY = padding + 2;

        // Title
        if (config.isMenuShowTitle()) {
            String title = "⚡ POTS (" + config.getCounterMode().getDisplayName() + ")";
            TextHelper.drawText(context, this.textRenderer, Text.literal(title), padding, curY, config.getMenuBorderColor());
            curY += this.textRenderer.fontHeight + 3;
            context.fill(padding, curY - 1, w - padding, curY, 0x44FFD54F);
            curY += 2;
        }

        // Sample Players (Names clearly shown, NOT numbers!)
        String[] sampleNames = { "RealBnc", "xKralTR", "Opponent" };
        int[] samplePots = { 14, 8, 2 };

        for (int i = 0; i < sampleNames.length; i++) {
            if (curY + this.textRenderer.fontHeight > h - padding) break;

            String name = sampleNames[i];
            String countStr = "-" + samplePots[i];
            int potColor = ColorHelper.getPotColor(samplePots[i], config.isMenuAutoColor(), config.getMenuCustomColor());

            // Name on left (Clean white)
            TextHelper.drawTextWithShadow(context, this.textRenderer, Text.literal(name), padding, curY, 0xFFEDEDED);

            // Count on right
            int countW = this.textRenderer.getWidth(countStr);
            TextHelper.drawTextWithShadow(context, this.textRenderer, Text.literal(countStr), w - padding - countW, curY, potColor | 0xFF000000);

            curY += this.textRenderer.fontHeight + config.getMenuLineSpacing();
        }

        // Resize corner indicator (Golden grip)
        context.fill(w - 7, h - 7, w - 1, h - 1, 0xFFFFD54F);

        context.getMatrices().popMatrix();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
