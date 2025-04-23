package com.custommobsforge.custommobsforge.common.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PresetPacket {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<Preset> presets;

    public PresetPacket(List<Preset> presets) {
        this.presets = presets;
    }

    public static void encode(PresetPacket msg, FriendlyByteBuf buf) {
        String json = GSON.toJson(msg.presets);
        buf.writeUtf(json);
    }

    public static PresetPacket decode(FriendlyByteBuf buf) {
        String json = buf.readUtf();
        List<Preset> presets = GSON.fromJson(json, new TypeToken<List<Preset>>(){}.getType());
        if (presets == null) presets = new ArrayList<>();
        return new PresetPacket(presets);
    }

    public List<Preset> getPresets() {
        return presets;
    }
}