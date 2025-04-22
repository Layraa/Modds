package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ResourceListRequestPacket {
    private final String resourceType;

    public ResourceListRequestPacket(String resourceType) {
        this.resourceType = resourceType;
    }

    public ResourceListRequestPacket(FriendlyByteBuf buf) {
        this.resourceType = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(resourceType);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                List<String> resources = getResourceList();
                NetworkHandler.sendToPlayer(new ResourceListResponsePacket(resourceType, resources), player);
            }
        });
        context.get().setPacketHandled(true);
    }

    private List<String> getResourceList() {
        List<String> resources = new ArrayList<>();
        String path;
        String extension;
        switch (resourceType) {
            case "model":
                path = "geo";
                extension = ".json";
                break;
            case "texture":
                path = "textures/entity";
                extension = ".png";
                break;
            case "animation":
                path = "animations";
                extension = ".json";
                break;
            default:
                return resources;
        }

        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return resources;

            ResourceManager resourceManager = server.getResourceManager();
            Set<ResourceLocation> resourceLocations = resourceManager.listResources(path, (location) -> location.getPath().endsWith(extension)).keySet();

            resources.addAll(resourceLocations.stream()
                    .filter(location -> location.getNamespace().equals("custommobsforge"))
                    .map(location -> {
                        String fileName = location.getPath();
                        return fileName.substring(path.length() + 1, fileName.length() - extension.length());
                    })
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resources;
    }
}