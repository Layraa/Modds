package com.custommobsforge.custommobsforge.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResourceValidationResponsePacket {
    private final boolean valid;
    private final boolean isCreateMode;
    private final String name;
    private final float health;
    private final double speed;
    private final String model;
    private final String texture;
    private final String animation;

    public ResourceValidationResponsePacket(boolean valid, boolean isCreateMode, String name, float health, double speed, String model, String texture, String animation) {
        this.valid = valid;
        this.isCreateMode = isCreateMode;
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.model = model;
        this.texture = texture;
        this.animation = animation;
    }

    public ResourceValidationResponsePacket(FriendlyByteBuf buf) {
        this.valid = buf.readBoolean();
        this.isCreateMode = buf.readBoolean();
        this.name = buf.readUtf();
        this.health = buf.readFloat();
        this.speed = buf.readDouble();
        this.model = buf.readUtf();
        this.texture = buf.readUtf();
        this.animation = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(valid);
        buf.writeBoolean(isCreateMode);
        buf.writeUtf(name);
        buf.writeFloat(health);
        buf.writeDouble(speed);
        buf.writeUtf(model);
        buf.writeUtf(texture);
        buf.writeUtf(animation);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.post(new ResourceValidationResponseEvent(this));
        });
        context.get().setPacketHandled(true);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isCreateMode() {
        return isCreateMode;
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
}