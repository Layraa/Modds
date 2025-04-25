package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.client.ClientCustomMobsForge;
import com.custommobsforge.custommobsforge.client.ClientPresetHandler;
import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CustomMobRenderer extends GeoEntityRenderer<CustomMob> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(ClientCustomMobsForge.MOD_ID, "textures/entity/custom_mob.png");

    public CustomMobRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomMobModel());
    }

    @Override
    public ResourceLocation getTextureLocation(CustomMob entity) {
        Preset preset = getPresetForMob(entity);
        if (preset == null) {
            CustomMobsForge.LOGGER.warn("CustomMobRenderer: Using default texture for CustomMob " + entity.getId());
            return DEFAULT_TEXTURE;
        }
        String textureName = preset.getTexture();
        if (!textureName.endsWith(".png")) {
            textureName = textureName + ".png";
        }
        ResourceLocation textureLocation = new ResourceLocation(ClientCustomMobsForge.MOD_ID, "textures/entity/" + textureName);
        CustomMobsForge.LOGGER.debug("Trying to load texture: {}", textureLocation);
        return textureLocation;
    }

    private Preset getPresetForMob(CustomMob entity) {
        Preset preset = entity.getPreset();
        if (preset != null) {
            return preset;
        }
        String presetName = entity.getPresetName();
        if (presetName.isEmpty()) {
            CustomMobsForge.LOGGER.warn("CustomMobModel: Preset name is empty for CustomMob " + entity.getId());
            return null;
        }
        preset = ClientPresetHandler.getPresetByName(presetName);
        if (preset != null) {
            entity.setCustomPreset(preset);
        }
        return preset;
    }
}