package com.baz.soundcontrol.mixin;

import com.baz.soundcontrol.config.Config;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(net.minecraft.client.gui.components.SubtitleOverlay.class)
public class SubtitleMixin {
    @ModifyVariable(method = "onPlaySound", at = @At("STORE"), ordinal = 0)
    private Component useSoundId(Component subtitle, SoundInstance sound, WeighedSoundEvents soundEvents, float range) {
        if (!Config.showSoundIdsInSubtitles()) {
            return subtitle;
        }

        return Component.literal(sound.getIdentifier().toString());
    }
}
