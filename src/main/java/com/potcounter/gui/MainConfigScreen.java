package com.potcounter.gui;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.config.CounterDisplay;
import com.potcounter.config.CounterMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MainConfigScreen extends Screen {
    private static final Identifier LOGO_TEXTURE = Identifier.of("potcounter", "textures/gui/logo.png");

    private final Screen parent;
    private final CounterConfig config;

    private ButtonWidget activeButton;
    private ButtonWidget soundToggleButton;
    private ButtonWidget counterDisplayButton;
    private ButtonWidget soundsButton;
    private ButtonWidget menuAppearanceButton;
    private ButtonWidget counterModeButton;
    private ButtonWidget doneButton;

    public MainConfigScreen(Screen parent) {
        super(Text.literal("Pot Counter Settings"));
        this.parent = parent;
        this.config = PotCounterMod.getInstance().getConfig();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        int colWidth = 145;
        int btnHeight = 20;
        int leftColX = centerX - colWidth - 5;
        int rightColX = centerX + 5;
        int centerBtnWidth = 200;
        int centerBtnX = centerX - centerBtnWidth / 2;

        // Calculate vertical start so the logo and buttons are nicely balanced on screen
        int totalHeight = 208;
        int topY = Math.max(10, (this.height - totalHeight) / 2);

        int row1Y = topY + 80;
        int row2Y = row1Y + 26;
        int row3Y = row2Y + 26;
        int row4Y = row3Y + 28;
        int row5Y = row4Y + 28;

        // Row 1: Center -> Active Toggle (On / Off)
        activeButton = ButtonWidget.builder(
                getActiveToggleText(),
                button -> {
                    boolean newActive = !config.isActive();
                    config.setActive(newActive);
                    button.setMessage(getActiveToggleText());
                    if (!newActive) {
                        PotCounterMod mod = PotCounterMod.getInstance();
                        if (mod != null) {
                            mod.getCombatManager().clear();
                            mod.getPotTracker().resetAll();
                            mod.getPotDetectionManager().clear();
                        }
                    }
                    updateButtonsState();
                    saveConfig();
                }
        ).dimensions(centerBtnX, row1Y, centerBtnWidth, btnHeight)
         .tooltip(Tooltip.of(Text.literal("Toggle entire Pot Counter mod ON or OFF.")))
         .build();
        addDrawableChild(activeButton);

        // Row 2: Left -> Sound Toggle
        soundToggleButton = ButtonWidget.builder(
                getSoundToggleText(),
                button -> {
                    config.setSoundEnabled(!config.isSoundEnabled());
                    button.setMessage(getSoundToggleText());
                    updateButtonsState();
                    saveConfig();
                }
        ).dimensions(leftColX, row2Y, colWidth, btnHeight)
         .tooltip(Tooltip.of(Text.literal("Toggle sound notification when potions are thrown.")))
         .build();
        addDrawableChild(soundToggleButton);

        // Row 2: Right -> Display Toggle (Nametag / Menu HUD)
        counterDisplayButton = ButtonWidget.builder(
                getCounterDisplayText(),
                button -> {
                    config.setCounterDisplay(config.getCounterDisplay().next());
                    button.setMessage(getCounterDisplayText());
                    updateButtonsState();
                    saveConfig();
                }
        ).dimensions(rightColX, row2Y, colWidth, btnHeight)
         .tooltip(Tooltip.of(Text.literal("Switch counter display between player Nametag and on-screen Menu HUD.")))
         .build();
        addDrawableChild(counterDisplayButton);

        // Row 3: Left -> Sound Selection Submenu
        soundsButton = ButtonWidget.builder(
                Text.literal("§e🎵 Sound Selection..."),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new SoundSelectionScreen(this));
                    }
                }
        ).dimensions(leftColX, row3Y, colWidth, btnHeight)
         .tooltip(Tooltip.of(Text.literal("Select sound effect and adjust playback volume.")))
         .build();
        addDrawableChild(soundsButton);

        // Row 3: Right -> HUD Customization Submenu
        menuAppearanceButton = ButtonWidget.builder(
                Text.literal("§6⚡ HUD Customization..."),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new MenuAppearanceScreen(this));
                    }
                }
        ).dimensions(rightColX, row3Y, colWidth, btnHeight)
         .tooltip(Tooltip.of(Text.literal("Customize Menu HUD position, scale, theme, and colors.")))
         .build();
        addDrawableChild(menuAppearanceButton);

        // Row 4: Center -> Counter Mode Toggle (Single / All Players)
        counterModeButton = ButtonWidget.builder(
                getCounterModeText(),
                button -> {
                    config.setCounterMode(config.getCounterMode().next());
                    button.setMessage(getCounterModeText());
                    saveConfig();
                }
        ).dimensions(centerBtnX, row4Y, centerBtnWidth, btnHeight)
         .tooltip(Tooltip.of(Text.literal("Single: Only show counter for active combat opponent.\nAll Players: Show counters for all nearby players.")))
         .build();
        addDrawableChild(counterModeButton);

        // Row 5: Center -> Done / Save Button
        doneButton = ButtonWidget.builder(
                Text.literal("§a✔ Done"),
                button -> close()
        ).dimensions(centerBtnX, row5Y, centerBtnWidth, btnHeight).build();
        addDrawableChild(doneButton);

        updateButtonsState();
    }

    private void updateButtonsState() {
        boolean active = config.isActive();
        soundToggleButton.active = active;
        soundsButton.active = active && config.isSoundEnabled();
        counterModeButton.active = active;
        counterDisplayButton.active = active;
        menuAppearanceButton.active = active && (config.getCounterDisplay() == CounterDisplay.MENU);
    }

    private Text getActiveToggleText() {
        return Text.literal("Active: " + (config.isActive() ? "§aON" : "§cOFF"));
    }

    private Text getSoundToggleText() {
        return Text.literal("Sound: " + (config.isSoundEnabled() ? "§aON" : "§cOFF"));
    }

    private Text getCounterModeText() {
        return Text.literal("Counter Mode: §6" + config.getCounterMode().getDisplayName());
    }

    private Text getCounterDisplayText() {
        return Text.literal("Display: §e" + config.getCounterDisplay().getDisplayName());
    }

    private void saveConfig() {
        PotCounterMod.getInstance().getConfigManager().save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // Dynamic vertical centering matching init()
        int totalHeight = 208;
        int topY = Math.max(10, (this.height - totalHeight) / 2);

        // Draw the 3D Minecraft "Pot Counter" logo centered at the top
        int logoWidth = 136;
        int logoHeight = 72;
        int logoX = centerX - logoWidth / 2;
        int logoY = topY;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE, logoX, logoY, 0.0f, 0.0f, logoWidth, logoHeight, logoWidth, logoHeight);
    }

    @Override
    public void close() {
        saveConfig();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
