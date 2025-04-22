package com.custommobsforge.custommobsforge.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResourceLoader {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        verifyResources();
    }

    private static void verifyResources() {
        // Проверяем наличие тестовых ресурсов в assets/custommobsforge
        String[] resourceTypes = {"geo/test_model.json", "textures/test_texture.png", "animations/test_animation.json"};
        for (String resourcePath : resourceTypes) {
            ResourceLocation location = new ResourceLocation("custommobsforge", resourcePath);
            try (InputStream inputStream = ResourceLoader.class.getResourceAsStream("/assets/custommobsforge/" + resourcePath)) {
                if (inputStream == null) {
                    LOGGER.error("Resource not found in mod assets: {}", resourcePath);
                } else {
                    LOGGER.info("Resource found: {}", resourcePath);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to verify resource {}: {}", resourcePath, e.getMessage());
            }
        }
    }
}