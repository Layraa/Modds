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
}