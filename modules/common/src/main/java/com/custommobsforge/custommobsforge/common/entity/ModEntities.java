package com.custommobsforge.custommobsforge.common.entity;

import com.custommobsforge.custommobsforge.CustomMobsForge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CustomMobsForge.MODID);

    public static final RegistryObject<EntityType<CustomMob>> CUSTOM_MOB = ENTITIES.register("custom_mob",
            () -> EntityType.Builder.of(CustomMob::new, MobCategory.CREATURE)
                    .build("custom_mob"));
}