package com.baz.soundcontrol;

import com.baz.soundcontrol.config.Config;
import com.baz.soundcontrol.screen.SoundScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod("soundcontrol")
public class Client {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("soundcontrol", "controls")
    );
    private static KeyMapping openKey;

    public Client(IEventBus modBus) {
        Config.load();
        modBus.addListener(this::registerKeys);
        NeoForge.EVENT_BUS.addListener(this::tickClient);
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

    private void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        openKey = new KeyMapping(
                "key.soundcontrol.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                CATEGORY
        );
        event.register(openKey);
    }

    private void tickClient(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();

        while (openKey != null && openKey.consumeClick()) {
            client.setScreen(new SoundScreen(null));
        }
    }
}
