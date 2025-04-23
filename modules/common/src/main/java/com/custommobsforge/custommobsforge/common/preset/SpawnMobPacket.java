package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnMobPacket {
    private final String presetName;
    private final Vec3 position;

    public SpawnMobPacket(String presetName, Vec3 position) {
        this.presetName = presetName;
        this.position = position;
    }

    public static void encode(SpawnMobPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.presetName);
        buf.writeDouble(msg.position.x);
        buf.writeDouble(msg.position.y);
        buf.writeDouble(msg.position.z);
    }

    public static SpawnMobPacket decode(FriendlyByteBuf buf) {
        return new SpawnMobPacket(
                buf.readUtf(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        );
    }

    public String getPresetName() {
        return presetName;
    }

    public Vec3 getPosition() {
        return position;
    }
}