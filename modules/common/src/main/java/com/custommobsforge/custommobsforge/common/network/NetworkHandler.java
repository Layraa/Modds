package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.entity.ModEntities;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetManager;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class NetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("custommobsforge", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(OpenGuiPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenGuiPacket::new)
                .encoder(OpenGuiPacket::write)
                .consumerMainThread((packet, context) -> {})
                .add();

        CHANNEL.messageBuilder(SpawnMobPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpawnMobPacket::new)
                .encoder(SpawnMobPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        Level level = player.level();
                        var preset = PresetManager.getInstance().getPreset(packet.getPresetName());
                        if (preset != null) {
                            CustomMob mob = new CustomMob(ModEntities.CUSTOM_MOB.get(), level);
                            mob.setModelName(preset.modelName());
                            mob.setTextureName(preset.textureName());
                            mob.setAnimationName(preset.animationName());
                            mob.setHealthValue(preset.health());
                            mob.setSpeedValue(preset.speed());
                            mob.setSize(preset.sizeWidth(), preset.sizeHeight());
                            mob.setPos(player.getX(), player.getY(), player.getZ());
                            mob.refreshDimensions();
                            level.addFreshEntity(mob);
                        }
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(RequestPresetsPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestPresetsPacket::new)
                .encoder(RequestPresetsPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        for (Preset preset : PresetManager.getInstance().getPresets()) {
                            NetworkHandler.sendToPlayer(new PresetSyncPacket(
                                    preset.name(),
                                    preset.health(),
                                    preset.speed(),
                                    preset.sizeWidth(),
                                    preset.sizeHeight(),
                                    preset.modelName(),
                                    preset.textureName(),
                                    preset.animationName()
                            ), player);
                        }
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(PresetSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PresetSyncPacket::new)
                .encoder(PresetSyncPacket::write)
                .consumerMainThread((packet, context) -> {})
                .add();

        CHANNEL.messageBuilder(PresetCreatePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetCreatePacket::new)
                .encoder(PresetCreatePacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        handlePresetUpdate(packet.getName(), packet.getHealth(), packet.getSpeed(),
                                packet.getSizeWidth(), packet.getSizeHeight(), packet.getModelName(),
                                packet.getTextureName(), packet.getAnimationName(), false, player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(PresetEditPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetEditPacket::new)
                .encoder(PresetEditPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        handlePresetUpdate(packet.getName(), packet.getHealth(), packet.getSpeed(),
                                packet.getSizeWidth(), packet.getSizeHeight(), packet.getModelName(),
                                packet.getTextureName(), packet.getAnimationName(), true, player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(PresetDeletePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetDeletePacket::new)
                .encoder(PresetDeletePacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        PresetManager.getInstance().removePreset(packet.getName());
                        NetworkHandler.sendToPlayer(new PresetDeleteSyncPacket(packet.getName()), player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(PresetDeleteSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PresetDeleteSyncPacket::new)
                .encoder(PresetDeleteSyncPacket::write)
                .consumerMainThread((packet, context) -> {})
                .add();

        CHANNEL.messageBuilder(ResourceListRequestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ResourceListRequestPacket::new)
                .encoder(ResourceListRequestPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        String type = packet.getType();
                        String pathPrefix = switch (type) {
                            case "model" -> "geo";
                            case "texture" -> "textures";
                            case "animation" -> "animations";
                            default -> throw new IllegalArgumentException("Unknown resource type: " + type);
                        };

                        List<String> resources;
                        try {
                            resources = getResourceList(pathPrefix);
                        } catch (IOException e) {
                            LOGGER.error("Failed to list resources for type {}: {}", type, e.getMessage());
                            resources = List.of();
                        }

                        NetworkHandler.sendToPlayer(new ResourceListResponsePacket(type, resources), player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(ResourceListResponsePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceListResponsePacket::new)
                .encoder(ResourceListResponsePacket::write)
                .consumerMainThread((packet, context) -> {})
                .add();

        CHANNEL.messageBuilder(ValidateResourcesPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ValidateResourcesPacket::new)
                .encoder(ValidateResourcesPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        boolean valid = true;

                        String modelPath = "geo/" + packet.getModel() + ".json";
                        String texturePath = "textures/" + packet.getTexture() + ".png";
                        String animationPath = "animations/" + packet.getAnimation() + ".json";

                        if (resourceMissing(modelPath)) {
                            LOGGER.warn("Model file does not exist in mod resources: {}", modelPath);
                            valid = false;
                        }
                        if (resourceMissing(texturePath)) {
                            LOGGER.warn("Texture file does not exist in mod resources: {}", texturePath);
                            valid = false;
                        }
                        if (resourceMissing(animationPath)) {
                            LOGGER.warn("Animation file does not exist in mod resources: {}", animationPath);
                            valid = false;
                        }

                        NetworkHandler.sendToPlayer(new ResourceValidationResponsePacket(
                                valid,
                                packet.isCreateMode(),
                                packet.getName(),
                                packet.getHealth(),
                                packet.getSpeed(),
                                packet.getSizeWidth(),
                                packet.getSizeHeight(),
                                packet.getModel(),
                                packet.getTexture(),
                                packet.getAnimation()
                        ), player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(ResourceValidationResponsePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceValidationResponsePacket::new)
                .encoder(ResourceValidationResponsePacket::write)
                .consumerMainThread((packet, context) -> {})
                .add();
    }

    private static List<String> getResourceList(String pathPrefix) throws IOException {
        List<String> resources = new java.util.ArrayList<>();
        try (var stream = NetworkHandler.class.getResourceAsStream("/assets/custommobsforge/" + pathPrefix)) {
            if (stream == null) {
                return resources;
            }
            resources.add("test_" + pathPrefix.substring(0, pathPrefix.length() - 1));
        }
        return resources;
    }

    private static boolean resourceMissing(String path) {
        try (InputStream inputStream = NetworkHandler.class.getResourceAsStream("/assets/custommobsforge/" + path)) {
            return inputStream == null;
        } catch (IOException e) {
            LOGGER.error("Failed to check resource {}: {}", path, e.getMessage());
            return true;
        }
    }

    private static void handlePresetUpdate(String name, float health, double speed, float sizeWidth, float sizeHeight,
                                           String modelName, String textureName, String animationName,
                                           boolean isEdit, ServerPlayer player) {
        if (isEdit) {
            PresetManager.getInstance().removePreset(name);
        }
        PresetManager.getInstance().addPreset(
                name,
                health,
                speed,
                sizeWidth,
                sizeHeight,
                modelName,
                textureName,
                animationName
        );
        NetworkHandler.sendToPlayer(new PresetSyncPacket(
                name,
                health,
                speed,
                sizeWidth,
                sizeHeight,
                modelName,
                textureName,
                animationName
        ), player);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}