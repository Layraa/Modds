package com.custommobsforge.custommobsforge.common.network;

import net.minecraftforge.eventbus.api.Event;

public class ResourceValidationResponseEvent extends Event {
    private final ResourceValidationResponsePacket packet;

    public ResourceValidationResponseEvent(ResourceValidationResponsePacket packet) {
        this.packet = packet;
    }

    public ResourceValidationResponsePacket getPacket() {
        return packet;
    }
}