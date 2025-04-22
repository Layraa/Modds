package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.PresetManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestPresetsPacket {
    public RequestPresetsPacket() {
    }

    public RequestPresetsPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                var presets = PresetManager.getInstance().getPresets();
                if (!presets.isEmpty()) {
                    presets.forEach(preset -> NetworkHandler.sendToPlayer(new PresetSyncPacket(
                            preset.getName(),
                            preset.health(),
                            preset.speed(),
                            preset.modelName(),
                            preset.textureName(),
                            preset.animationName()
                    ), player));
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}