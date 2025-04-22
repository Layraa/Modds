package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PresetDeletePacket {
    private final String name;

    public PresetDeletePacket(String name) {
        this.name = name;
    }

    public PresetDeletePacket(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public String getName() {
        return name;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
    }
}