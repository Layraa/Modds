package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.client.clien.ClientNetworkHandler;
import com.custommobsforge.custommobsforge.client.gui.PresetEditorScreen;
import com.custommobsforge.custommobsforge.client.gui.PresetManagerScreen;
import com.custommobsforge.custommobsforge.client.render.CustomMobRenderer;
import com.custommobsforge.custommobsforge.common.entity.ModEntities;
import com.custommobsforge.custommobsforge.common.preset.PresetManager;
import com.custommobsforge.custommobsforge.common.network.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkEvent;

@Mod.EventBusSubscriber(modid = "custommobsforge", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientCustomMobsForge {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientNetworkHandler.register();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CUSTOM_MOB.get(), CustomMobRenderer::new);
    }

    public static void onOpenGui(OpenGuiPacket packet, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new PresetManagerScreen());
        });
        context.setPacketHandled(true);
    }

    public static void onPresetSync(PresetSyncPacket packet, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            PresetManager.getInstance().addPreset(
                    packet.getName(),
                    packet.getHealth(),
                    packet.getSpeed(),
                    packet.getSizeWidth(),
                    packet.getSizeHeight(),
                    packet.getModelName(),
                    packet.getTextureName(),
                    packet.getAnimationName()
            );
        });
        context.setPacketHandled(true);
    }

    public static void onPresetDeleteSync(PresetDeleteSyncPacket packet, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            PresetManager.getInstance().removePreset(packet.getName());
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof PresetManagerScreen screen) {
                screen.setSelectedPreset(null);
                screen.getPresetList().refreshEntries();
            }
        });
        context.setPacketHandled(true);
    }

    public static void onResourceListResponse(ResourceListResponsePacket packet, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof PresetEditorScreen editorScreen) {
                editorScreen.handleResourceList(packet.getType(), packet.getResources());
            } else if (minecraft.screen instanceof PresetManagerScreen managerScreen) {
                managerScreen.handleResourceList(packet.getType(), packet.getResources());
            }
        });
        context.setPacketHandled(true);
    }

    public static void onResourceValidationResponse(ResourceValidationResponsePacket packet, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof PresetEditorScreen screen) {
                screen.handleResourceValidation(
                        packet.isValid(),
                        packet.isCreateMode(),
                        packet.getName(),
                        packet.getHealth(),
                        packet.getSpeed(),
                        packet.getSizeWidth(),
                        packet.getSizeHeight(),
                        packet.getModel(),
                        packet.getTexture(),
                        packet.getAnimation()
                );
            } else if (minecraft.screen instanceof PresetManagerScreen screen) {
                screen.handleResourceValidation(
                        packet.isValid(),
                        packet.isCreateMode(),
                        packet.getName(),
                        packet.getHealth(),
                        packet.getSpeed(),
                        packet.getSizeWidth(),
                        packet.getSizeHeight(),
                        packet.getModel(),
                        packet.getTexture(),
                        packet.getAnimation()
                );
            }
        });
        context.setPacketHandled(true);
    }

    public static boolean isClientSide() {
        return Minecraft.getInstance().isLocalServer() || Minecraft.getInstance().getSingleplayerServer() != null;
    }
}