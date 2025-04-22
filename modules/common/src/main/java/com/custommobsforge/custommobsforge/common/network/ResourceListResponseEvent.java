package com.custommobsforge.custommobsforge.common.network;

import net.minecraftforge.eventbus.api.Event;

public class ResourceListResponseEvent extends Event {
    private final ResourceListResponsePacket packet;

    public ResourceListResponseEvent(ResourceListResponsePacket packet) {
        this.packet = packet;
    }

    public ResourceListResponsePacket getPacket() {
        return packet;
    }
}