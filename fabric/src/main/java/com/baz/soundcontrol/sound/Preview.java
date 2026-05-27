package com.baz.soundcontrol.sound;

import com.baz.soundcontrol.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

public final class Preview {
    private static final long BYPASS_TIME = 3_000L;
    private static final Map<String, Long> bypassedSounds = new HashMap<>();
    private static SimpleSoundInstance currentSound;

    private Preview() {
    }

    public static void play(String soundId) {
        Identifier id = Identifier.tryParse(soundId);

        if (id == null) {
            return;
        }

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(id);

        if (sound == null) {
            return;
        }

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        if (currentSound != null) {
            soundManager.stop(currentSound);
        }

        currentSound = SimpleSoundInstance.forUI(sound, 1.0F, Config.previewVolume() / 100.0F);
        bypassedSounds.clear();
        bypassedSounds.put(soundId, System.currentTimeMillis());
        soundManager.play(currentSound);
    }

    public static boolean shouldBypass(String soundId) {
        Long startedAt = bypassedSounds.get(soundId);

        if (startedAt == null) {
            return false;
        }

        if (System.currentTimeMillis() - startedAt > BYPASS_TIME) {
            bypassedSounds.remove(soundId);
            return false;
        }

        return true;
    }
}
