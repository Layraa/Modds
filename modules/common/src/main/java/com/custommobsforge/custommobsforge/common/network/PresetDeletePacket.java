package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.PresetManager;
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

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PresetManager.getInstance().deletePreset(name);
        });
        ctx.get().setPacketHandled(true);
    }
}