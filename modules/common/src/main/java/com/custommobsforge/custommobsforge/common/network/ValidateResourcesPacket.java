package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class ValidateResourcesPacket {
    private final String model;
    private final String texture;
    private final String animation;
    private final boolean isCreateMode;
    private final String name;
    private final float health;
    private final double speed;

    public ValidateResourcesPacket(String model, String texture, String animation, boolean isCreateMode, String name, float health, double speed) {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.isCreateMode = isCreateMode;
        this.name = name;
        this.health = health;
        this.speed = speed;
    }

    public ValidateResourcesPacket(FriendlyByteBuf buf) {
        this.model = buf.readUtf();
        this.texture = buf.readUtf();
        this.animation = buf.readUtf();
        this.isCreateMode = buf.readBoolean();
        this.name = buf.readUtf();
        this.health = buf.readFloat();
        this.speed = buf.readDouble();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(model);
        buf.writeUtf(texture);
        buf.writeUtf(animation);
        buf.writeBoolean(isCreateMode);
        buf.writeUtf(name);
        buf.writeFloat(health);
        buf.writeDouble(speed);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                boolean valid = validateResources();
                NetworkHandler.sendToPlayer(new ResourceValidationResponsePacket(valid, isCreateMode, name, health, speed, model, texture, animation), player);
            }
        });
        context.get().setPacketHandled(true);
    }

    private boolean validateResources() {
        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return false;

            ResourceManager resourceManager = server.getResourceManager();

            // Проверяем наличие модели
            ResourceLocation modelLocation = new ResourceLocation("custommobsforge", "geo/" + model + ".json");
            boolean modelExists = resourceManager.getResource(modelLocation).isPresent();

            // Проверяем наличие текстуры
            ResourceLocation textureLocation = new ResourceLocation("custommobsforge", "textures/entity/" + texture + ".png");
            boolean textureExists = resourceManager.getResource(textureLocation).isPresent();

            // Проверяем наличие анимации
            ResourceLocation animationLocation = new ResourceLocation("custommobsforge", "animations/" + animation + ".json");
            boolean animationExists = resourceManager.getResource(animationLocation).isPresent();

            return modelExists && textureExists && animationExists;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}