package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.RequestPresetsPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PresetManager {
    private static final Logger LOGGER = LogManager.getLogger("CustomMobsForge");
    private static final File PRESET_DIR = new File("config/custommobsforge/presets/");
    private static final Map<String, Preset> presets = new HashMap<>();
    private static SimpleChannel channel;
    private static final String RESOURCE_NAMESPACE = "custommobsforge_client";

    private static class PresetConfig {
        static final int MAX_HP = 1000;
        static final float MAX_SPEED = 2.0f;
        static final float MAX_SIZE = 5.0f;
        static final List<String> VALID_BEHAVIORS = List.of("hostile", "passive", "neutral");
    }

    public static void init(SimpleChannel channel) {
        PresetManager.channel = channel;
        loadPresets();
    }

    public static void loadPresets() {
        presets.clear();
        if (!PRESET_DIR.exists()) {
            PRESET_DIR.mkdirs();
            LOGGER.info("Created preset directory: {}", PRESET_DIR.getPath());
            return;
        }

        File[] presetFiles = PRESET_DIR.listFiles((dir, name) -> name.endsWith(".bin"));
        if (presetFiles == null || presetFiles.length == 0) {
            LOGGER.info("No preset files found in directory: {}", PRESET_DIR.getPath());
            return;
        }

        for (File file : presetFiles) {
            try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
                byte[] bytes = new byte[input.available()];
                input.readFully(bytes);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
                Preset preset = Preset.readFromBuf(buf);
                presets.put(preset.getName(), preset);
                LOGGER.info("Loaded preset: {} from file: {}", preset.getName(), file.getName());
            } catch (IllegalStateException e) {
                LOGGER.error("Skipping invalid preset file: {}. Reason: {}", file.getName(), e.getMessage());
                if (file.delete()) {
                    LOGGER.info("Deleted corrupted preset file: {}", file.getName());
                } else {
                    LOGGER.warn("Failed to delete corrupted preset file: {}", file.getName());
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load preset from file: {}. Reason: {}", file.getName(), e.getMessage(), e);
            }
        }
    }

    private static void savePresetToFile(Preset preset) {
        File presetFile = new File(PRESET_DIR, preset.getName() + ".bin");
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(presetFile))) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            preset.writeToBuf(buf);
            output.write(buf.array(), 0, buf.readableBytes());
            LOGGER.info("Saved preset: {} to file: {}", preset.getName(), presetFile.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to save preset: {}. Reason: {}", preset.getName(), e.getMessage(), e);
        }
    }

    private static void removePresetFile(String presetName) {
        File presetFile = new File(PRESET_DIR, presetName + ".bin");
        if (presetFile.exists()) {
            if (presetFile.delete()) {
                LOGGER.info("Deleted preset file: {}", presetFile.getName());
            } else {
                LOGGER.error("Failed to delete preset file: {}", presetFile.getName());
            }
        }
    }

    public static void addPreset(Preset preset, ServerPlayer player, boolean isEdit) {
        if (!player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("You do not have permission to add presets."));
            LOGGER.warn("Player {} attempted to add preset without permission", player.getName().getString());
            return;
        }

        try {
            if (!validatePreset(preset)) {
                player.sendSystemMessage(Component.literal("Invalid preset data."));
                LOGGER.warn("Player {} attempted to add invalid preset: {}", player.getName().getString(), preset.getName());
                return;
            }
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("Failed to validate preset data."));
            LOGGER.error("Validation error for preset {} by player {}: {}", preset.getName(), player.getName().getString(), e.getMessage(), e);
            return;
        }

        if (preset.getCreator() == null) {
            preset.setCreator(player.getUUID().toString());
        }
        preset.setResourceNamespace(RESOURCE_NAMESPACE);

        Preset existingPreset = presets.get(preset.getName());
        if (existingPreset != null) {
            if (isEdit) {
                if (!existingPreset.getCreator().equals(player.getUUID().toString()) && !player.hasPermissions(4)) {
                    player.sendSystemMessage(Component.literal("You can only edit your own presets."));
                    LOGGER.warn("Player {} attempted to edit preset they don't own: {}", player.getName().getString(), preset.getName());
                    return;
                }
                presets.put(preset.getName(), preset);
                savePresetToFile(preset);
                player.sendSystemMessage(Component.literal("Preset updated: " + preset.getName()));
                LOGGER.info("Player {} updated preset: {}", player.getName().getString(), preset.getName());
                syncPresetsToClients(PresetPacket.Operation.UPDATE, List.of(preset), List.of());
            } else {
                player.sendSystemMessage(Component.literal("A preset with the name '" + preset.getName() + "' already exists."));
                LOGGER.warn("Player {} attempted to add duplicate preset: {}", player.getName().getString(), preset.getName());
            }
            return;
        }

        presets.put(preset.getName(), preset);
        savePresetToFile(preset);
        player.sendSystemMessage(Component.literal("Preset added: " + preset.getName()));
        LOGGER.info("Player {} added preset: {}", player.getName().getString(), preset.getName());
        syncPresetsToClients(PresetPacket.Operation.ADD, List.of(preset), List.of());
    }

    public static void removePreset(String name, ServerPlayer player) {
        if (!player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("You do not have permission to remove presets."));
            LOGGER.warn("Player {} attempted to remove preset without permission", player.getName().getString());
            return;
        }

        Preset preset = presets.get(name);
        if (preset == null) {
            player.sendSystemMessage(Component.literal("Preset not found: " + name));
            LOGGER.warn("Player {} attempted to remove non-existent preset: {}", player.getName().getString(), name);
            return;
        }

        if (preset.getCreator() != null && !preset.getCreator().equals(player.getUUID().toString()) && !player.hasPermissions(4)) {
            player.sendSystemMessage(Component.literal("You can only delete your own presets."));
            LOGGER.warn("Player {} attempted to remove preset they don't own: {}", player.getName().getString(), name);
            return;
        }

        presets.remove(name);
        removePresetFile(name);
        player.sendSystemMessage(Component.literal("Preset removed: " + name));
        LOGGER.info("Player {} removed preset: {}", player.getName().getString(), name);
        syncPresetsToClients(PresetPacket.Operation.DELETE, List.of(), List.of(name));
    }

    public static void spawnMobForPreset(ServerPlayer player, String presetName, Vec3 position) {
        if (!player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("You do not have permission to spawn mobs."));
            LOGGER.warn("Player {} attempted to spawn mob without permission", player.getName().getString());
            return;
        }

        Preset preset = presets.get(presetName);
        if (preset == null) {
            File presetFile = new File(PRESET_DIR, presetName + ".bin");
            if (presetFile.exists()) {
                try (DataInputStream input = new DataInputStream(new FileInputStream(presetFile))) {
                    byte[] bytes = new byte[input.available()];
                    input.readFully(bytes);
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
                    preset = Preset.readFromBuf(buf);
                    presets.put(presetName, preset);
                    LOGGER.info("Loaded preset for spawning: {}", presetName);
                } catch (IllegalStateException e) {
                    LOGGER.error("Skipping invalid preset file for spawning: {}. Reason: {}", presetName, e.getMessage());
                } catch (IOException e) {
                    LOGGER.error("Failed to load preset for spawning: {}. Reason: {}", presetName, e.getMessage(), e);
                }
            }
            if (preset == null) {
                player.sendSystemMessage(Component.literal("Preset not found: " + presetName));
                LOGGER.warn("Player {} attempted to spawn non-existent preset: {}", player.getName().getString(), presetName);
                return;
            }
        }

        if (preset.getCreator() != null && !preset.getCreator().equals(player.getUUID().toString()) && !player.hasPermissions(4)) {
            player.sendSystemMessage(Component.literal("You can only spawn your own presets."));
            LOGGER.warn("Player {} attempted to spawn preset they don't own: {}", player.getName().getString(), presetName);
            return;
        }

        preset.setResourceNamespace(RESOURCE_NAMESPACE);
        LOGGER.info("Spawning mob for preset: {} at position: ({}, {}, {})", preset.getName(), position.x, position.y, position.z);
        ServerMobHandler.spawnCustomMob(player.serverLevel(), preset, position.x, position.y, position.z);
    }

    public static void syncPresetsToClient(ServerPlayer player, boolean fullUpdate) {
        if (channel == null) {
            throw new IllegalStateException("PresetManager not initialized with a SimpleChannel");
        }
        List<Preset> playerPresets = new ArrayList<>(presets.values());
        for (Preset preset : playerPresets) {
            preset.setResourceNamespace(RESOURCE_NAMESPACE);
        }
        // Всегда отправляем PresetPacket, даже если пресетов нет
        channel.send(PacketDistributor.PLAYER.with(() -> player), new PresetPacket(
                fullUpdate ? PresetPacket.Operation.FULL_UPDATE : PresetPacket.Operation.ADD,
                playerPresets,
                List.of()
        ));
        LOGGER.info("Synced {} presets to player: {}", playerPresets.size(), player.getName().getString());
    }

    public static void syncPresetsToClients(PresetPacket.Operation operation, List<Preset> changedPresets, List<String> deletedPresetNames) {
        if (channel == null) {
            throw new IllegalStateException("PresetManager not initialized with a SimpleChannel");
        }
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            List<Preset> playerPresets = new ArrayList<>(changedPresets);
            for (Preset preset : playerPresets) {
                preset.setResourceNamespace(RESOURCE_NAMESPACE);
            }
            List<String> playerPresetNames = new ArrayList<>(deletedPresetNames);
            if (!playerPresets.isEmpty() || !playerPresetNames.isEmpty()) {
                channel.send(PacketDistributor.PLAYER.with(() -> player), new PresetPacket(operation, playerPresets, playerPresetNames));
                LOGGER.info("Synced operation {} to player {}: {} presets, {} deleted names", operation, player.getName().getString(), playerPresets.size(), playerPresetNames.size());
            }
        }
    }

    public static List<Preset> getPresets() {
        return new ArrayList<>(presets.values());
    }

    public static Preset getPreset(String name) {
        return presets.get(name);
    }

    private static boolean validatePreset(Preset preset) {
        try {
            if (preset == null) {
                LOGGER.warn("Validation failed: Preset is null");
                return false;
            }
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
            if (preset.getHp() > PresetConfig.MAX_HP) {
                LOGGER.warn("Validation failed: HP is > {}", PresetConfig.MAX_HP);
                return false;
            }
            if (preset.getSpeed() > PresetConfig.MAX_SPEED) {
                LOGGER.warn("Validation failed: Speed is > {}", PresetConfig.MAX_SPEED);
                return false;
            }
            if (preset.getSize() > PresetConfig.MAX_SIZE) {
                LOGGER.warn("Validation failed: Size is > {}", PresetConfig.MAX_SIZE);
                return false;
            }
            String behavior = preset.getBehavior().toLowerCase();
            if (!PresetConfig.VALID_BEHAVIORS.contains(behavior)) {
                LOGGER.warn("Validation failed: Behavior is invalid: {}", behavior);
                return false;
            }

            if (!ResourceConfig.validateResources(preset.getModel(), preset.getAnimation(), preset.getTexture())) {
                LOGGER.warn("Validation failed: Resources (model, animation, or texture) not found for preset: {}", preset.getName());
                return false;
            }

            LOGGER.info("Preset validation successful: {}", preset.getName());
            return true;
        } catch (Exception e) {
            LOGGER.error("Unexpected error during preset validation for preset {}: {}", preset != null ? preset.getName() : "null", e.getMessage(), e);
            return false;
        }
    }
}