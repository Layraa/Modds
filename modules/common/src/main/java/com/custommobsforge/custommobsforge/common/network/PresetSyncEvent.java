package com.custommobsforge.custommobsforge.common.network;

import net.minecraftforge.eventbus.api.Event;

public class PresetSyncEvent extends Event {
    private final PresetSyncPacket packet;

    public PresetSyncEvent(PresetSyncPacket packet) {
        this.packet = packet;
    }

    public PresetSyncPacket getPacket() {
        return packet;
    }
}