package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestPresetsPacket {
    public RequestPresetsPacket() {
    }

    public RequestPresetsPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerCustomMobsForge server = new ServerCustomMobsForge();
            server.onRequestPresets(context.get().getSender());
        });
        context.get().setPacketHandled(true);
    }
}