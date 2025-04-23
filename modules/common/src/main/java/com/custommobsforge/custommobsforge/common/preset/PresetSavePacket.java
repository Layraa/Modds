package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;

public class PresetSavePacket {
    private final Preset preset;
    private final boolean isEdit; // Новый флаг

    public PresetSavePacket(Preset preset, boolean isEdit) {
        this.preset = preset;
        this.isEdit = isEdit;
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
        buf.writeUtf(msg.preset.getCreator() != null ? msg.preset.getCreator() : "");
        buf.writeBoolean(msg.isEdit); // Сохраняем флаг
    }

    public static PresetSavePacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        String model = buf.readUtf();
        String animation = buf.readUtf();
        String texture = buf.readUtf();
        String behavior = buf.readUtf();
        int hp = buf.readInt();
        float speed = buf.readFloat();
        float size = buf.readFloat();
        String creator = buf.readUtf();
        boolean isEdit = buf.readBoolean(); // Читаем флаг
        Preset preset = new Preset(name, model, animation, texture, behavior, hp, speed, size, creator.isEmpty() ? null : creator);
        return new PresetSavePacket(preset, isEdit);
    }

    public Preset getPreset() {
        return preset;
    }

    public boolean isEdit() {
        return isEdit;
    }
}