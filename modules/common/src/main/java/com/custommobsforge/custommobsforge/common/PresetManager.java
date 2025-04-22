package com.custommobsforge.custommobsforge.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresetManager {
    private static final PresetManager INSTANCE = new PresetManager();
    private final Map<String, Preset> presets = new HashMap<>();

    public record Preset(String name, float health, double speed, String modelName, String textureName, String animationName) {}

    public static PresetManager getInstance() {
        return INSTANCE;
    }

    public void addPreset(String name, float health, double speed, String modelName, String textureName, String animationName) {
        presets.put(name, new Preset(name, health, speed, modelName, textureName, animationName));
    }

    public void removePreset(String name) {
        presets.remove(name);
    }

    public Preset getPreset(String name) {
        return presets.get(name);
    }

    public List<Preset> getPresets() {
        return new ArrayList<>(presets.values());
    }
}