package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ServerMobHandler {
    public static void spawnCustomMob(ServerLevel level, Preset preset, double x, double y, double z) {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            throw new IllegalStateException("ServerMobHandler can only be used on the server side!");
        }
        CustomMob mob = new CustomMob(CustomMobsForge.registerCustomMob().get(), level, preset);
        mob.setPos(x, y, z);
        mob.setCustomName(net.minecraft.network.chat.Component.literal(preset.getName()));
        mob.setScale(preset.getSize());
        level.addFreshEntity(mob);
    }
}