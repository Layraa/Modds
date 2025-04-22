package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.CustomMobsForge;
import com.custommobsforge.custommobsforge.client.render.CustomMobRenderer;
import com.custommobsforge.custommobsforge.common.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CustomMobsForge.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomMobsForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientNetworkHandler.register();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CUSTOM_MOB.get(), CustomMobRenderer::new);
    }
}