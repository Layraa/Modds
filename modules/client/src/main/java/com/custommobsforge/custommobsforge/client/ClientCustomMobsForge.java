package com.custommobsforge.custommobsforge;

import com.custommobsforge.custommobsforge.client.ClientEventHandler;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.event.ServerSupportCheckEvent;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.ServerCheckPacket;
import com.custommobsforge.custommobsforge.client.render.CustomMobRendererAdapter;
import com.custommobsforge.custommobsforge.common.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ClientCustomMobsForge.MOD_ID)
@Mod.EventBusSubscriber(modid = ClientCustomMobsForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientCustomMobsForge {
    public static final String MOD_ID = "custommobsforge";
    private static boolean serverSupportsMod = false;
    private static final Logger LOGGER = LogManager.getLogger();

    public ClientCustomMobsForge() {
        LOGGER.info("ClientCustomMobsForge: Initializing");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        NetworkHandler.register();
        modEventBus.addListener(this::registerRenderers);

        // Инициализируем ClientEventHandler
        new ClientEventHandler();

        // В singleplayer-режиме устанавливаем serverSupportsMod = true
        if (Minecraft.getInstance().isSingleplayer()) {
            LOGGER.info("Singleplayer mode detected, setting serverSupportsMod = true");
            serverSupportsMod = true;
        } else {
            LOGGER.info("Not in singleplayer mode, sending ServerCheckPacket");
            NetworkHandler.sendToServer(new ServerCheckPacket());
        }
    }

    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LOGGER.info("ClientCustomMobsForge: Registering entity renderers");
        event.registerEntityRenderer(ModEntities.CUSTOM_MOB.get(), CustomMobRendererAdapter::new);
    }

    @SubscribeEvent
    public void onServerSupportCheck(ServerSupportCheckEvent event) {
        LOGGER.info("Server support check: supported = {}", event.isSupported());
        serverSupportsMod = event.isSupported();
    }

    @SuppressWarnings("unused")
    public static void setServerSupportsMod(boolean supports) {
        serverSupportsMod = supports;
    }
}