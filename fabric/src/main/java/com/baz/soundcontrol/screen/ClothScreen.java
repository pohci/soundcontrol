package com.baz.soundcontrol.screen;

import com.baz.soundcontrol.Client;
import com.baz.soundcontrol.config.Config;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClothScreen {
    private ClothScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("SoundControl Settings"))
                .setShouldListSmoothScroll(true)
                .setShouldTabsSmoothScroll(true)
                .setTransparentBackground(true)
                .setSavingRunnable(Config::save);
        ConfigEntryBuilder entries = builder.entryBuilder();

        addGeneral(builder, entries);
        addVolume(builder, entries);

        return builder.build();
    }

    private static void addGeneral(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(entries.startKeyCodeField(Component.literal("Open menu key"), openKey())
                .setDefaultValue(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_O))
                .setKeySaveConsumer(Client::setOpenKey)
                .setTooltip(Component.literal("key used to open the sound manager"))
                .build());
        general.addEntry(entries.startBooleanToggle(Component.literal("Pause in single-player"), Config.pauseInSingleplayer())
                .setDefaultValue(true)
                .setSaveConsumer(Config::setPauseInSingleplayer)
                .setTooltip(Component.literal("pause the game while SoundControl is open in single-player"))
                .build());
        general.addEntry(entries.startBooleanToggle(Component.literal("Muted sounds first"), Config.mutedFirst())
                .setDefaultValue(false)
                .setSaveConsumer(Config::setMutedFirst)
                .setTooltip(Component.literal("sort muted sounds above other modified sounds"))
                .build());
        general.addEntry(entries.startBooleanToggle(Component.literal("Sound ids in subtitles"), Config.showSoundIdsInSubtitles())
                .setDefaultValue(false)
                .setSaveConsumer(Config::setShowSoundIdsInSubtitles)
                .setTooltip(Component.literal("replace vanilla subtitle text with raw sound ids"))
                .build());
    }

    private static void addVolume(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory volume = builder.getOrCreateCategory(Component.literal("Volume"));

        volume.addEntry(entries.startIntSlider(Component.literal("Max sound volume"), Config.maxVolume(), 100, 300)
                .setDefaultValue(200)
                .setSaveConsumer(Config::setMaxVolume)
                .setTextGetter(ClothScreen::percent)
                .setTooltip(Component.literal("highest value sound sliders can use"))
                .build());
        volume.addEntry(entries.startIntSlider(Component.literal("Preview volume"), Config.previewVolume(), 0, Config.maxVolume())
                .setDefaultValue(100)
                .setSaveConsumer(Config::setPreviewVolume)
                .setTextGetter(ClothScreen::percent)
                .setTooltip(Component.literal("volume used by the play button"))
                .build());
    }

    private static InputConstants.Key openKey() {
        KeyMapping mapping = Client.openKey();
        return mapping == null ? InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_O) : InputConstants.getKey(mapping.saveString());
    }

    private static Component percent(int value) {
        return Component.literal(value + "%");
    }
}
