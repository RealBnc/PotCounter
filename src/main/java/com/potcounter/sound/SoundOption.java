package com.potcounter.sound;

public record SoundOption(String id, String displayName) {
    public static final SoundOption[] AVAILABLE_SOUNDS = new SoundOption[] {
        new SoundOption("minecraft:entity.experience_orb.pickup", "Experience Orb Pickup"),
        new SoundOption("minecraft:entity.arrow.hit_player", "Arrow Hit Player"),
        new SoundOption("minecraft:block.note_block.bell", "Note Block Bell"),
        new SoundOption("minecraft:block.note_block.pling", "Note Block Pling"),
        new SoundOption("minecraft:block.note_block.chime", "Note Block Chime"),
        new SoundOption("minecraft:entity.item.pickup", "Item Pickup"),
        new SoundOption("minecraft:ui.button.click", "UI Button Click"),
        new SoundOption("minecraft:entity.splash_potion.break", "Splash Potion Break"),
        new SoundOption("minecraft:block.anvil.land", "Anvil Land"),
        new SoundOption("minecraft:entity.player.levelup", "Player Level Up")
    };

    public static SoundOption getById(String id) {
        if (id == null) return AVAILABLE_SOUNDS[0];
        for (SoundOption option : AVAILABLE_SOUNDS) {
            if (option.id().equalsIgnoreCase(id)) {
                return option;
            }
        }
        return AVAILABLE_SOUNDS[0];
    }
}
