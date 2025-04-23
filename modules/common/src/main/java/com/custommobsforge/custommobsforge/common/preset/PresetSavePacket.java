package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PresetSavePacket {
    private final Preset preset;

    public PresetSavePacket(Preset preset) {
        this.preset = preset;
    }

    public static void encode(PresetSavePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.preset.getName());
        buf.writeUtf(msg.preset.getModel());
        buf.writeUtf(msg.preset.getAnimation());
        buf.writeUtf(msg.preset.getTexture());
        buf.writeUtf(msg.preset.getBehavior());
        buf.writeInt(msg.preset.getHp());
        buf.writeFloat(msg.preset.getSpeed());
        buf.writeFloat(msg.preset.getSize());
    }

    public static PresetSavePacket decode(FriendlyByteBuf buf) {
        return new PresetSavePacket(new Preset(
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt(),
                buf.readFloat(),
                buf.readFloat()
        ));
    }

    public Preset getPreset() {
        return preset;
    }
}