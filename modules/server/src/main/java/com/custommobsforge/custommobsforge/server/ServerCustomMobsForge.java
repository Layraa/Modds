package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.SpawnMobPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Mod(value = ServerCustomMobsForge.MOD_ID)
@Mod.EventBusSubscriber(modid = ServerCustomMobsForge.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerCustomMobsForge {
    public static final String MOD_ID = "custommobsforge_server";

    public ServerCustomMobsForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        CustomMobsForge.createEntityRegistry().register(modEventBus);
        CustomMobsForge.LOGGER.info("ServerCustomMobsForge initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        int packetId = 0;
        CustomMobsForge.CHANNEL.registerMessage(packetId++, PresetPacket.class, PresetPacket::encode, PresetPacket::decode, (msg, ctx) -> {
            ctx.get().setPacketHandled(true);
        });
        CustomMobsForge.CHANNEL.registerMessage(packetId++, PresetSavePacket.class, PresetSavePacket::encode, PresetSavePacket::decode, this::handlePresetSave);
        CustomMobsForge.CHANNEL.registerMessage(packetId++, PresetDeletePacket.class, PresetDeletePacket::encode, PresetDeletePacket::decode, this::handlePresetDelete);
        CustomMobsForge.CHANNEL.registerMessage(packetId++, SpawnMobPacket.class, SpawnMobPacket::encode, SpawnMobPacket::decode, this::handleSpawnMob);

        PresetManager.init(CustomMobsForge.CHANNEL);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CustomCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PresetManager.syncPresetsToClient(player);
            CustomMobsForge.LOGGER.info("Synced presets to player: " + player.getName().getString());
        }
    }

    private void handlePresetSave(PresetSavePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PresetManager.addPreset(msg.getPreset(), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handlePresetDelete(PresetDeletePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PresetManager.removePreset(msg.getPresetName(), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handleSpawnMob(SpawnMobPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.hasPermissions(2)) {
                PresetManager.spawnMobForPreset(player, msg.getPresetName(), msg.getPosition());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}