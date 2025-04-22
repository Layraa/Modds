package com.custommobsforge.custommobsforge;

import com.custommobsforge.custommobsforge.common.ModEntities;
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.OpenGuiPacket;
import com.custommobsforge.custommobsforge.common.network.PresetCreatePacket;
import com.custommobsforge.custommobsforge.common.network.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.network.PresetEditPacket;
import com.custommobsforge.custommobsforge.common.network.PresetSyncPacket;
import com.custommobsforge.custommobsforge.common.network.RequestPresetsPacket;
import com.custommobsforge.custommobsforge.common.network.ResourceListRequestPacket;
import com.custommobsforge.custommobsforge.common.network.ResourceListResponsePacket;
import com.custommobsforge.custommobsforge.common.network.SpawnMobPacket;
import com.custommobsforge.custommobsforge.common.network.ValidateResourcesPacket;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Mod(value = ServerCustomMobsForge.MOD_ID)
public class ServerCustomMobsForge {
    public static final String MOD_ID = "custommobsforge_server";
    private static final Logger LOGGER = LogManager.getLogger();

    public ServerCustomMobsForge() {
        LOGGER.info("ServerCustomMobsForge: Initializing");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // Регистрируем сущности
        ModEntities.register(modEventBus);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ServerCustomMobsForge: Common setup");
        NetworkHandler.register();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEventHandler {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            LOGGER.info("ServerCustomMobsForge: Registering commands");
            event.getDispatcher().register(
                    Commands.literal("custommobsforge")
                            .executes(context -> {
                                NetworkHandler.sendToPlayer(new OpenGuiPacket(), context.getSource().getPlayerOrException());
                                return 1;
                            })
            );
        }

        @SubscribeEvent
        public static void onSpawnMob(SpawnMobPacket packet, ServerPlayer player) {
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

        @SubscribeEvent
        public static void onRequestPresets(RequestPresetsPacket packet, ServerPlayer player) {
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

        @SubscribeEvent
        public static void onPresetCreate(PresetCreatePacket packet, ServerPlayer player) {
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

        @SubscribeEvent
        public static void onPresetEdit(PresetEditPacket packet, ServerPlayer player) {
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

        @SubscribeEvent
        public static void onPresetDelete(PresetDeletePacket packet, ServerPlayer player) {
            PresetManager.getInstance().removePreset(packet.getName());
        }

        @SubscribeEvent
        public static void onResourceListRequest(ResourceListRequestPacket packet, ServerPlayer player) {
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
                resources = Files.walk(dir)
                        .filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .map(name -> name.substring(0, name.lastIndexOf('.')))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                LOGGER.error("Failed to list resources for type {}: {}", type, e.getMessage());
                resources = List.of();
            }

            NetworkHandler.sendToPlayer(new ResourceListResponsePacket(type, resources), player);
        }

        @SubscribeEvent
        public static void onValidateResources(ValidateResourcesPacket packet, ServerPlayer player) {
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
    }
}