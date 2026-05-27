package com.baz.soundcontrol.mixin;

import com.baz.soundcontrol.screen.SoundScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsMixin {
    @Inject(method = "addOptions", at = @At("TAIL"))
    private void addSoundControlButton(CallbackInfo info) {
        Button button = Button.builder(Component.literal("Open SoundControl"), clicked -> {
                    Minecraft.getInstance().setScreen(new SoundScreen((Screen) (Object) this));
                })
                .width(310)
                .build();

        ((OptionsSubScreenAccessor) this).soundcontrol$getList().addSmall(List.of(button));
    }
}
