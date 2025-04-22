package com.custommobsforge.custommobsforge.client.clien;

import com.custommobsforge.custommobsforge.common.network.*;
import net.minecraftforge.network.NetworkDirection;

public class ClientNetworkHandler {
    public static void register() {
        // Используем канал из NetworkHandler вместо создания нового
        NetworkHandler.CHANNEL.messageBuilder(OpenGuiPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenGuiPacket::new)
                .encoder(OpenGuiPacket::write)
                .consumerMainThread(ClientPacketHandler::handleOpenGui)
                .add();

        NetworkHandler.CHANNEL.messageBuilder(PresetSyncPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PresetSyncPacket::new)
                .encoder(PresetSyncPacket::write)
                .consumerMainThread(ClientPacketHandler::handlePresetSync)
                .add();

        NetworkHandler.CHANNEL.messageBuilder(PresetDeleteSyncPacket.class, 7, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PresetDeleteSyncPacket::new)
                .encoder(PresetDeleteSyncPacket::write)
                .consumerMainThread(ClientPacketHandler::handlePresetDeleteSync)
                .add();

        NetworkHandler.CHANNEL.messageBuilder(ResourceListResponsePacket.class, 9, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceListResponsePacket::new)
                .encoder(ResourceListResponsePacket::write)
                .consumerMainThread(ClientPacketHandler::handleResourceListResponse)
                .add();

        NetworkHandler.CHANNEL.messageBuilder(ResourceValidationResponsePacket.class, 11, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceValidationResponsePacket::new)
                .encoder(ResourceValidationResponsePacket::write)
                .consumerMainThread(ClientPacketHandler::handleResourceValidationResponse)
                .add();
    }

    public static void sendToServer(Object packet) {
        NetworkHandler.CHANNEL.sendToServer(packet);
    }
}