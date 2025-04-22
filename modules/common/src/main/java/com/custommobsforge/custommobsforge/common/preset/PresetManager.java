package com.custommobsforge.custommobsforge.common.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresetManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Path PRESETS_PATH = Path.of("config/custommobsforge/presets.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PresetManager instance;
    private final List<Preset> presets = Collections.synchronizedList(new ArrayList<>());

    public static PresetManager getInstance() {
        if (instance == null) {
            instance = new PresetManager();
            instance.loadPresets();
        }
        return instance;
    }

    private void loadPresets() {
        try {
            Files.createDirectories(PRESETS_PATH.getParent());
            if (Files.exists(PRESETS_PATH)) {
                String json = Files.readString(PRESETS_PATH);
                if (json.trim().isEmpty()) {
                    LOGGER.warn("Presets file is empty");
                    return;
                }
                List<Preset> loadedPresets = GSON.fromJson(json, new TypeToken<List<Preset>>(){}.getType());
                if (loadedPresets != null) {
                    presets.addAll(loadedPresets);
                } else {
                    LOGGER.warn("Failed to deserialize presets, file may be corrupted");
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load presets: {}", e.getMessage());
        } catch (com.google.gson.JsonSyntaxException e) {
            LOGGER.error("Invalid JSON syntax in presets file: {}", e.getMessage());
        }
    }

    private void savePresets() {
        try {
            Files.createDirectories(PRESETS_PATH.getParent());
            Files.writeString(PRESETS_PATH, GSON.toJson(presets));
        } catch (IOException e) {
            LOGGER.error("Failed to save presets: {}", e.getMessage());
        }
    }

    public void addPreset(String name, float health, double speed, float sizeWidth, float sizeHeight, String modelName, String textureName, String animationName) {
        presets.removeIf(preset -> preset.name().equals(name));
        presets.add(new Preset(name, health, speed, sizeWidth, sizeHeight, modelName, textureName, animationName));
        savePresets();
    }

    public void removePreset(String name) {
        presets.removeIf(preset -> preset.name().equals(name));
        savePresets();
    }

    public Preset getPreset(String name) {
        return presets.stream()
                .filter(preset -> preset.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<Preset> getPresets() {
        return new ArrayList<>(presets);
    }
}