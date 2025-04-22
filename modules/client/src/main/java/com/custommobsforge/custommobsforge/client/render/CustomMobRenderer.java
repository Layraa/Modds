package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CustomMobRenderer extends GeoEntityRenderer<CustomMob> {
    public CustomMobRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GeoModel<CustomMob>() {
            @Override
            public ResourceLocation getModelResource(CustomMob animatable) {
                return new ResourceLocation("custommobsforge", "geo/" + animatable.getModelName() + ".json");
            }

            @Override
            public ResourceLocation getTextureResource(CustomMob animatable) {
                return new ResourceLocation("custommobsforge", "textures/entity/" + animatable.getTextureName() + ".png");
            }

            @Override
            public ResourceLocation getAnimationResource(CustomMob animatable) {
                return new ResourceLocation("custommobsforge", "animations/" + animatable.getAnimationName() + ".json");
            }
        });
    }
}