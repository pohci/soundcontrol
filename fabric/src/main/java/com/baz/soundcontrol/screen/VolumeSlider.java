package com.baz.soundcontrol.screen;

import com.baz.soundcontrol.config.Config;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class VolumeSlider extends AbstractSliderButton {
    private final String soundId;

    public VolumeSlider(int x, int y, int width, int height, String soundId) {
        super(x, y, width, height, Component.empty(), Config.getVolume(soundId) / (double) Config.maxVolume());
        this.soundId = soundId;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(Config.getVolume(soundId) + "%"));
    }

    @Override
    protected void applyValue() {
        Config.setVolume(soundId, (int) Math.round(value * Config.maxVolume()));
        updateMessage();
    }
}
