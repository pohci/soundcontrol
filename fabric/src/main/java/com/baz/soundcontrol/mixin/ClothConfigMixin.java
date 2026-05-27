package com.baz.soundcontrol.mixin;

import com.baz.soundcontrol.config.Config;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ClothConfigScreen.class, remap = false)
public class ClothConfigMixin {
    public boolean isPauseScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        return Config.pauseInSingleplayer() && minecraft.isSingleplayer();
    }
}
