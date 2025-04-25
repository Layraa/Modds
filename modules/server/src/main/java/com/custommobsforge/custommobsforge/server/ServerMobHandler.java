package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;

public class ServerMobHandler {
    public static void spawnCustomMob(ServerLevel level, Preset preset, double x, double y, double z) {
        EntityType<CustomMob> entityType = CustomMobsForge.registerCustomMob().get();
        CustomMob mob = entityType.create(level);
        if (mob == null) {
            CustomMobsForge.LOGGER.error("Failed to create CustomMob for preset: {}", preset.getName());
            return;
        }

        mob.setCustomPreset(preset);
        mob.setPos(x, y, z);
        mob.setCustomName(Component.literal(preset.getName()));
        mob.setScale(preset.getSize());

        // Принудительно синхронизируем PRESET_NAME
        mob.getEntityData().set(CustomMob.PRESET_NAME_ACCESSOR, preset.getName());
        CustomMobsForge.LOGGER.debug("Set PRESET_NAME to {} for mob {}", preset.getName(), mob.getId());

        if (level.addFreshEntity(mob)) {
            CustomMobsForge.LOGGER.info("Spawned mob: {} at ({}, {}, {})", preset.getName(), x, y, z);
        } else {
            CustomMobsForge.LOGGER.error("Failed to spawn mob: {} at ({}, {}, {})", preset.getName(), x, y, z);
        }
    }
}