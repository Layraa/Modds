package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResourceValidationResponsePacket {
    private final boolean valid;
    private final boolean createMode;
    private final String name;
    private final float health;
    private final double speed;
    private final String model;
    private final String texture;
    private final String animation;

    public ResourceValidationResponsePacket(boolean valid, boolean createMode, String name, float health, double speed, String model, String texture, String animation) {
        this.valid = valid;
        this.createMode = createMode;
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.model = model;
        this.texture = texture;
        this.animation = animation;
    }

    public ResourceValidationResponsePacket(FriendlyByteBuf buf) {
        this.valid = buf.readBoolean();
        this.createMode = buf.readBoolean();
        this.name = buf.readUtf();
        this.health = buf.readFloat();
        this.speed = buf.readDouble();
        this.model = buf.readUtf();
        this.texture = buf.readUtf();
        this.animation = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(valid);
        buf.writeBoolean(createMode);
        buf.writeUtf(name);
        buf.writeFloat(health);
        buf.writeDouble(speed);
        buf.writeUtf(model);
        buf.writeUtf(texture);
        buf.writeUtf(animation);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isCreateMode() {
        return createMode;
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

    public String getModel() {
        return model;
    }

    public String getTexture() {
        return texture;
    }

    public String getAnimation() {
        return animation;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        // Логика обработки перенесена в ClientPacketHandler
    }
}