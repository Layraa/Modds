package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;

public class RequestPresetsPacket {
    public RequestPresetsPacket() {}

    public static void encode(RequestPresetsPacket msg, FriendlyByteBuf buf) {
        // Пакет пустой, ничего не передаём
    }

    public static RequestPresetsPacket decode(FriendlyByteBuf buf) {
        return new RequestPresetsPacket();
    }
}