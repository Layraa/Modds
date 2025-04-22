package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.PresetManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PresetDeleteSyncPacket {
    private final String name;

    public PresetDeleteSyncPacket(String name) {
        this.name = name;
    }

    public PresetDeleteSyncPacket(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> PresetManager.getInstance().removePreset(name));
        context.get().setPacketHandled(true);
    }
}