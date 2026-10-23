package com.potcounter.sound;

import com.potcounter.config.CounterConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PotCounter/Sound");
    private final CounterConfig config;

    public SoundManager(CounterConfig config) {
        this.config = config;
    }

    public void playPotSound() {
        if (!config.isSoundEnabled()) {
            return;
        }
        playSound(config.getSoundId(), config.getSoundVolume());
    }

    public void previewSound(String soundId, float volume) {
        playSound(soundId, volume);
    }

    private void playSound(String soundId, float volume) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.getSoundManager() == null) {
                return;
            }

            Identifier identifier = Identifier.tryParse(soundId);
            if (identifier == null) {
                identifier = Identifier.of("minecraft", "entity.experience_orb.pickup");
            }

            // Direct sound event creation - completely avoids registry get method differences across MC versions
            SoundEvent soundEvent = SoundEvent.of(identifier);
            if (soundEvent != null) {
                client.getSoundManager().play(PositionedSoundInstance.master(soundEvent, 1.0f, volume));
            }
        } catch (Throwable t) {
            LOGGER.warn("Failed to play sound: {}", soundId, t);
        }
    }
}
