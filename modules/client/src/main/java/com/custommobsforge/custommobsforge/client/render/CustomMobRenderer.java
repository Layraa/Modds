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
        super(renderManager, new CustomMobModel());
    }

    @Override
    public ResourceLocation getTextureLocation(CustomMob entity) {
        String textureName = entity.getTextureName();
        if (textureName == null || textureName.isEmpty()) {
            textureName = "test_texture";
        }
        return new ResourceLocation("custommobsforge", "textures/" + textureName + ".png");
    }

    @Override
    public void render(CustomMob entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
    }
}

class CustomMobModel extends GeoModel<CustomMob> {
    @Override
    public ResourceLocation getModelResource(CustomMob animatable) {
        String modelName = animatable.getModelName();
        if (modelName == null || modelName.isEmpty()) {
            modelName = "test_model";
        }
        return new ResourceLocation("custommobsforge", "geo/" + modelName + ".json");
    }

    @Override
    public ResourceLocation getTextureResource(CustomMob animatable) {
        String textureName = animatable.getTextureName();
        if (textureName == null || textureName.isEmpty()) {
            textureName = "test_texture";
        }
        return new ResourceLocation("custommobsforge", "textures/" + textureName + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(CustomMob animatable) {
        String animationName = animatable.getAnimationName();
        if (animationName == null || animationName.isEmpty()) {
            animationName = "test_animation";
        }
        return new ResourceLocation("custommobsforge", "animations/" + animationName + ".json");
    }
}