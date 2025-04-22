package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.client.gui.PresetEditorScreen;
import com.custommobsforge.custommobsforge.client.gui.PresetManagerScreen;
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.network.OpenGuiPacket;
import com.custommobsforge.custommobsforge.common.network.PresetDeleteSyncPacket;
import com.custommobsforge.custommobsforge.common.network.PresetSyncPacket;
import com.custommobsforge.custommobsforge.common.network.ResourceListResponsePacket;
import com.custommobsforge.custommobsforge.common.network.ResourceValidationResponsePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleOpenGui(OpenGuiPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new PresetManagerScreen());
        });
        context.get().setPacketHandled(true);
    }

    public static void handlePresetSync(PresetSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
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
        context.get().setPacketHandled(true);
    }

    public static void handlePresetDeleteSync(PresetDeleteSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PresetManager.getInstance().removePreset(packet.getName());
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof PresetManagerScreen screen) {
                screen.setSelectedPreset(null);
                screen.getPresetList().refreshEntries();
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void handleResourceListResponse(ResourceListResponsePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof PresetEditorScreen editorScreen) {
                editorScreen.handleResourceList(packet.getType(), packet.getResources());
            } else if (minecraft.screen instanceof PresetManagerScreen managerScreen) {
                managerScreen.handleResourceList(packet.getType(), packet.getResources());
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void handleResourceValidationResponse(ResourceValidationResponsePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
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
        context.get().setPacketHandled(true);
    }
}