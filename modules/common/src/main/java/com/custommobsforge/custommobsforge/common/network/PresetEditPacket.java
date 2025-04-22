package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.PresetManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PresetEditPacket {
    private final String name;
    private final float health;
    private final double speed;
    private final String modelName;
    private final String textureName;
    private final String animationName;

    public PresetEditPacket(String name, float health, double speed, String modelName, String textureName, String animationName) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.modelName = modelName;
        this.textureName = textureName;
        this.animationName = animationName;
    }

    public PresetEditPacket(FriendlyByteBuf buf) {
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

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PresetManager.getInstance().editPreset(name, health, speed, modelName, textureName, animationName);
            // Синхронизация с клиентами
            ServerPlayer sender = context.get().getSender();
            if (sender != null) {
                sender.getServer().getPlayerList().getPlayers().forEach(player -> {
                    NetworkHandler.sendToPlayer(
                            new PresetSyncPacket(name, health, speed, modelName, textureName, animationName),
                            player
                    );
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}