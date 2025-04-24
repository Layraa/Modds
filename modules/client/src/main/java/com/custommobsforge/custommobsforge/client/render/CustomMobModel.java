package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.client.ClientPresetHandler;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class CustomMobModel extends GeoModel<CustomMob> {
    public static final ResourceLocation LAYER_LOCATION = new ResourceLocation("custommobsforge", "geo/custom_mob.geo.json");
    private static final ResourceLocation DEFAULT_MODEL = new ResourceLocation("custommobsforge", "geo/custom_mob.geo.json");
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("custommobsforge", "textures/entity/custom_mob.png");
    private static final ResourceLocation DEFAULT_ANIMATION = new ResourceLocation("custommobsforge", "animations/custom_mob.animation.json");

    @Override
    public ResourceLocation getModelResource(CustomMob animatable) {
        Preset preset = getPresetForMob(animatable);
        if (preset == null) {
            System.out.println("CustomMobModel: Using default model for CustomMob " + animatable.getId());
            return DEFAULT_MODEL;
        }
        String modelName = preset.getModel();
        if (!modelName.endsWith(".geo.json")) {
            modelName = modelName.endsWith(".geo") ? modelName + ".json" : modelName + ".geo.json";
        }
        return new ResourceLocation("custommobsforge", "geo/" + modelName);
    }

    @Override
    public ResourceLocation getTextureResource(CustomMob animatable) {
        Preset preset = getPresetForMob(animatable);
        if (preset == null) {
            System.out.println("CustomMobModel: Using default texture for CustomMob " + animatable.getId());
            return DEFAULT_TEXTURE;
        }
        String textureName = preset.getTexture();
        if (!textureName.endsWith(".png")) {
            textureName = textureName + ".png";
        }
        return new ResourceLocation("custommobsforge", "textures/entity/" + textureName);
    }

    @Override
    public ResourceLocation getAnimationResource(CustomMob animatable) {
        Preset preset = getPresetForMob(animatable);
        if (preset == null) {
            System.out.println("CustomMobModel: Using default animation for CustomMob " + animatable.getId());
            return DEFAULT_ANIMATION;
        }
        String animationName = preset.getAnimation();
        if (!animationName.endsWith(".animation.json")) {
            animationName = animationName.endsWith(".animation") ? animationName + ".json" : animationName + ".animation.json";
        }
        return new ResourceLocation("custommobsforge", "animations/" + animationName);
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

    private Preset getPresetForMob(CustomMob animatable) {
        Preset preset = animatable.getPreset();
        if (preset != null) {
            return preset;
        }
        String presetName = animatable.getPresetName();
        if (presetName.isEmpty()) {
            System.out.println("CustomMobModel: Preset name is empty for CustomMob " + animatable.getId());
            return null;
        }
        return ClientPresetHandler.getPresets().stream()
                .filter(p -> p.getName().equals(presetName))
                .findFirst()
                .orElse(null);
    }
}