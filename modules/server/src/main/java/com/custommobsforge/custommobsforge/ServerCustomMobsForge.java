package com.custommobsforge.custommobsforge;

import com.custommobsforge.custommobsforge.common.ModEntities;
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.PresetSyncPacket;
import com.custommobsforge.custommobsforge.common.network.RequestPresetsPacket;
import com.custommobsforge.custommobsforge.common.network.ServerCheckPacket;
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
import net.minecraftforge.network.NetworkDirection;

@Mod(CustomMobsForge.MOD_ID)
@SuppressWarnings("unused")
public class CustomMobsForge {
    public static final String MOD_ID = "custommobsforge";

    public CustomMobsForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        ModEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void serverSetup(final FMLDedicatedServerSetupEvent event) {
        NetworkHandler.register();
        // Регистрируем обработчик для ServerCheckPacket
        NetworkHandler.INSTANCE.messageBuilder(ServerCheckPacket.class, 5, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerCheckPacket::new)
                .encoder(ServerCheckPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        NetworkHandler.sendToPlayer(new ServerCheckPacket(), player);
                    }
                    context.get().setPacketHandled(true);
                })
                .add();

        // Регистрируем обработчик для RequestPresetsPacket
        NetworkHandler.INSTANCE.messageBuilder(RequestPresetsPacket.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestPresetsPacket::new)
                .encoder(RequestPresetsPacket::write)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
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
                    context.get().setPacketHandled(true);
                })
                .add();

        // Добавим тестовый пресет при запуске сервера
        PresetManager.getInstance().addPreset("Wolf", 20, 0.3, "wolf", "wolf", "wolf");
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // Синхронизация пресетов с клиентом при входе игрока
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
        );
    }
}