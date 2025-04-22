package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.preset.CustomMobsForgeCommand;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetManager;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.PresetSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("custommobsforge_server")
@Mod.EventBusSubscriber(modid = "custommobsforge_server", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerCustomMobsForge {
    private final CustomMobsForge commonInitializer;

    public ServerCustomMobsForge() {
        commonInitializer = new CustomMobsForge();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        commonInitializer.register(modEventBus);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
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