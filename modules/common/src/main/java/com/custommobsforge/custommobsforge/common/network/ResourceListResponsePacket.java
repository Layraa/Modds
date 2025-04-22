package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ResourceListResponsePacket {
    private final String resourceType;
    private final List<String> resources;

    public ResourceListResponsePacket(String resourceType, List<String> resources) {
        this.resourceType = resourceType;
        this.resources = resources;
    }

    public ResourceListResponsePacket(FriendlyByteBuf buf) {
        this.resourceType = buf.readUtf();
        int size = buf.readInt();
        this.resources = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            resources.add(buf.readUtf());
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(resourceType);
        buf.writeInt(resources.size());
        for (String resource : resources) {
            buf.writeUtf(resource);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.post(new ResourceListResponseEvent(this));
        });
        context.get().setPacketHandled(true);
    }

    public String getResourceType() {
        return resourceType;
    }

    public List<String> getResources() {
        return resources;
    }
}