package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ServerMobHandler {
    public static void spawnCustomMob(ServerLevel level, Preset preset, double x, double y, double z) {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            throw new IllegalStateException("ServerMobHandler can only be used on the server side!");
        }
        CustomMobsForge.LOGGER.info("Creating CustomMob with preset: " + preset.getName());
        EntityType<CustomMob> entityType = CustomMobsForge.registerCustomMob().get();
        CustomMobsForge.LOGGER.info("EntityType from registerCustomMob: " + entityType);
        CustomMobsForge.LOGGER.info("ServerLevel: " + level);

        CustomMob mob;
        try {
            // Создаём сущность через EntityType, который использует конструктор (EntityType, Level)
            mob = entityType.create(level);
            if (mob == null) {
                throw new IllegalStateException("Failed to create CustomMob instance");
            }
            // Устанавливаем пресет вручную через сеттер или другой метод
            mob.setCustomPreset(preset); // Требуется добавить метод setCustomPreset в CustomMob
            CustomMobsForge.LOGGER.info("CustomMob created successfully: " + mob);
        } catch (Exception e) {
            CustomMobsForge.LOGGER.error("Failed to create CustomMob for preset: " + preset.getName(), e);
            return;
        }

        CustomMobsForge.LOGGER.info("Setting position for mob: (" + x + ", " + y + ", " + z + ")");
        mob.setPos(x, y, z);
        CustomMobsForge.LOGGER.info("Setting custom name: " + preset.getName());
        mob.setCustomName(Component.literal(preset.getName()));
        CustomMobsForge.LOGGER.info("Setting scale: " + preset.getSize());
        mob.setScale(preset.getSize());
        CustomMobsForge.LOGGER.info("Adding mob to world...");
        boolean added = level.addFreshEntity(mob);
        if (added) {
            CustomMobsForge.LOGGER.info("Successfully spawned mob: " + preset.getName() + " at (" + x + ", " + y + ", " + z + ")");
        } else {
            CustomMobsForge.LOGGER.error("Failed to spawn mob: " + preset.getName() + " at (" + x + ", " + y + ", " + z + ")");
        }
    }
}