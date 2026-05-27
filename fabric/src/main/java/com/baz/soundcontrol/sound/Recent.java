package com.baz.soundcontrol.sound;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Recent {
    private static final long WINDOW = 60_000L;
    private static final Map<String, Long> sounds = new LinkedHashMap<>();

    private Recent() {
    }

    public static void record(Identifier soundId) {
        long now = System.currentTimeMillis();
        String id = soundId.toString();

        sounds.remove(id);
        sounds.put(id, now);
        prune(now);
    }

    public static List<String> soundIds() {
        long now = System.currentTimeMillis();
        prune(now);

        List<String> ids = new ArrayList<>(sounds.keySet());
        ids.sort((left, right) -> Long.compare(sounds.get(right), sounds.get(left)));
        return ids;
    }

    private static void prune(long now) {
        sounds.entrySet().removeIf(entry -> now - entry.getValue() > WINDOW);
    }
}
