package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class PresetPacket {
    public enum Operation {
        FULL_UPDATE,
        ADD,
        UPDATE,
        DELETE
    }

    private final Operation operation;
    private final List<Preset> presets;
    private final List<String> presetNames;

    public PresetPacket(Operation operation, List<Preset> presets, List<String> presetNames) {
        this.operation = operation;
        this.presets = new ArrayList<>(presets);
        this.presetNames = new ArrayList<>(presetNames);
    }

    public Operation getOperation() {
        return operation;
    }

    public List<Preset> getPresets() {
        return presets;
    }

    public List<String> getPresetNames() {
        return presetNames;
    }

    public static void encode(PresetPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.operation);
        if (msg.operation == Operation.DELETE) {
            buf.writeInt(msg.presetNames.size());
            for (String name : msg.presetNames) {
                buf.writeUtf(name);
            }
        } else {
            buf.writeInt(msg.presets.size());
            for (Preset preset : msg.presets) {
                preset.writeToBuf(buf);
            }
        }
    }

    public static PresetPacket decode(FriendlyByteBuf buf) {
        Operation operation = buf.readEnum(Operation.class);
        if (operation == Operation.DELETE) {
            int size = buf.readInt();
            List<String> presetNames = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                presetNames.add(buf.readUtf());
            }
            return new PresetPacket(operation, List.of(), presetNames);
        } else {
            int size = buf.readInt();
            List<Preset> presets = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                presets.add(Preset.readFromBuf(buf));
            }
            return new PresetPacket(operation, presets, List.of());
        }
    }
}