package com.custommobsforge.custommobsforge;

import com.custommobsforge.custommobsforge.common.preset.CustomMobsForgeCommand;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetManager;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.PresetSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomMobsForge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerCustomMobsForge {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CustomMobsForgeCommand.register(event.getDispatcher());
    }

    public void onRequestPresets(ServerPlayer player) {
        for (Preset preset : PresetManager.getInstance().getPresets()) {
            NetworkHandler.sendToPlayer(new PresetSyncPacket(
                    preset.name(),
                    preset.health(),
                    preset.speed(),
                    preset.sizeWidth(),
                    preset.sizeHeight(),
                    preset.modelName(),
                    preset.textureName(),
                    preset.animationName()
            ), player);
        }
    }
}