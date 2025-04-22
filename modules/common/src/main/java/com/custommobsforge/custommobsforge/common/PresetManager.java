package com.custommobsforge.custommobsforge.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PresetManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PRESETS_FILE = Path.of("config/custommobsforge/presets.json");
    private static PresetManager instance;
    private final List<Preset> presets = new ArrayList<>();

    private PresetManager() {
        loadPresets();
    }

    public static PresetManager getInstance() {
        if (instance == null) {
            instance = new PresetManager();
        }
        return instance;
    }

    public void addPreset(String name, float health, double speed, String modelName, String textureName, String animationName) {
        presets.add(new Preset(name, health, speed, modelName, textureName, animationName));
        savePresets();
    }

    public void removePreset(String name) {
        presets.removeIf(preset -> preset.getName().equals(name));
        savePresets();
    }

    public Preset getPreset(String name) {
        return presets.stream()
                .filter(preset -> preset.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Collection<Preset> getPresets() {
        return new ArrayList<>(presets);
    }

    private void loadPresets() {
        try {
            Files.createDirectories(PRESETS_FILE.getParent());
            if (Files.exists(PRESETS_FILE)) {
                String json = Files.readString(PRESETS_FILE);
                List<Preset> loadedPresets = GSON.fromJson(json, new TypeToken<List<Preset>>() {}.getType());
                if (loadedPresets != null) {
                    presets.addAll(loadedPresets);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load presets: {}", e.getMessage());
        }
    }

    private void savePresets() {
        try {
            Files.createDirectories(PRESETS_FILE.getParent());
            String json = GSON.toJson(presets);
            Files.writeString(PRESETS_FILE, json);
        } catch (IOException e) {
            LOGGER.error("Failed to save presets: {}", e.getMessage());
        }
    }
}