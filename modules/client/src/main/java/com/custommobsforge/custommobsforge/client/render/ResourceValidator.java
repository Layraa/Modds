package com.custommobsforge.custommobsforge.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ResourceValidator {
    public static boolean validateResources(ResourceLocation model, ResourceLocation animation, ResourceLocation texture) {
        // Проверка существования ресурсов
        try {
            Minecraft.getInstance().getResourceManager().getResource(model).orElseThrow();
            Minecraft.getInstance().getResourceManager().getResource(animation).orElseThrow();
            Minecraft.getInstance().getResourceManager().getResource(texture).orElseThrow();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}