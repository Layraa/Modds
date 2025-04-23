package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.client.ClientCustomMobsForge;
import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ClientCustomMobsForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        RegistryObject<?> customMobRegistry = CustomMobsForge.registerCustomMob();
        if (customMobRegistry.isPresent()) {
            event.registerEntityRenderer(CustomMobsForge.registerCustomMob().get(), CustomMobRenderer::new);
        } else {
            throw new IllegalStateException("CustomMob entity is not registered! Ensure the entity is properly registered in CustomMobsForge.");
        }
    }
}