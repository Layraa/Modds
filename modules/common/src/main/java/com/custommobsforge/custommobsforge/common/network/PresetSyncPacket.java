package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PresetSyncPacket {
    private final String name;
    private final float health;
    private final double speed;
    private final String modelName;
    private final String textureName;
    private final String animationName;

    public PresetSyncPacket(String name, float health, double speed, String modelName, String textureName, String animationName) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.modelName = modelName;
        this.textureName = textureName;
        this.animationName = animationName;
    }

    public PresetSyncPacket(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
        this.health = buf.readFloat();
        this.speed = buf.readDouble();
        this.modelName = buf.readUtf();
        this.textureName = buf.readUtf();
        this.animationName = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeFloat(health);
        buf.writeDouble(speed);
        buf.writeUtf(modelName);
        buf.writeUtf(textureName);
        buf.writeUtf(animationName);
    }

    public String getName() {
        return name;
    }

    public float getHealth() {
        return health;
    }

    public double getSpeed() {
        return speed;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTextureName() {
        return textureName;
    }

    public String getAnimationName() {
        return animationName;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        // Логика обработки перенесена в ClientPacketHandler
    }
}