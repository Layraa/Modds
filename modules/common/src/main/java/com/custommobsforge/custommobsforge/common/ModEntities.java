package com.custommobsforge.custommobsforge.common;

import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CustomMobsForge.MOD_ID);

    public static final RegistryObject<EntityType<CustomMob>> CUSTOM_MOB = ENTITIES.register("custom_mob",
            () -> EntityType.Builder.create(CustomMob::new, SpawnGroup.CREATURE)
                    .setDimensions(0.6f, 1.8f)
                    .build(new Identifier(CustomMobsForge.MOD_ID, "custom_mob").toString())
    );

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}