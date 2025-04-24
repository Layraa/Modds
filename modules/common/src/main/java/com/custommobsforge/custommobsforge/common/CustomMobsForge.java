package com.custommobsforge.custommobsforge.common;

import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.RequestPresetsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomMobsForge.MOD_ID)
public class CustomMobsForge {
    public static final String MOD_ID = "custommobsforge_common";
    private static final String PROTOCOL_VERSION = "1";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    private static final RegistryObject<EntityType<CustomMob>> CUSTOM_MOB = ENTITIES.register("custom_mob", () ->
            EntityType.Builder.<CustomMob>of((type, level) -> new CustomMob(type, level), MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(new ResourceLocation(MOD_ID, "custom_mob").toString())
    );

    public CustomMobsForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(modEventBus);
        registerPackets();
        // Регистрируем обработчик события для создания атрибутов
        modEventBus.addListener(this::registerEntityAttributes);
        LOGGER.info("CustomMobsForge (Common) initialized, version: 1.0.1");
    }

    // Метод для регистрации атрибутов сущности
    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(CUSTOM_MOB.get(), CustomMob.createAttributes().build());
        LOGGER.info("Registered attributes for CustomMob");
    }

    private void registerPackets() {
        int id = 0;
        CHANNEL.messageBuilder(PresetSavePacket.class, id++)
                .encoder(PresetSavePacket::encode)
                .decoder(PresetSavePacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(PresetDeletePacket.class, id++)
                .encoder(PresetDeletePacket::encode)
                .decoder(PresetDeletePacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(PresetPacket.class, id++)
                .encoder(PresetPacket::encode)
                .decoder(PresetPacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(RequestPresetsPacket.class, id++)
                .encoder(RequestPresetsPacket::encode)
                .decoder(RequestPresetsPacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();
    }

    public static DeferredRegister<EntityType<?>> createEntityRegistry() {
        return ENTITIES;
    }

    public static RegistryObject<EntityType<CustomMob>> registerCustomMob() {
        if (CUSTOM_MOB == null) {
            throw new IllegalStateException("CustomMob entity registry object is not initialized!");
        }
        return CUSTOM_MOB;
    }
}