package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.ModEntities;
import com.custommobsforge.custommobsforge.common.PresetManager;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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
                .consumerMainThread(OpenGuiPacket::handle)
                .add();

        CHANNEL.messageBuilder(SpawnMobPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpawnMobPacket::new)
                .encoder(SpawnMobPacket::write)
                .consumerMainThread(SpawnMobPacket::handle)
                .add();

        CHANNEL.messageBuilder(RequestPresetsPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestPresetsPacket::new)
                .encoder(RequestPresetsPacket::write)
                .consumerMainThread(RequestPresetsPacket::handle)
                .add();

        CHANNEL.messageBuilder(PresetSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PresetSyncPacket::new)
                .encoder(PresetSyncPacket::write)
                .consumerMainThread(PresetSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(PresetCreatePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetCreatePacket::new)
                .encoder(PresetCreatePacket::write)
                .consumerMainThread(PresetCreatePacket::handle)
                .add();

        CHANNEL.messageBuilder(PresetEditPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetEditPacket::new)
                .encoder(PresetEditPacket::write)
                .consumerMainThread(PresetEditPacket::handle)
                .add();

        CHANNEL.messageBuilder(PresetDeletePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetDeletePacket::new)
                .encoder(PresetDeletePacket::write)
                .consumerMainThread(PresetDeletePacket::handle)
                .add();

        CHANNEL.messageBuilder(ResourceListRequestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ResourceListRequestPacket::new)
                .encoder(ResourceListRequestPacket::write)
                .consumerMainThread(ResourceListRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(ResourceListResponsePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceListResponsePacket::new)
                .encoder(ResourceListResponsePacket::write)
                .consumerMainThread(ResourceListResponsePacket::handle)
                .add();

        CHANNEL.messageBuilder(ValidateResourcesPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ValidateResourcesPacket::new)
                .encoder(ValidateResourcesPacket::write)
                .consumerMainThread(ValidateResourcesPacket::handle)
                .add();

        CHANNEL.messageBuilder(ResourceValidationResponsePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceValidationResponsePacket::new)
                .encoder(ResourceValidationResponsePacket::write)
                .consumerMainThread(ResourceValidationResponsePacket::handle)
                .add();
    }

    public static void registerServerHandlers() {
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
                            mob.setPresetName(packet.getPresetName());
                            mob.setModelName(preset.modelName());
                            mob.setTextureName(preset.textureName());
                            mob.setAnimationName(preset.animationName());
                            mob.setHealthValue(preset.health());
                            mob.setSpeedValue(preset.speed());
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
                        for (var preset : PresetManager.getInstance().getPresets()) {
                            NetworkHandler.sendToPlayer(new PresetSyncPacket(
                                    preset.name(),
                                    preset.health(),
                                    preset.speed(),
                                    preset.modelName(),
                                    preset.textureName(),
                                    preset.animationName()
                            ), player);
                        }
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(PresetCreatePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetCreatePacket::new)
                .encoder(PresetCreatePacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        PresetManager.getInstance().addPreset(
                                packet.getName(),
                                packet.getHealth(),
                                packet.getSpeed(),
                                packet.getModelName(),
                                packet.getTextureName(),
                                packet.getAnimationName()
                        );
                        NetworkHandler.sendToPlayer(new PresetSyncPacket(
                                packet.getName(),
                                packet.getHealth(),
                                packet.getSpeed(),
                                packet.getModelName(),
                                packet.getTextureName(),
                                packet.getAnimationName()
                        ), player);
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
                        PresetManager.getInstance().removePreset(packet.getName());
                        PresetManager.getInstance().addPreset(
                                packet.getName(),
                                packet.getHealth(),
                                packet.getSpeed(),
                                packet.getModelName(),
                                packet.getTextureName(),
                                packet.getAnimationName()
                        );
                        NetworkHandler.sendToPlayer(new PresetSyncPacket(
                                packet.getName(),
                                packet.getHealth(),
                                packet.getSpeed(),
                                packet.getModelName(),
                                packet.getTextureName(),
                                packet.getAnimationName()
                        ), player);
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
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(ResourceListRequestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ResourceListRequestPacket::new)
                .encoder(ResourceListRequestPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        String type = packet.getType();
                        Path dir = switch (type) {
                            case "model" -> Path.of("config/custommobsforge/models");
                            case "texture" -> Path.of("config/custommobsforge/textures");
                            case "animation" -> Path.of("config/custommobsforge/animations");
                            default -> throw new IllegalArgumentException("Unknown resource type: " + type);
                        };

                        List<String> resources;
                        try {
                            Files.createDirectories(dir);
                            try (var stream = Files.walk(dir)) {
                                resources = stream
                                        .filter(Files::isRegularFile)
                                        .map(Path::getFileName)
                                        .map(Path::toString)
                                        .map(name -> name.substring(0, name.lastIndexOf('.')))
                                        .collect(Collectors.toList());
                            }
                        } catch (IOException e) {
                            LOGGER.error("Failed to list resources for type {}: {}", type, e.getMessage());
                            resources = List.of();
                        }

                        NetworkHandler.sendToPlayer(new ResourceListResponsePacket(type, resources), player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(ValidateResourcesPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ValidateResourcesPacket::new)
                .encoder(ValidateResourcesPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        boolean valid = true;
                        String modelPath = "config/custommobsforge/models/" + packet.getModel() + ".json";
                        String texturePath = "config/custommobsforge/textures/" + packet.getTexture() + ".png";
                        String animationPath = "config/custommobsforge/animations/" + packet.getAnimation() + ".json";

                        if (!Files.exists(Path.of(modelPath))) {
                            LOGGER.warn("Model file does not exist: {}", modelPath);
                            valid = false;
                        }
                        if (!Files.exists(Path.of(texturePath))) {
                            LOGGER.warn("Texture file does not exist: {}", texturePath);
                            valid = false;
                        }
                        if (!Files.exists(Path.of(animationPath))) {
                            LOGGER.warn("Animation file does not exist: {}", animationPath);
                            valid = false;
                        }

                        NetworkHandler.sendToPlayer(new ResourceValidationResponsePacket(
                                valid,
                                packet.isCreateMode(),
                                packet.getName(),
                                packet.getHealth(),
                                packet.getSpeed(),
                                packet.getModel(),
                                packet.getTexture(),
                                packet.getAnimation()
                        ), player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}