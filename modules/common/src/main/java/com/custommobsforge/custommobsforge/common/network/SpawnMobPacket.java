package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.ModEntities;
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnMobPacket {
    private final String presetName;

    public SpawnMobPacket(String presetName) {
        this.presetName = presetName;
    }

    public SpawnMobPacket(FriendlyByteBuf buf) {
        this.presetName = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(presetName);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                Level level = player.level();
                var preset = PresetManager.getInstance().getPreset(presetName);
                if (preset != null) {
                    CustomMob mob = new CustomMob(ModEntities.CUSTOM_MOB.get(), level);
                    mob.setModelName(preset.modelName());
                    mob.setTextureName(preset.textureName());
                    mob.setAnimationName(preset.animationName());
                    mob.setHealthValue(preset.health());
                    mob.setSpeedValue(preset.speed());
                    mob.setPos(player.getX(), player.getY(), player.getZ());
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.COMMAND, null, null);
                    level.addFreshEntity(mob);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}