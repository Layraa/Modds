package com.custommobsforge.custommobsforge.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PresetManager {
    private static final PresetManager INSTANCE = new PresetManager();
    private final Map<String, Preset> presets = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PRESETS_FILE = "custommobsforge/presets.json";

    public static PresetManager getInstance() {
        return INSTANCE;
    }

    public static void init() {
        loadPresets();
    }

    public boolean addPreset(String name, float health, double speed, String modelName, String textureName, String animationName) {
        if (presets.containsKey(name)) {
            return false;
        }
        presets.put(name, new Preset(name, health, speed, modelName, textureName, animationName));
        savePresets();
        return true;
    }

    public Preset getPreset(String name) {
        return presets.get(name);
    }

    public Map<String, Preset> getAllPresets() {
        return new HashMap<>(presets);
    }

    public void editPreset(String name, float health, double speed, String modelName, String textureName, String animationName) {
        if (presets.containsKey(name)) {
            presets.put(name, new Preset(name, health, speed, modelName, textureName, animationName));
            savePresets();
        }
    }

    public void deletePreset(String name) {
        presets.remove(name);
        savePresets();
    }

    public void clearPresets() {
        presets.clear();
        savePresets();
    }

    private static void savePresets() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerLevel level = server.overworld();
            Path presetsPath = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve(PRESETS_FILE);
            try {
                Files.createDirectories(presetsPath.getParent());
                try (Writer writer = new FileWriter(presetsPath.toFile())) {
                    GSON.toJson(INSTANCE.presets, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadPresets() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerLevel level = server.overworld();
            Path presetsPath = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve(PRESETS_FILE);
            if (Files.exists(presetsPath)) {
                try (Reader reader = new FileReader(presetsPath.toFile())) {
                    Map<String, Preset> loadedPresets = GSON.fromJson(reader, new TypeToken<Map<String, Preset>>(){}.getType());
                    if (loadedPresets != null) {
                        INSTANCE.presets.clear();
                        INSTANCE.presets.putAll(loadedPresets);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public record Preset(String name, float health, double speed, String modelName, String textureName, String animationName) {
    }
}