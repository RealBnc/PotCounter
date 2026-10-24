package com.potcounter.gui;

import com.potcounter.PotCounterMod;
import com.potcounter.config.CounterConfig;
import com.potcounter.render.TextHelper;
import com.potcounter.sound.SoundManager;
import com.potcounter.sound.SoundOption;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SoundSelectionScreen extends Screen {
    private final Screen parent;
    private final CounterConfig config;
    private final SoundManager soundManager;

    private int selectedIndex = 0;
    private float currentVolume;

    public SoundSelectionScreen(Screen parent) {
        super(Text.literal("Sound Selection"));
        this.parent = parent;
        this.config = PotCounterMod.getInstance().getConfig();
        this.soundManager = PotCounterMod.getInstance().getSoundManager();
        this.currentVolume = config.getSoundVolume();

        for (int i = 0; i < SoundOption.AVAILABLE_SOUNDS.length; i++) {
            if (SoundOption.AVAILABLE_SOUNDS[i].id().equalsIgnoreCase(config.getSoundId())) {
                this.selectedIndex = i;
                break;
            }
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int col1X = centerX - 190;
        int col2X = centerX + 10;
        int buttonWidth = 180;
        int buttonHeight = 20;

        for (int i = 0; i < SoundOption.AVAILABLE_SOUNDS.length; i++) {
            final int index = i;
            SoundOption option = SoundOption.AVAILABLE_SOUNDS[i];
            int x = (i < 5) ? col1X : col2X;
            int y = startY + ((i % 5) * 24);

            ButtonWidget soundButton = ButtonWidget.builder(
                    getOptionText(index, option),
                    button -> {
                        selectedIndex = index;
                        config.setSoundId(option.id());
                        saveConfig();
                        rebuild();
                    }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();

            addDrawableChild(soundButton);
        }

        int bottomY = startY + (5 * 24) + 10;
        CustomSliderWidget volumeSlider = new CustomSliderWidget(
                centerX - 100,
                bottomY,
                200,
                20,
                "Volume",
                "%",
                0.0,
                100.0,
                currentVolume * 100.0,
                val -> {
                    currentVolume = (float) (val / 100.0);
                    config.setSoundVolume(currentVolume);
                    saveConfig();
                }
        );
        addDrawableChild(volumeSlider);

        ButtonWidget previewButton = ButtonWidget.builder(
                Text.literal("▶ Test Selected Sound"),
                button -> {
                    SoundOption selected = SoundOption.AVAILABLE_SOUNDS[selectedIndex];
                    soundManager.previewSound(selected.id(), currentVolume);
                }
        ).dimensions(centerX - 100, bottomY + 24, 200, 20).build();
        addDrawableChild(previewButton);

        ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                button -> close()
        ).dimensions(centerX - 100, bottomY + 50, 200, 20).build();
        addDrawableChild(backButton);
    }

    private void rebuild() {
        clearChildren();
        init();
    }

    private Text getOptionText(int index, SoundOption option) {
        if (index == selectedIndex) {
            return Text.literal("§a✔ " + option.displayName());
        }
        return Text.literal(option.displayName());
    }

    private void saveConfig() {
        PotCounterMod.getInstance().getConfigManager().save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        TextHelper.drawCenteredTextWithShadow(context, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        saveConfig();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
