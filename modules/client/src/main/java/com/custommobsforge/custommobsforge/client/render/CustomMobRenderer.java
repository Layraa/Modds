package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CustomMobRenderer extends GeoEntityRenderer<CustomMob> {
    public CustomMobRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomMobModel());
    }

    @Override
    public ResourceLocation getTextureLocation(CustomMob entity) {
        String texturePath = entity.getPreset().getTexture();
        return new ResourceLocation(CustomMobsForge.MOD_ID, "textures/entity/" + texturePath);
    }
}