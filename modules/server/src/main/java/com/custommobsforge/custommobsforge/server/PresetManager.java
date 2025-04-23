package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.RequestPresetsPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PresetManager {
    private static final Logger LOGGER = LogManager.getLogger("CustomMobsForge");
    private static final File PRESET_DIR = new File("config/custommobsforge/presets/");
    private static List<Preset> presets = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SimpleChannel channel;

    public static void init(SimpleChannel channel) {
        PresetManager.channel = channel;
        loadPresets();
        channel.messageBuilder(RequestPresetsPacket.class, 4)
                .encoder(RequestPresetsPacket::encode)
                .decoder(RequestPresetsPacket::decode)
                .consumerMainThread((msg, ctx) -> handleRequestPresetsPacket(msg, ctx))
                .add();
    }

    private static void handleRequestPresetsPacket(RequestPresetsPacket msg, Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                syncPresetsToClient(ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void loadPresets() {
        presets.clear();
        if (!PRESET_DIR.exists()) {
            PRESET_DIR.mkdirs();
            LOGGER.info("Created preset directory: " + PRESET_DIR.getPath());
            return;
        }

        File[] presetFiles = PRESET_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (presetFiles == null || presetFiles.length == 0) {
            LOGGER.info("No preset files found in directory: " + PRESET_DIR.getPath());
            return;
        }

        for (File file : presetFiles) {
            try (Reader reader = new FileReader(file)) {
                Preset preset = GSON.fromJson(reader, Preset.class);
                if (preset != null) {
                    presets.add(preset);
                    LOGGER.info("Loaded preset: " + preset.getName() + " from file: " + file.getName());
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load preset from file: " + file.getName(), e);
            }
        }
    }

    private static void savePresetToFile(Preset preset) {
        File presetFile = new File(PRESET_DIR, preset.getName() + ".json");
        try (Writer writer = new FileWriter(presetFile)) {
            GSON.toJson(preset, writer);
            LOGGER.info("Saved preset: " + preset.getName() + " to file: " + presetFile.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to save preset: " + preset.getName(), e);
        }
    }

    private static void removePresetFile(String presetName) {
        File presetFile = new File(PRESET_DIR, presetName + ".json");
        if (presetFile.exists()) {
            if (presetFile.delete()) {
                LOGGER.info("Deleted preset file: " + presetFile.getName());
            } else {
                LOGGER.error("Failed to delete preset file: " + presetFile.getName());
            }
        }
    }

    public static void addPreset(Preset preset, ServerPlayer player, boolean isEdit) {
        if (player.hasPermissions(2)) {
            LOGGER.info("Validating preset: " + preset.getName());
            if (validatePreset(preset)) {
                if (preset.getCreator() == null) {
                    preset.setCreator(player.getUUID().toString());
                }
                Preset existingPreset = presets.stream()
                        .filter(p -> p.getName().equals(preset.getName()))
                        .findFirst()
                        .orElse(null);

                if (existingPreset != null) {
                    if (isEdit) {
                        if (!existingPreset.getCreator().equals(player.getUUID().toString()) && !player.hasPermissions(4)) {
                            player.sendSystemMessage(Component.literal("You can only edit your own presets."));
                            LOGGER.warn("Player " + player.getName().getString() + " attempted to edit preset they don't own: " + preset.getName());
                            return;
                        }
                        presets.remove(existingPreset);
                        presets.add(preset);
                        savePresetToFile(preset);
                        player.sendSystemMessage(Component.literal("Preset updated: " + preset.getName()));
                        LOGGER.info("Player " + player.getName().getString() + " updated preset: " + preset.getName());
                    } else {
                        player.sendSystemMessage(Component.literal("A preset with the name '" + preset.getName() + "' already exists."));
                        LOGGER.warn("Player " + player.getName().getString() + " attempted to add duplicate preset: " + preset.getName());
                        return;
                    }
                } else {
                    presets.add(preset);
                    savePresetToFile(preset);
                    player.sendSystemMessage(Component.literal("Preset added: " + preset.getName()));
                    LOGGER.info("Player " + player.getName().getString() + " added preset: " + preset.getName());
                }
                syncPresetsToClients();
            } else {
                player.sendSystemMessage(Component.literal("Invalid preset data."));
                LOGGER.warn("Player " + player.getName().getString() + " attempted to add invalid preset: " + preset.getName());
            }
        } else {
            player.sendSystemMessage(Component.literal("You do not have permission to add presets."));
            LOGGER.warn("Player " + player.getName().getString() + " attempted to add preset without permission");
        }
    }

    public static void removePreset(String name, ServerPlayer player) {
        if (player.hasPermissions(2)) {
            Preset preset = presets.stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst()
                    .orElse(null);
            if (preset == null) {
                player.sendSystemMessage(Component.literal("Preset not found: " + name));
                LOGGER.warn("Player " + player.getName().getString() + " attempted to remove non-existent preset: " + name);
                return;
            }
            if (preset.getCreator() != null && !preset.getCreator().equals(player.getUUID().toString()) && !player.hasPermissions(4)) {
                player.sendSystemMessage(Component.literal("You can only delete your own presets."));
                LOGGER.warn("Player " + player.getName().getString() + " attempted to remove preset they don't own: " + name);
                return;
            }
            presets.removeIf(p -> p.getName().equals(name));
            removePresetFile(name);
            syncPresetsToClients();
            player.sendSystemMessage(Component.literal("Preset removed: " + name));
            LOGGER.info("Player " + player.getName().getString() + " removed preset: " + name);
        } else {
            player.sendSystemMessage(Component.literal("You do not have permission to remove presets."));
            LOGGER.warn("Player " + player.getName().getString() + " attempted to remove preset without permission");
        }
    }

    public static void spawnMobForPreset(ServerPlayer player, String presetName, Vec3 position) {
        if (player.hasPermissions(2)) {
            Preset preset = presets.stream()
                    .filter(p -> p.getName().equals(presetName))
                    .findFirst()
                    .orElse(null);
            if (preset == null) {
                File presetFile = new File(PRESET_DIR, presetName + ".json");
                if (presetFile.exists()) {
                    try (Reader reader = new FileReader(presetFile)) {
                        preset = GSON.fromJson(reader, Preset.class);
                        if (preset != null) {
                            LOGGER.info("Loaded preset for spawning: " + presetName);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to load preset for spawning: " + presetName, e);
                    }
                }
                if (preset == null) {
                    player.sendSystemMessage(Component.literal("Preset not found: " + presetName));
                    LOGGER.warn("Player " + player.getName().getString() + " attempted to spawn non-existent preset: " + presetName);
                    return;
                }
            }
            if (preset.getCreator() != null && !preset.getCreator().equals(player.getUUID().toString()) && !player.hasPermissions(4)) {
                player.sendSystemMessage(Component.literal("You can only spawn your own presets."));
                LOGGER.warn("Player " + player.getName().getString() + " attempted to spawn preset they don't own: " + presetName);
                return;
            }
            ServerMobHandler.spawnCustomMob(
                    player.serverLevel(),
                    preset,
                    position.x,
                    position.y,
                    position.z
            );
            player.sendSystemMessage(Component.literal("Spawned mob: " + preset.getName()));
            LOGGER.info("Player " + player.getName().getString() + " spawned mob from preset: " + presetName);
        } else {
            player.sendSystemMessage(Component.literal("You do not have permission to spawn mobs."));
            LOGGER.warn("Player " + player.getName().getString() + " attempted to spawn mob without permission");
        }
    }

    public static void syncPresetsToClient(ServerPlayer player) {
        if (channel == null) {
            throw new IllegalStateException("PresetManager not initialized with a SimpleChannel");
        }
        List<Preset> playerPresets = player.hasPermissions(4)
                ? new ArrayList<>(presets)
                : presets.stream()
                .filter(p -> p.getCreator() != null && p.getCreator().equals(player.getUUID().toString()))
                .collect(Collectors.toList());
        channel.send(PacketDistributor.PLAYER.with(() -> player), new PresetPacket(playerPresets));
        LOGGER.info("Synced presets to player: " + player.getName().getString());
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
        if (preset.getName() == null || preset.getName().isEmpty()) {
            LOGGER.warn("Validation failed: Name is null or empty");
            return false;
        }
        if (preset.getModel() == null || preset.getModel().isEmpty()) {
            LOGGER.warn("Validation failed: Model is null or empty");
            return false;
        }
        if (preset.getAnimation() == null || preset.getAnimation().isEmpty()) {
            LOGGER.warn("Validation failed: Animation is null or empty");
            return false;
        }
        if (preset.getTexture() == null || preset.getTexture().isEmpty()) {
            LOGGER.warn("Validation failed: Texture is null or empty");
            return false;
        }
        if (preset.getBehavior() == null || preset.getBehavior().isEmpty()) {
            LOGGER.warn("Validation failed: Behavior is null or empty");
            return false;
        }
        if (preset.getHp() <= 0) {
            LOGGER.warn("Validation failed: HP is <= 0");
            return false;
        }
        if (preset.getSpeed() <= 0) {
            LOGGER.warn("Validation failed: Speed is <= 0");
            return false;
        }
        if (preset.getSize() <= 0) {
            LOGGER.warn("Validation failed: Size is <= 0");
            return false;
        }
        if (preset.getHp() > 1000) {
            LOGGER.warn("Validation failed: HP is > 1000");
            return false;
        }
        if (preset.getSpeed() > 2.0f) {
            LOGGER.warn("Validation failed: Speed is > 2.0");
            return false;
        }
        if (preset.getSize() > 5.0f) {
            LOGGER.warn("Validation failed: Size is > 5.0");
            return false;
        }
        String behavior = preset.getBehavior().toLowerCase();
        if (!behavior.equals("hostile") && !behavior.equals("passive") && !behavior.equals("neutral")) {
            LOGGER.warn("Validation failed: Behavior is invalid: " + behavior);
            return false;
        }

        // Валидация ресурсов
        ResourceConfig config = ResourceConfig.getInstance();
        if (!config.getModels().stream().anyMatch(entry -> entry.id.equals(preset.getModel()))) {
            LOGGER.warn("Validation failed: Invalid model: " + preset.getModel());
            return false;
        }
        if (!config.getAnimations().stream().anyMatch(entry -> entry.id.equals(preset.getAnimation()))) {
            LOGGER.warn("Validation failed: Invalid animation: " + preset.getAnimation());
            return false;
        }
        if (!config.getTextures().stream().anyMatch(entry -> entry.id.equals(preset.getTexture()))) {
            LOGGER.warn("Validation failed: Invalid texture: " + preset.getTexture());
            return false;
        }

        LOGGER.info("Preset validation successful: " + preset.getName());
        return true;
    }
}