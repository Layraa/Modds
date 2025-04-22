package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnMobPacket {
    private final String presetName;

    public SpawnMobPacket(String presetName) {
        this.presetName = presetName;
    }

    public SpawnMobPacket(FriendlyByteBuf buf) {
        this.presetName = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(presetName);
    }

    public String getPresetName() {
        return presetName;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
    }
}