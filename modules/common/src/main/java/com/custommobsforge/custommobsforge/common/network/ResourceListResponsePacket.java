package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ResourceListResponsePacket {
    private final String type;
    private final List<String> resources;

    public ResourceListResponsePacket(String type, List<String> resources) {
        this.type = type;
        this.resources = new ArrayList<>(resources);
    }

    public ResourceListResponsePacket(FriendlyByteBuf buf) {
        this.type = buf.readUtf();
        this.resources = buf.readList(FriendlyByteBuf::readUtf);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(type);
        buf.writeCollection(resources, FriendlyByteBuf::writeUtf);
    }

    public String getType() {
        return type;
    }

    public List<String> getResources() {
        return resources;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        // Логика обработки перенесена в ClientPacketHandler
    }
}