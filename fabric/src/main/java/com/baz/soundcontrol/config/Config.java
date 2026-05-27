package com.baz.soundcontrol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("soundcontrol.json");
    private static final String DEFAULT_PROFILE = "Default";
    private static final Map<String, Profile> profiles = new LinkedHashMap<>();
    private static String activeProfile = DEFAULT_PROFILE;

    private Config() {
    }

    public static void load() {
        profiles.clear();
        activeProfile = DEFAULT_PROFILE;

        if (!Files.exists(PATH)) {
            profiles.put(DEFAULT_PROFILE, new Profile());
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("profiles")) {
                activeProfile = getString(root, "activeConfig", DEFAULT_PROFILE);
                JsonObject savedProfiles = root.getAsJsonObject("profiles");

                for (String name : savedProfiles.keySet()) {
                    profiles.put(name, readProfile(savedProfiles.getAsJsonObject(name)));
                }
            } else {
                Profile profile = new Profile();
                readSounds(profile.sounds, root.has("sounds") ? root.getAsJsonObject("sounds") : new JsonObject());
                profiles.put(DEFAULT_PROFILE, profile);
            }
        } catch (RuntimeException | IOException exception) {
            profiles.clear();
            activeProfile = DEFAULT_PROFILE;
        }

        if (profiles.isEmpty()) {
            profiles.put(DEFAULT_PROFILE, new Profile());
        }

        if (!profiles.containsKey(activeProfile)) {
            activeProfile = profiles.keySet().iterator().next();
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        JsonObject savedProfiles = new JsonObject();

        for (Map.Entry<String, Profile> entry : profiles.entrySet()) {
            savedProfiles.add(entry.getKey(), writeProfile(entry.getValue()));
        }

        root.addProperty("activeConfig", activeProfile);
        root.add("profiles", savedProfiles);

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static Set<String> soundIds() {
        return current().sounds.keySet();
    }

    public static int getVolume(String soundId) {
        return current().sounds.getOrDefault(soundId, 100);
    }

    public static void setVolume(String soundId, int volume) {
        int clampedVolume = clamp(volume);

        if (clampedVolume == 100) {
            current().sounds.remove(soundId);
        } else {
            current().sounds.put(soundId, clampedVolume);
        }

        save();
    }

    public static void resetVolume(String soundId) {
        current().sounds.remove(soundId);
        save();
    }

    public static boolean isModified(String soundId) {
        return getVolume(soundId) != 100;
    }

    public static String activeProfile() {
        return activeProfile;
    }

    public static List<String> profileNames() {
        return new ArrayList<>(profiles.keySet());
    }

    public static boolean loadProfile(String name) {
        if (!profiles.containsKey(name)) {
            return false;
        }

        activeProfile = name;
        save();
        return true;
    }

    public static void saveProfile(String name) {
        String cleanName = cleanProfileName(name);

        if (cleanName.isEmpty()) {
            return;
        }

        profiles.put(cleanName, current().copy());
        activeProfile = cleanName;
        save();
    }

    public static void deleteProfile(String name) {
        if (profiles.size() <= 1) {
            return;
        }

        profiles.remove(name);

        if (!profiles.containsKey(activeProfile)) {
            activeProfile = profiles.keySet().iterator().next();
        }

        save();
    }

    public static boolean pauseInSingleplayer() {
        return current().pauseInSingleplayer;
    }

    public static void setPauseInSingleplayer(boolean pauseInSingleplayer) {
        current().pauseInSingleplayer = pauseInSingleplayer;
        save();
    }

    public static boolean mutedFirst() {
        return current().mutedFirst;
    }

    public static void setMutedFirst(boolean mutedFirst) {
        current().mutedFirst = mutedFirst;
        save();
    }

    public static boolean showSoundIdsInSubtitles() {
        return current().showSoundIdsInSubtitles;
    }

    public static void setShowSoundIdsInSubtitles(boolean showSoundIdsInSubtitles) {
        current().showSoundIdsInSubtitles = showSoundIdsInSubtitles;
        save();
    }

    public static int maxVolume() {
        return current().maxVolume;
    }

    public static void setMaxVolume(int maxVolume) {
        Profile profile = current();
        profile.maxVolume = Math.max(100, Math.min(300, maxVolume));
        profile.sounds.replaceAll((soundId, volume) -> Math.min(volume, profile.maxVolume));
        save();
    }

    public static int previewVolume() {
        return current().previewVolume;
    }

    public static void setPreviewVolume(int previewVolume) {
        current().previewVolume = clamp(previewVolume);
        save();
    }

    private static int clamp(int volume) {
        return Math.max(0, Math.min(current().maxVolume, volume));
    }

    private static Profile current() {
        return profiles.computeIfAbsent(activeProfile, name -> new Profile());
    }

    private static Profile readProfile(JsonObject object) {
        Profile profile = new Profile();

        profile.pauseInSingleplayer = getBoolean(object, "pauseInSingleplayer", true);
        profile.mutedFirst = getBoolean(object, "mutedFirst", false);
        profile.showSoundIdsInSubtitles = getBoolean(object, "showSoundIdsInSubtitles", false);
        profile.maxVolume = Math.max(100, Math.min(300, getInt(object, "maxVolume", 200)));
        profile.previewVolume = clamp(getInt(object, "previewVolume", 100));
        readSounds(profile.sounds, object.has("sounds") ? object.getAsJsonObject("sounds") : new JsonObject());

        return profile;
    }

    private static JsonObject writeProfile(Profile profile) {
        JsonObject object = new JsonObject();
        JsonObject sounds = new JsonObject();

        for (Map.Entry<String, Integer> entry : profile.sounds.entrySet()) {
            sounds.addProperty(entry.getKey(), entry.getValue());
        }

        object.addProperty("pauseInSingleplayer", profile.pauseInSingleplayer);
        object.addProperty("mutedFirst", profile.mutedFirst);
        object.addProperty("showSoundIdsInSubtitles", profile.showSoundIdsInSubtitles);
        object.addProperty("maxVolume", profile.maxVolume);
        object.addProperty("previewVolume", profile.previewVolume);
        object.add("sounds", sounds);

        return object;
    }

    private static void readSounds(Map<String, Integer> target, JsonObject sounds) {
        for (String soundId : sounds.keySet()) {
            target.put(soundId, clamp(sounds.get(soundId).getAsInt()));
        }
    }

    private static String getString(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsString();
    }

    private static boolean getBoolean(JsonObject object, String key, boolean fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsBoolean();
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsInt();
    }

    private static String cleanProfileName(String name) {
        return name == null ? "" : name.trim();
    }

    private static final class Profile {
        private final Map<String, Integer> sounds = new LinkedHashMap<>();
        private boolean pauseInSingleplayer = true;
        private boolean mutedFirst;
        private boolean showSoundIdsInSubtitles;
        private int maxVolume = 200;
        private int previewVolume = 100;

        private Profile copy() {
            Profile copy = new Profile();
            copy.sounds.putAll(sounds);
            copy.pauseInSingleplayer = pauseInSingleplayer;
            copy.mutedFirst = mutedFirst;
            copy.showSoundIdsInSubtitles = showSoundIdsInSubtitles;
            copy.maxVolume = maxVolume;
            copy.previewVolume = previewVolume;
            return copy;
        }
    }
}
