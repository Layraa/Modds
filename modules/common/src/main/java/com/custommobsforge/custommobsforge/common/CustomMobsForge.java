package com.custommobsforge.custommobsforge.common;

import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.RequestPresetsPacket;
import com.custommobsforge.custommobsforge.common.preset.SpawnMobPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
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
            new net.minecraft.resources.ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    private static final RegistryObject<EntityType<CustomMob>> CUSTOM_MOB = ENTITIES.register("custom_mob", () ->
            EntityType.Builder.<CustomMob>of((EntityType<CustomMob> type, Level level) -> new CustomMob(type, level), MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build("custom_mob")
    );

    public CustomMobsForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(modEventBus);
        registerPackets();
        LOGGER.info("CustomMobsForge (Common) initialized");
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
        CHANNEL.messageBuilder(SpawnMobPacket.class, id++)
                .encoder(SpawnMobPacket::encode)
                .decoder(SpawnMobPacket::decode)
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