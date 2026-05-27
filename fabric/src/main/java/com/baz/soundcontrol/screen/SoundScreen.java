package com.baz.soundcontrol.screen;

import com.baz.soundcontrol.config.Config;
import com.baz.soundcontrol.sound.Preview;
import com.baz.soundcontrol.sound.Recent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SoundScreen extends Screen {
    private static final int ROW_HEIGHT = 28;
    private static final int CONTENT_LEFT = 124;
    private static final int CONTENT_TOP = 52;
    private static final int FOOTER_HEIGHT = 28;
    private static List<String> allSounds;

    private final Screen parent;
    private final List<String> visibleSounds = new ArrayList<>();
    private EditBox search;
    private EditBox profileName;
    private Page page = Page.SOUNDS;
    private int scroll;
    private long openedAt;

    public SoundScreen(Screen parent) {
        super(Component.literal("Sound Control"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        search = new EditBox(font, CONTENT_LEFT, 20, Math.max(120, width - CONTENT_LEFT - 180), 20, Component.literal("Search"));
        search.setHint(Component.literal("Search"));
        search.setResponder(query -> {
            scroll = 0;
            rebuildRows();
        });
        profileName = new EditBox(font, CONTENT_LEFT, CONTENT_TOP + 20, 210, 20, Component.literal("Config Name"));
        profileName.setHint(Component.literal("Config name"));

        rebuildRows();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return Config.pauseInSingleplayer() && minecraft != null && minecraft.isSingleplayer();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, getFilteredSounds().size() - getVisibleRowCount());
        int nextScroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(scrollY)));

        if (nextScroll != scroll) {
            scroll = nextScroll;
            rebuildRows();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        drawFrame(graphics);
    }

    private void rebuildRows() {
        boolean focused = search != null && search.isFocused();
        boolean profileFocused = profileName != null && profileName.isFocused();
        clearWidgets();

        addRenderableOnly(new StringWidget(CONTENT_LEFT, 6, 140, 14, title, font));
        addSidebarButtons();

        search.setFocused(focused);
        profileName.setFocused(profileFocused);
        visibleSounds.clear();

        if (page == Page.CONFIGS) {
            addConfigControls();
            return;
        }

        addRenderableWidget(search);

        List<String> filteredSounds = getFilteredSounds();
        int rowCount = getVisibleRowCount();
        int end = Math.min(filteredSounds.size(), scroll + rowCount);

        for (int index = scroll; index < end; index++) {
            String soundId = filteredSounds.get(index);
            int y = CONTENT_TOP + (index - scroll) * ROW_HEIGHT;
            visibleSounds.add(soundId);
            addRenderableOnly(new StringWidget(CONTENT_LEFT, y + 5, width - CONTENT_LEFT - 390, 12, Component.literal(soundId), font)
                    .setMaxWidth(width - CONTENT_LEFT - 390));

            addSoundControls(soundId, y);

            addPreviewButton(soundId, y);
        }

        if (visibleSounds.isEmpty()) {
            String message = page == Page.RECENT
                    ? "No matching sounds played in the last 60 seconds."
                    : "No matching sounds.";
            addRenderableOnly(new StringWidget(CONTENT_LEFT, CONTENT_TOP + 12, 300, 14, Component.literal(message), font));
        }
    }

    private void addSidebarButtons() {
        int buttonWidth = 104;
        int x = 8;
        int y = 54;

        addRenderableWidget(Button.builder(Component.literal("Sounds"), button -> setPage(Page.SOUNDS))
                .bounds(x, y, buttonWidth, 20)
                .build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Recent"), button -> setPage(Page.RECENT))
                .bounds(x, y, buttonWidth, 20)
                .build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Modified"), button -> setPage(Page.MODIFIED))
                .bounds(x, y, buttonWidth, 20)
                .build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Settings"), button -> {
                    if (minecraft != null) {
                        minecraft.setScreen(ClothScreen.create(this));
                    }
                })
                .bounds(x, y, buttonWidth, 20)
                .build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Configs"), button -> setPage(Page.CONFIGS))
                .bounds(x, y, buttonWidth, 20)
                .build());
    }

    private void addSoundControls(String soundId, int y) {
        addRenderableWidget(new VolumeSlider(width - 318, y, 104, 20, soundId));
        addRenderableWidget(Button.builder(Component.literal(Config.getVolume(soundId) == 0 ? "Unmute" : "Mute"), button -> {
                    Config.setVolume(soundId, Config.getVolume(soundId) == 0 ? 100 : 0);
                    rebuildRows();
                })
                .bounds(width - 206, y, 66, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
                    Config.resetVolume(soundId);
                    rebuildRows();
                })
                .bounds(width - 132, y, 62, 20)
                .build()).active = Config.isModified(soundId);
    }

    private void addPreviewButton(String soundId, int y) {
        addRenderableWidget(Button.builder(Component.literal("Play"), button -> Preview.play(soundId))
                .bounds(width - 70, y, 54, 20)
                .build());
    }

    private void addConfigControls() {
        int y = CONTENT_TOP + 48;

        addRenderableOnly(new StringWidget(CONTENT_LEFT, CONTENT_TOP, 210, 14,
                Component.literal("Create or overwrite config"), font));
        addRenderableWidget(profileName);
        addRenderableWidget(Button.builder(Component.literal("Create / Overwrite"), button -> {
                    Config.saveProfile(profileName.getValue());
                    rebuildRows();
                })
                .bounds(CONTENT_LEFT + 220, CONTENT_TOP + 20, 120, 20)
                .build());

        for (String name : Config.profileNames()) {
            addRenderableOnly(new StringWidget(CONTENT_LEFT, y + 5, width - CONTENT_LEFT - 310, 12, Component.literal(name), font)
                    .setMaxWidth(width - CONTENT_LEFT - 310));
            addRenderableWidget(Button.builder(Component.literal("Load"), button -> {
                        Config.loadProfile(name);
                        rebuildRows();
                    })
                    .bounds(width - 246, y, 58, 20)
                    .build()).active = !name.equals(Config.activeProfile());
            addRenderableWidget(Button.builder(Component.literal("Overwrite"), button -> {
                        Config.saveProfile(name);
                        rebuildRows();
                    })
                    .bounds(width - 182, y, 86, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
                        Config.deleteProfile(name);
                        rebuildRows();
                    })
                    .bounds(width - 90, y, 74, 20)
                    .build()).active = Config.profileNames().size() > 1;
            y += ROW_HEIGHT;
        }
    }

    private void drawFrame(GuiGraphicsExtractor graphics) {
        int fade = (int) Math.min(90, (System.currentTimeMillis() - openedAt) / 8);
        int panelAlpha = Math.max(45, fade);
        int panelColor = (panelAlpha << 24);
        String configText = "Config Loaded: " + Config.activeProfile();
        String recentText = "Recent Sounds: " + Recent.soundIds().size();
        String modifiedText = "Modified Sounds: " + Config.soundIds().size();
        int footerY = height - 18;
        int recentX = Math.max(CONTENT_LEFT, 16 + font.width(configText) + 24);
        int modifiedX = recentX + font.width(recentText) + 24;

        graphics.fill(0, 0, CONTENT_LEFT - 8, height, 0x66000000);
        graphics.fill(0, height - FOOTER_HEIGHT, width, height, 0x88000000);
        graphics.fill(CONTENT_LEFT - 8, 0, CONTENT_LEFT - 7, height - FOOTER_HEIGHT, 0x66FFFFFF);
        graphics.fill(0, height - FOOTER_HEIGHT, width, height - FOOTER_HEIGHT + 1, 0x66FFFFFF);
        graphics.fill(CONTENT_LEFT - 8, 0, width, 24, panelColor);

        graphics.text(font, "SoundControl", 8, 10, 0xFFFFFFFF, false);
        graphics.text(font, configText, 8, footerY, 0xFFE0E0E0, false);
        graphics.text(font, recentText, recentX, footerY, 0xFFB0B0B0, false);

        if (modifiedX + font.width(modifiedText) < width - 8) {
            graphics.text(font, modifiedText, modifiedX, footerY, 0xFFB0B0B0, false);
        } else {
            graphics.text(font, modifiedText, 8, footerY - 10, 0xFFB0B0B0, false);
        }
    }

    private String pageLabel() {
        return switch (page) {
            case SOUNDS -> "all sounds";
            case RECENT -> "recent sounds";
            case MODIFIED -> "modified sounds";
            case CONFIGS -> "configs";
        };
    }

    private void setPage(Page nextPage) {
        page = nextPage;
        scroll = 0;
        rebuildRows();
    }

    private List<String> getFilteredSounds() {
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<String> source = switch (page) {
            case SOUNDS -> new ArrayList<>(getAllSounds());
            case RECENT -> getRecentSource(query);
            case MODIFIED -> getModifiedSounds();
            default -> new ArrayList<>();
        };

        if (!query.isEmpty()) {
            source.removeIf(soundId -> !soundId.toLowerCase(Locale.ROOT).contains(query));
        }

        if (page == Page.SOUNDS || page == Page.MODIFIED || !query.isEmpty()) {
            source.sort((left, right) -> {
                if (Config.mutedFirst()) {
                    int muted = Boolean.compare(Config.getVolume(right) == 0, Config.getVolume(left) == 0);

                    if (muted != 0) {
                        return muted;
                    }
                }

                return Comparator.<String>naturalOrder().compare(left, right);
            });
        }

        return source;
    }

    private List<String> getRecentSource(String query) {
        List<String> sounds = Recent.soundIds();
        return sounds;
    }

    private int getVisibleRowCount() {
        return Math.max(1, (height - CONTENT_TOP - 18) / ROW_HEIGHT);
    }

    private List<String> getModifiedSounds() {
        List<String> sounds = new ArrayList<>(Config.soundIds());
        sounds.removeIf(soundId -> !Config.isModified(soundId));
        return sounds;
    }

    private static List<String> getAllSounds() {
        if (allSounds == null) {
            allSounds = BuiltInRegistries.SOUND_EVENT.keySet().stream()
                    .map(Identifier::toString)
                    .sorted()
                    .toList();
        }

        return allSounds;
    }

    private enum Page {
        SOUNDS,
        RECENT,
        MODIFIED,
        CONFIGS
    }
}
