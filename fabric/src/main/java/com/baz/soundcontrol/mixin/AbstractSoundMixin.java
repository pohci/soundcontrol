package com.baz.soundcontrol.mixin;

import com.baz.soundcontrol.config.Config;
import com.baz.soundcontrol.sound.Preview;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSoundInstance.class)
public class AbstractSoundMixin {
    @ModifyReturnValue(method = "getVolume()F", at = @At("RETURN"))
    private float soundcontrol$applyVolume(float original) {
        AbstractSoundInstance sound = (AbstractSoundInstance) (Object) this;
        String soundId = sound.getIdentifier().toString();

        if (Preview.shouldBypass(soundId)) {
            return original;
        }

        int volume = Config.getVolume(soundId);

        if (volume == 100) {
            return original;
        }

        return original * volume / 100.0F;
    }
}
