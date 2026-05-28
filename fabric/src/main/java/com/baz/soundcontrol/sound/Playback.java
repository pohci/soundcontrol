package com.baz.soundcontrol.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

public final class Playback {
    private Playback() {
    }

    // stop channels already playing this event so slider/mute changes apply immediately
    public static void stopPlaying(String soundId) {
        Minecraft client = Minecraft.getInstance();

        if (client == null) {
            return;
        }

        Identifier id = Identifier.tryParse(soundId);

        if (id == null) {
            return;
        }

        SoundManager soundManager = client.getSoundManager();

        for (SoundSource source : SoundSource.values()) {
            soundManager.stop(id, source);
        }
    }
}
