package com.custommobsforge.custommobsforge.common;

import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "custommobsforge");

    public static final RegistryObject<EntityType<CustomMob>> CUSTOM_MOB = ENTITIES.register("custom_mob",
            () -> EntityType.Builder.of(CustomMob::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .build("custom_mob"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}