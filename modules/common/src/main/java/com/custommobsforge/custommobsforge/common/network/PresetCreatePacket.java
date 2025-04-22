package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PresetCreatePacket {
    private final String name;
    private final float health;
    private final double speed;
    private final float sizeWidth;
    private final float sizeHeight;
    private final String modelName;
    private final String textureName;
    private final String animationName;

    public PresetCreatePacket(String name, float health, double speed, float sizeWidth, float sizeHeight, String modelName, String textureName, String animationName) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.sizeWidth = sizeWidth;
        this.sizeHeight = sizeHeight;
        this.modelName = modelName;
        this.textureName = textureName;
        this.animationName = animationName;
    }

    public PresetCreatePacket(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
        this.health = buf.readFloat();
        this.speed = buf.readDouble();
        this.sizeWidth = buf.readFloat();
        this.sizeHeight = buf.readFloat();
        this.modelName = buf.readUtf();
        this.textureName = buf.readUtf();
        this.animationName = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeFloat(health);
        buf.writeDouble(speed);
        buf.writeFloat(sizeWidth);
        buf.writeFloat(sizeHeight);
        buf.writeUtf(modelName);
        buf.writeUtf(textureName);
        buf.writeUtf(animationName);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
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

    public float getSizeWidth() {
        return sizeWidth;
    }

    public float getSizeHeight() {
        return sizeHeight;
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
}