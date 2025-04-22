package com.custommobsforge.custommobsforge.client.clien;

import com.custommobsforge.custommobsforge.common.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ClientNetworkHandler {
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
                .consumerMainThread(ClientPacketHandler::handleOpenGui) // Обновлено
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
                .consumerMainThread(ClientPacketHandler::handlePresetSync)
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

        CHANNEL.messageBuilder(PresetDeleteSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PresetDeleteSyncPacket::new)
                .encoder(PresetDeleteSyncPacket::write)
                .consumerMainThread(ClientPacketHandler::handlePresetDeleteSync)
                .add();

        CHANNEL.messageBuilder(ResourceListRequestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ResourceListRequestPacket::new)
                .encoder(ResourceListRequestPacket::write)
                .consumerMainThread(ResourceListRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(ResourceListResponsePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceListResponsePacket::new)
                .encoder(ResourceListResponsePacket::write)
                .consumerMainThread(ClientPacketHandler::handleResourceListResponse)
                .add();

        CHANNEL.messageBuilder(ValidateResourcesPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ValidateResourcesPacket::new)
                .encoder(ValidateResourcesPacket::write)
                .consumerMainThread(ValidateResourcesPacket::handle)
                .add();

        CHANNEL.messageBuilder(ResourceValidationResponsePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceValidationResponsePacket::new)
                .encoder(ResourceValidationResponsePacket::write)
                .consumerMainThread(ClientPacketHandler::handleResourceValidationResponse)
                .add();
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}