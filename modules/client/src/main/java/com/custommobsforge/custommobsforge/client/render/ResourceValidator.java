package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.client.ClientCustomMobsForge;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceValidator {
    private static final int MAX_CACHE_SIZE = 1000;
    private static final Map<String, Boolean> validationCache = new LinkedHashMap<String, Boolean>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public static boolean validateResources(String model, String animation, String texture) {
        String cacheKey = model + "|" + animation + "|" + texture;
        if (validationCache.containsKey(cacheKey)) {
            CustomMobsForge.LOGGER.debug("Using cached validation result for key: {}", cacheKey);
            return validationCache.get(cacheKey);
        }

        try {
            ResourceLocation modelLocation = new ResourceLocation(ClientCustomMobsForge.MOD_ID, "geo/" + model + ".geo.json");
            ResourceLocation animationLocation = new ResourceLocation(ClientCustomMobsForge.MOD_ID, "animations/" + animation + ".animation.json");
            ResourceLocation textureLocation = new ResourceLocation(ClientCustomMobsForge.MOD_ID, "textures/entity/" + texture + ".png");

            CustomMobsForge.LOGGER.debug("Checking model: {}", modelLocation);
            Minecraft.getInstance().getResourceManager().getResource(modelLocation).orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelLocation));
            CustomMobsForge.LOGGER.debug("Checking animation: {}", animationLocation);
            Minecraft.getInstance().getResourceManager().getResource(animationLocation).orElseThrow(() -> new IllegalArgumentException("Animation not found: " + animationLocation));
            CustomMobsForge.LOGGER.debug("Checking texture: {}", textureLocation);
            Minecraft.getInstance().getResourceManager().getResource(textureLocation).orElseThrow(() -> new IllegalArgumentException("Texture not found: " + textureLocation));
            CustomMobsForge.LOGGER.debug("All resources validated successfully on client");
            validationCache.put(cacheKey, true);
            return true;
        } catch (Exception e) {
            CustomMobsForge.LOGGER.error("Resource validation failed on client: {}", e.getMessage());
            validationCache.put(cacheKey, false);
            return false;
        }
    }

    public static void clearCache() {
        validationCache.clear();
        CustomMobsForge.LOGGER.debug("Validation cache cleared");
    }
}