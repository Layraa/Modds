package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.client.gui.ClientCommands;
import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.SpawnMobPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Mod(value = ClientCustomMobsForge.MOD_ID)
@Mod.EventBusSubscriber(modid = ClientCustomMobsForge.MOD_ID, value = Dist.CLIENT)
public class ClientCustomMobsForge {
    public static final String MOD_ID = "custommobsforge";

    public ClientCustomMobsForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::clientSetup);
        CustomMobsForge.createEntityRegistry().register(modEventBus);
        CustomMobsForge.LOGGER.info("ClientCustomMobsForge initialized");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        int packetId = 0;
        CustomMobsForge.CHANNEL.registerMessage(packetId++, PresetPacket.class, PresetPacket::encode, PresetPacket::decode, this::handlePresetPacket);
        CustomMobsForge.CHANNEL.registerMessage(packetId++, PresetSavePacket.class, PresetSavePacket::encode, PresetSavePacket::decode, (msg, ctx) -> {
            ctx.get().setPacketHandled(true);
        });
        CustomMobsForge.CHANNEL.registerMessage(packetId++, PresetDeletePacket.class, PresetDeletePacket::encode, PresetDeletePacket::decode, (msg, ctx) -> {
            ctx.get().setPacketHandled(true);
        });
        CustomMobsForge.CHANNEL.registerMessage(packetId++, SpawnMobPacket.class, SpawnMobPacket::encode, SpawnMobPacket::decode, (msg, ctx) -> {
            ctx.get().setPacketHandled(true);
        });
        CustomMobsForge.LOGGER.info("Client setup completed");
    }

    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        ClientCommands.register(event.getDispatcher());
        CustomMobsForge.LOGGER.info("Client commands registered");
    }

    private void handlePresetPacket(PresetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientPresetHandler.setPresets(msg.getPresets()));
        ctx.get().setPacketHandled(true);
    }
}