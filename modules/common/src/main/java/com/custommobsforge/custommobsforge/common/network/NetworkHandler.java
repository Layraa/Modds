package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("custommobsforge", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        LOGGER.info("Registering network packets for CustomMobsForge");
        int id = 0;
        INSTANCE.messageBuilder(PresetCreatePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetCreatePacket::new)
                .encoder(PresetCreatePacket::write)
                .consumerMainThread(PresetCreatePacket::handle)
                .add();
        INSTANCE.messageBuilder(PresetEditPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetEditPacket::new)
                .encoder(PresetEditPacket::write)
                .consumerMainThread(PresetEditPacket::handle)
                .add();
        INSTANCE.messageBuilder(PresetDeletePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PresetDeletePacket::new)
                .encoder(PresetDeletePacket::write)
                .consumerMainThread(PresetDeletePacket::handle)
                .add();
        INSTANCE.messageBuilder(SpawnMobPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpawnMobPacket::new)
                .encoder(SpawnMobPacket::write)
                .consumerMainThread(SpawnMobPacket::handle)
                .add();
        INSTANCE.messageBuilder(PresetSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PresetSyncPacket::new)
                .encoder(PresetSyncPacket::write)
                .consumerMainThread(PresetSyncPacket::handle)
                .add();
        INSTANCE.messageBuilder(ServerCheckPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerCheckPacket::new)
                .encoder(ServerCheckPacket::write)
                .consumerMainThread(ServerCheckPacket::handle)
                .add();
        INSTANCE.messageBuilder(RequestPresetsPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestPresetsPacket::new)
                .encoder(RequestPresetsPacket::write)
                .consumerMainThread(RequestPresetsPacket::handle)
                .add();
        INSTANCE.messageBuilder(ServerCheckResponsePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ServerCheckResponsePacket::new)
                .encoder(ServerCheckResponsePacket::write)
                .consumerMainThread(ServerCheckResponsePacket::handle)
                .add();
        INSTANCE.messageBuilder(OpenGuiPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenGuiPacket::new)
                .encoder(OpenGuiPacket::write)
                .consumerMainThread(OpenGuiPacket::handle)
                .add();
        INSTANCE.messageBuilder(ValidateResourcesPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ValidateResourcesPacket::new)
                .encoder(ValidateResourcesPacket::write)
                .consumerMainThread(ValidateResourcesPacket::handle)
                .add();
        INSTANCE.messageBuilder(ResourceListRequestPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ResourceListRequestPacket::new)
                .encoder(ResourceListRequestPacket::write)
                .consumerMainThread(ResourceListRequestPacket::handle)
                .add();
        INSTANCE.messageBuilder(ResourceListResponsePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceListResponsePacket::new)
                .encoder(ResourceListResponsePacket::write)
                .consumerMainThread(ResourceListResponsePacket::handle)
                .add();
        INSTANCE.messageBuilder(ResourceValidationResponsePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceValidationResponsePacket::new)
                .encoder(ResourceValidationResponsePacket::write)
                .consumerMainThread(ResourceValidationResponsePacket::handle)
                .add();
        LOGGER.info("Finished registering {} network packets", id);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        if (player != null) {
            INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}