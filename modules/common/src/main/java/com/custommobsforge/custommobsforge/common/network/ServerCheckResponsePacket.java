package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.event.ServerSupportCheckEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerCheckResponsePacket {
    private final boolean supported;

    public ServerCheckResponsePacket(boolean supported) {
        this.supported = supported;
    }

    public ServerCheckResponsePacket(FriendlyByteBuf buf) {
        this.supported = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(supported);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> MinecraftForge.EVENT_BUS.post(new ServerSupportCheckEvent(supported)));
        context.get().setPacketHandled(true);
    }
}