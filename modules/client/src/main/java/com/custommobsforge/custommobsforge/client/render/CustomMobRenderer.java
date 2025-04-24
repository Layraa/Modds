package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.client.ClientPresetHandler;
import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CustomMobRenderer extends GeoEntityRenderer<CustomMob> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(CustomMobsForge.MOD_ID, "textures/entity/custom_mob.png");

    public CustomMobRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomMobModel());
    }

    @Override
    public ResourceLocation getTextureLocation(CustomMob entity) {
        Preset preset = getPresetForMob(entity);
        if (preset == null) {
            System.out.println("CustomMobRenderer: Using default texture for CustomMob " + entity.getId());
            return DEFAULT_TEXTURE;
        }
        String textureName = preset.getTexture();
        if (!textureName.endsWith(".png")) {
            textureName = textureName + ".png";
        }
        return new ResourceLocation(CustomMobsForge.MOD_ID, "textures/entity/" + textureName);
    }

    private Preset getPresetForMob(CustomMob entity) {
        Preset preset = entity.getPreset();
        if (preset != null) {
            return preset;
        }
        String presetName = entity.getPresetName();
        if (presetName.isEmpty()) {
            System.out.println("CustomMobRenderer: Preset name is empty for CustomMob " + entity.getId());
            return null;
        }
        return ClientPresetHandler.getPresets().stream()
                .filter(p -> p.getName().equals(presetName))
                .findFirst()
                .orElse(null);
    }
}