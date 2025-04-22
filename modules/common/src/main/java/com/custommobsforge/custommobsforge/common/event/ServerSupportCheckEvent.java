package com.custommobsforge.custommobsforge.common.event;

import net.minecraftforge.eventbus.api.Event;

public class ServerSupportCheckEvent extends Event {
    private final boolean supported;

    public ServerSupportCheckEvent(boolean supported) {
        this.supported = supported;
    }

    public boolean isSupported() {
        return supported;
    }
}