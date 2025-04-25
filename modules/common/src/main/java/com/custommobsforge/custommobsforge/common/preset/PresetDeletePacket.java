package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;

public class PresetDeletePacket {
    private final String presetName;

    public PresetDeletePacket(String presetName) {
        this.presetName = presetName;
    }

    public static void encode(PresetDeletePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.presetName);
    }

    public static PresetDeletePacket decode(FriendlyByteBuf buf) {
        return new PresetDeletePacket(buf.readUtf());
    }

    public String getPresetName() {
        return presetName;
    }
}