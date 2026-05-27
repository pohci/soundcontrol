package com.baz.soundcontrol;

import com.baz.soundcontrol.config.Config;
import com.baz.soundcontrol.screen.SoundScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class Client implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("soundcontrol", "controls")
    );
    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.soundcontrol.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                client.setScreen(new SoundScreen(null));
            }
        });
    }

    public static KeyMapping openKey() {
        return openKey;
    }

    public static void setOpenKey(InputConstants.Key key) {
        openKey.setKey(key);
        KeyMapping.resetMapping();

        Minecraft client = Minecraft.getInstance();
        if (client.options != null) {
            client.options.save();
        }
    }
}
