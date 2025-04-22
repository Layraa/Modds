package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResourceListRequestPacket {
    private final String type;

    public ResourceListRequestPacket(String type) {
        this.type = type;
    }

    public ResourceListRequestPacket(FriendlyByteBuf buf) {
        this.type = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(type);
    }

    public String getType() {
        return type;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
    }
}