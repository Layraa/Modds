package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerCheckPacket {
    public ServerCheckPacket() {}

    public ServerCheckPacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                NetworkHandler.sendToPlayer(new ServerCheckResponsePacket(true), player);
            }
        });
        context.get().setPacketHandled(true);
    }
}