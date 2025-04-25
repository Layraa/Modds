package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientPresetHandler {
    private static final Map<String, Preset> presets = new HashMap<>();

    public static void applyPresetUpdate(PresetPacket packet) {
        CustomMobsForge.LOGGER.info("Received PresetPacket with operation: {}, presets: {}, presetNames: {}",
                packet.getOperation(), packet.getPresets().size(), packet.getPresetNames().size());
        switch (packet.getOperation()) {
            case FULL_UPDATE:
                presets.clear();
                for (Preset preset : packet.getPresets()) {
                    presets.put(preset.getName(), preset);
                }
                break;
            case ADD:
                for (Preset preset : packet.getPresets()) {
                    presets.put(preset.getName(), preset);
                }
                break;
            case UPDATE:
                for (Preset preset : packet.getPresets()) {
                    presets.put(preset.getName(), preset);
                }
                break;
            case DELETE:
                for (String presetName : packet.getPresetNames()) {
                    presets.remove(presetName);
                }
                break;
        }
    }

    public static void clear() {
        presets.clear();
        CustomMobsForge.LOGGER.debug("Cleared ClientPresetHandler presets");
    }

    public static List<Preset> getPresets() {
        return new ArrayList<>(presets.values());
    }

    public static Map<String, Preset> getPresetsMap() {
        return presets;
    }

    public static Preset getPresetByName(String presetName) {
        return presets.get(presetName);
    }

    public static void removePreset(String presetName) {
        presets.remove(presetName);
    }
}