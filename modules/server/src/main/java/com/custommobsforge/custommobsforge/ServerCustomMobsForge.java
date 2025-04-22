package com.custommobsforge.custommobsforge;

import com.custommobsforge.custommobsforge.common.ModEntities;
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.PresetSyncPacket;
import com.custommobsforge.custommobsforge.common.network.OpenGuiPacket;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ServerCustomMobsForge.MOD_ID)
@SuppressWarnings("unused")
public class ServerCustomMobsForge {
    public static final String MOD_ID = "custommobsforge";
    private static final Logger LOGGER = LogManager.getLogger();

    public ServerCustomMobsForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        ModEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void serverSetup(final FMLDedicatedServerSetupEvent event) {
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PresetManager.getInstance().getAllPresets().forEach((name, preset) ->
                NetworkHandler.sendToPlayer(
                        new PresetSyncPacket(
                                preset.name(),
                                preset.health(),
                                preset.speed(),
                                preset.modelName(),
                                preset.textureName(),
                                preset.animationName()
                        ),
                        player
                )
        );
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("custommobsforge")
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("preset", StringArgumentType.string())
                                        .executes(context -> {
                                            String presetName = StringArgumentType.getString(context, "preset");
                                            PresetManager.Preset preset = PresetManager.getInstance().getPreset(presetName);
                                            if (preset == null) {
                                                context.getSource().sendFailure(Component.literal("Preset not found: " + presetName));
                                                return 0;
                                            }

                                            ServerLevel world = context.getSource().getLevel();
                                            CustomMob mob = new CustomMob(ModEntities.CUSTOM_MOB.get(), world);
                                            mob.setPos(
                                                    context.getSource().getPosition().x(),
                                                    context.getSource().getPosition().y(),
                                                    context.getSource().getPosition().z()
                                            );
                                            mob.setPresetName(presetName);
                                            mob.setHealth(preset.health());
                                            if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                                                mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(preset.speed());
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Failed to set speed for mob: Attribute not found"));
                                            }
                                            world.addFreshEntity(mob);
                                            context.getSource().sendSuccess(() -> Component.literal("Spawned mob with preset: " + presetName), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("syncpresets")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player != null) {
                                        PresetManager.getInstance().getAllPresets().forEach((name, preset) ->
                                                NetworkHandler.sendToPlayer(
                                                        new PresetSyncPacket(
                                                                preset.name(),
                                                                preset.health(),
                                                                preset.speed(),
                                                                preset.modelName(),
                                                                preset.textureName(),
                                                                preset.animationName()
                                                        ),
                                                        player
                                                )
                                        );
                                    }
                                    return 1;
                                })
                        )
                        .then(Commands.literal("gui")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player != null) {
                                        LOGGER.info("Sending OpenGuiPacket to player: {}", player.getName().getString());
                                        NetworkHandler.sendToPlayer(new OpenGuiPacket(), player);
                                        context.getSource().sendSuccess(() -> Component.literal("Opening Custom Mobs GUI..."), false);
                                    } else {
                                        LOGGER.warn("Player is null when executing /custommobsforge gui");
                                        context.getSource().sendFailure(Component.literal("Cannot open GUI: Player not found."));
                                        return 0;
                                    }
                                    return 1;
                                })
                        )
        );
    }
}