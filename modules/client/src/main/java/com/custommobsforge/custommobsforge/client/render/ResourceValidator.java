package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ResourceValidator {
    public static boolean validateResources(String model, String animation, String texture) {
        try {
            ResourceLocation modelLocation = new ResourceLocation("custommobsforge", "geo/" + model + ".geo.json");
            ResourceLocation animationLocation = new ResourceLocation("custommobsforge", "animations/" + animation + ".animation.json");
            ResourceLocation textureLocation = new ResourceLocation("custommobsforge", "textures/entity/" + texture + ".png");

            CustomMobsForge.LOGGER.info("Checking model: " + modelLocation);
            Minecraft.getInstance().getResourceManager().getResource(modelLocation).orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelLocation));
            CustomMobsForge.LOGGER.info("Checking animation: " + animationLocation);
            Minecraft.getInstance().getResourceManager().getResource(animationLocation).orElseThrow(() -> new IllegalArgumentException("Animation not found: " + animationLocation));
            CustomMobsForge.LOGGER.info("Checking texture: " + textureLocation);
            Minecraft.getInstance().getResourceManager().getResource(textureLocation).orElseThrow(() -> new IllegalArgumentException("Texture not found: " + textureLocation));
            CustomMobsForge.LOGGER.info("All resources validated successfully");
            return true;
        } catch (Exception e) {
            CustomMobsForge.LOGGER.error("Resource validation failed: " + e.getMessage());
            return false;
        }
    }
}