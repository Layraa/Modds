package com.custommobsforge.custommobsforge.client.render;

import com.custommobsforge.custommobsforge.client.entity.ClientCustomMob;
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.RequestPresetsPacket;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomMobRenderer extends GeoEntityRenderer<ClientCustomMob> {
    private static final Logger LOGGER = LogManager.getLogger();

    public CustomMobRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomMobModel());
    }

    @Override
    public ResourceLocation getTextureLocation(ClientCustomMob entity) {
        return getResource(entity, "textures/entity/", "custom_mob.png", PresetManager.Preset::textureName);
    }

    private ResourceLocation getResource(ClientCustomMob animatable, String pathPrefix, String defaultResource, java.util.function.Function<PresetManager.Preset, String> nameExtractor) {
        String presetName = animatable.getPresetName();
        PresetManager.Preset preset = PresetManager.getInstance().getPreset(presetName);
        if (preset == null) {
            LOGGER.warn("Preset not found for entity with presetName: {}. Requesting presets from server.", presetName);
            NetworkHandler.sendToServer(new RequestPresetsPacket());
            return ResourceLocation.fromNamespaceAndPath("custommobsforge", pathPrefix + defaultResource);
        }
        String resourceName = nameExtractor.apply(preset);
        if (resourceName != null && !resourceName.isEmpty()) {
            ResourceLocation resource = ResourceLocation.fromNamespaceAndPath("custommobsforge", pathPrefix + resourceName + (pathPrefix.startsWith("textures") ? ".png" : pathPrefix.startsWith("geo") ? ".geo.json" : ".animation.json"));
            LOGGER.debug("Loading resource for preset {}: {}", presetName, resource);
            return resource;
        }
        LOGGER.debug("Using default resource for preset {}: {}", presetName, defaultResource);
        return ResourceLocation.fromNamespaceAndPath("custommobsforge", pathPrefix + defaultResource);
    }
}

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
class CustomMobModel extends GeoModel<ClientCustomMob> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public ResourceLocation getModelResource(ClientCustomMob animatable) {
        return getResource(animatable, "geo/", "custom_mob.geo.json", PresetManager.Preset::modelName);
    }

    @Override
    public ResourceLocation getTextureResource(ClientCustomMob animatable) {
        return getResource(animatable, "textures/entity/", "custom_mob.png", PresetManager.Preset::textureName);
    }

    @Override
    public ResourceLocation getAnimationResource(ClientCustomMob animatable) {
        return getResource(animatable, "animations/", "custom_mob.animation.json", PresetManager.Preset::animationName);
    }

    private ResourceLocation getResource(ClientCustomMob animatable, String pathPrefix, String defaultResource, java.util.function.Function<PresetManager.Preset, String> nameExtractor) {
        String presetName = animatable.getPresetName();
        PresetManager.Preset preset = PresetManager.getInstance().getPreset(presetName);
        if (preset == null) {
            LOGGER.warn("Preset not found for entity with presetName: {}. Requesting presets from server.", presetName);
            NetworkHandler.sendToServer(new RequestPresetsPacket());
            return ResourceLocation.fromNamespaceAndPath("custommobsforge", pathPrefix + defaultResource);
        }
        String resourceName = nameExtractor.apply(preset);
        if (resourceName != null && !resourceName.isEmpty()) {
            ResourceLocation resource = ResourceLocation.fromNamespaceAndPath("custommobsforge", pathPrefix + resourceName + (pathPrefix.startsWith("textures") ? ".png" : pathPrefix.startsWith("geo") ? ".geo.json" : ".animation.json"));
            LOGGER.debug("Loading resource for preset {}: {}", presetName, resource);
            return resource;
        }
        LOGGER.debug("Using default resource for preset {}: {}", presetName, defaultResource);
        return ResourceLocation.fromNamespaceAndPath("custommobsforge", pathPrefix + defaultResource);
    }
}