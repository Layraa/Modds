package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;

public class CustomMobModel extends GeoModel<CustomMob> {
    public static final ResourceLocation LAYER_LOCATION = new ResourceLocation("custommobsforge", "geo/custom_mob.geo.json");

    @Override
    public ResourceLocation getModelResource(CustomMob animatable) {
        String modelPath = animatable.getPreset().getModel();
        return new ResourceLocation("custommobsforge", "geo/" + modelPath);
    }

    @Override
    public ResourceLocation getTextureResource(CustomMob animatable) {
        return new ResourceLocation("custommobsforge", "textures/entity/" + animatable.getPreset().getTexture());
    }

    @Override
    public ResourceLocation getAnimationResource(CustomMob animatable) {
        String animationPath = animatable.getPreset().getAnimation();
        return new ResourceLocation("custommobsforge", "animations/" + animationPath);
    }

    @Override
    public void setCustomAnimations(CustomMob animatable, long instanceId, AnimationState<CustomMob> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData extraData = (EntityModelData) animationState.getExtraData().get(EntityModelData.class);
            if (extraData != null) {
                head.setRotX(extraData.headPitch() * ((float) Math.PI / 180F));
                head.setRotY(extraData.netHeadYaw() * ((float) Math.PI / 180F));
            }
        }
    }
}