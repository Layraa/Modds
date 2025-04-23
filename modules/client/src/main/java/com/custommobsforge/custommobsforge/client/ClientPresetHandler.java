package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.common.preset.Preset;
import java.util.ArrayList;
import java.util.List;

public class ClientPresetHandler {
    private static List<Preset> presets = new ArrayList<>();

    public static void setPresets(List<Preset> newPresets) {
        presets.clear();
        presets.addAll(newPresets);
    }

    public static List<Preset> getPresets() {
        return new ArrayList<>(presets);
    }

    public static void removePreset(String presetName) {
        presets.removeIf(preset -> preset.getName().equals(presetName));
    }

    public static void addPreset(Preset preset) {
        presets.add(preset);
    }
}