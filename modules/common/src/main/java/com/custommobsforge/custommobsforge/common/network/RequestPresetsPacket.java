package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestPresetsPacket {
    public RequestPresetsPacket() {
    }

    @SuppressWarnings("unused")
    public RequestPresetsPacket(FriendlyByteBuf buf) {
    }

    @SuppressWarnings("unused")
    public void write(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        // Логика обработки уже реализована в NetworkHandler
        context.get().setPacketHandled(true);
    }
}