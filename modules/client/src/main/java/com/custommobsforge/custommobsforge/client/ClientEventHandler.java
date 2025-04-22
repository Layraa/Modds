package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.client.gui.MainMenuScreen;
import com.custommobsforge.custommobsforge.common.event.OpenGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "custommobsforge", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public ClientEventHandler() {
        LOGGER.info("ClientEventHandler: Registering for events");
    }

    @SubscribeEvent
    public void onOpenGui(OpenGuiEvent event) {
        LOGGER.info("ClientEventHandler: Received OpenGuiEvent on client");
        LOGGER.info("ClientEventHandler: Opening MainMenuScreen");
        Minecraft.getInstance().setScreen(new MainMenuScreen());
    }
}