package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.RequestPresetsPacket;
import com.custommobsforge.custommobsforge.common.preset.SpawnMobPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ServerCustomMobsForge.MOD_ID)
@Mod.EventBusSubscriber(modid = ServerCustomMobsForge.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerCustomMobsForge {
    public static final String MOD_ID = "custommobsforge_server";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ServerCustomMobsForge() {
        CustomMobsForge.createEntityRegistry().register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        LOGGER.info("ServerCustomMobsForge initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SimpleChannel channel = CustomMobsForge.CHANNEL;
            int packetId = 0;

            channel.messageBuilder(PresetSavePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(PresetSavePacket::encode)
                    .decoder(PresetSavePacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        LOGGER.info("Received PresetSavePacket for preset: {}, isEdit: {}", msg.getPreset().getName(), msg.isEdit());
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            LOGGER.info("Processing PresetSavePacket for player: {}", player.getName().getString());
                            PresetManager.addPreset(msg.getPreset(), player, msg.isEdit());
                        } else {
                            LOGGER.warn("Received PresetSavePacket but player is null");
                        }
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            channel.messageBuilder(PresetDeletePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(PresetDeletePacket::encode)
                    .decoder(PresetDeletePacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        LOGGER.info("Received PresetDeletePacket for preset: {}", msg.getPresetName());
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            PresetManager.removePreset(msg.getPresetName(), player);
                        }
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            channel.messageBuilder(SpawnMobPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(SpawnMobPacket::encode)
                    .decoder(SpawnMobPacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            PresetManager.spawnMobForPreset(player, msg.getPresetName(), msg.getPosition());
                        }
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            channel.messageBuilder(PresetPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(PresetPacket::encode)
                    .decoder(PresetPacket::decode)
                    .consumerMainThread((msg, ctx) -> ctx.get().setPacketHandled(true))
                    .add();

            channel.messageBuilder(RequestPresetsPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(RequestPresetsPacket::encode)
                    .decoder(RequestPresetsPacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            PresetManager.syncPresetsToClient(player, true);
                        }
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            PresetManager.init(channel);
        });
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ResourceConfig.init();
        CustomMobsForge.LOGGER.info("ResourceConfig initialized on server start");
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CustomCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PresetManager.syncPresetsToClient(player, true);
            CustomMobsForge.LOGGER.info("Synced presets to player: {}", player.getName().getString());
        }
    }
}