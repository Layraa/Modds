package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PresetManager {
    private static final File PRESET_FILE = new File("config/custommobsforge/presets.json");
    private static List<Preset> presets = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SimpleChannel channel;

    public static void init(SimpleChannel channel) {
        PresetManager.channel = channel;
        loadPresets();
    }

    public static void loadPresets() {
        if (!PRESET_FILE.exists()) {
            PRESET_FILE.getParentFile().mkdirs();
            presets = new ArrayList<>();
            savePresets();
        }
        try (Reader reader = new FileReader(PRESET_FILE)) {
            presets = GSON.fromJson(reader, new TypeToken<List<Preset>>(){}.getType());
            if (presets == null) presets = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePresets() {
        try (Writer writer = new FileWriter(PRESET_FILE)) {
            GSON.toJson(presets, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addPreset(Preset preset, ServerPlayer player) {
        if (player.hasPermissions(2)) {
            if (validatePreset(preset)) {
                presets.add(preset);
                savePresets();
                syncPresetsToClient(player);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Preset added: " + preset.getName()));
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Invalid preset data."));
            }
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You do not have permission to add presets."));
        }
    }

    public static void removePreset(String name, ServerPlayer player) {
        if (player.hasPermissions(2)) {
            presets.removeIf(preset -> preset.getName().equals(name));
            savePresets();
            syncPresetsToClient(player);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Preset removed: " + name));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You do not have permission to remove presets."));
        }
    }

    public static void spawnMobForPreset(ServerPlayer player, String presetName, Vec3 position) {
        if (player.hasPermissions(2)) {
            Preset preset = presets.stream()
                    .filter(p -> p.getName().equals(presetName))
                    .findFirst()
                    .orElse(null);
            if (preset != null) {
                ServerMobHandler.spawnCustomMob(
                        player.serverLevel(),
                        preset,
                        position.x,
                        position.y,
                        position.z
                );
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Spawned mob: " + preset.getName()));
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Preset not found: " + presetName));
            }
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You do not have permission to spawn mobs."));
        }
    }

    public static void syncPresetsToClient(ServerPlayer player) {
        if (channel == null) {
            throw new IllegalStateException("PresetManager not initialized with a SimpleChannel");
        }
        channel.send(PacketDistributor.PLAYER.with(() -> player), new com.custommobsforge.custommobsforge.common.preset.PresetPacket(presets));
    }

    public static void syncPresetsToClients() {
        if (channel == null) {
            throw new IllegalStateException("PresetManager not initialized with a SimpleChannel");
        }
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            syncPresetsToClient(player);
        }
    }

    public static List<Preset> getPresets() {
        return new ArrayList<>(presets);
    }

    private static boolean validatePreset(Preset preset) {
        return preset.getName() != null && !preset.getName().isEmpty() &&
                preset.getModel() != null && !preset.getModel().isEmpty() &&
                preset.getAnimation() != null && !preset.getAnimation().isEmpty() &&
                preset.getTexture() != null && !preset.getTexture().isEmpty() &&
                preset.getBehavior() != null && !preset.getBehavior().isEmpty() &&
                preset.getHp() > 0 &&
                preset.getSpeed() > 0 &&
                preset.getSize() > 0;
    }
}