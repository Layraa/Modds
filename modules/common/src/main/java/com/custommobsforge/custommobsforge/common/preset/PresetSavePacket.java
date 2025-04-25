package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;

public class PresetSavePacket {
    private final Preset preset;
    private final boolean isEdit;

    public PresetSavePacket(Preset preset, boolean isEdit) {
        this.preset = preset;
        this.isEdit = isEdit;
    }

    public static void encode(PresetSavePacket msg, FriendlyByteBuf buf) {
        msg.preset.writeToBuf(buf);
        buf.writeBoolean(msg.isEdit);
    }

    public static PresetSavePacket decode(FriendlyByteBuf buf) {
        Preset preset = Preset.readFromBuf(buf);
        boolean isEdit = buf.readBoolean();
        return new PresetSavePacket(preset, isEdit);
    }

    public Preset getPreset() {
        return preset;
    }

    public boolean isEdit() {
        return isEdit;
    }
}