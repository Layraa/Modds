package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.client.gui.MainMenuScreen;
import com.custommobsforge.custommobsforge.client.gui.PresetEditorScreen;
import com.custommobsforge.custommobsforge.client.gui.PresetManagerScreen;
import com.custommobsforge.custommobsforge.client.render.CustomMobRenderer;
import com.custommobsforge.custommobsforge.common.ModEntities;
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.event.OpenGuiEvent;
import com.custommobsforge.custommobsforge.common.network.PresetSyncEvent;
import com.custommobsforge.custommobsforge.common.network.ResourceListResponseEvent;
import com.custommobsforge.custommobsforge.common.network.ResourceValidationResponseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ClientCustomMobsForge.MOD_ID)
public class ClientCustomMobsForge {
    public static final String MOD_ID = "custommobsforge_client";
    private static final Logger LOGGER = LogManager.getLogger();

    public ClientCustomMobsForge() {
        LOGGER.info("ClientCustomMobsForge: Initializing");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::clientSetup);

        IEventBus forgeBus = net.minecraftforge.common.MinecraftForge.EVENT_BUS;
        forgeBus.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("ClientCustomMobsForge: Client setup");
        NetworkHandler.register();

        event.enqueueWork(() -> {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener(this::registerRenderers);
        });
    }

    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LOGGER.info("ClientCustomMobsForge: Registering entity renderers");
        event.registerEntityRenderer(ModEntities.CUSTOM_MOB.get(), CustomMobRenderer::new);
    }

    @SubscribeEvent
    public void onPresetSync(PresetSyncEvent event) {
        if (!Minecraft.getInstance().level.isClientSide()) return;
        var packet = event.getPacket();
        LOGGER.info("ClientCustomMobsForge: Received PresetSyncEvent for preset: {}", packet.getName());
        PresetManager.getInstance().addPreset(
                packet.getName(),
                packet.getHealth(),
                packet.getSpeed(),
                packet.getModelName(),
                packet.getTextureName(),
                packet.getAnimationName()
        );
    }

    @SubscribeEvent
    public void onOpenGui(OpenGuiEvent event) {
        if (!Minecraft.getInstance().level.isClientSide()) return;
        Minecraft.getInstance().execute(() -> {
            LOGGER.info("Received OpenGuiEvent, opening MainMenuScreen");
            Minecraft.getInstance().setScreen(new MainMenuScreen());
        });
    }

    @SubscribeEvent
    public void onResourceListResponse(ResourceListResponseEvent event) {
        if (!Minecraft.getInstance().level.isClientSide()) return;
        var packet = event.getPacket();
        Minecraft.getInstance().execute(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof PresetEditorScreen || screen instanceof PresetManagerScreen) {
                // Убрали setSuggestions, так как он не поддерживается
            }
        });
    }

    @SubscribeEvent
    public void onResourceValidationResponse(ResourceValidationResponseEvent event) {
        if (!Minecraft.getInstance().level.isClientSide()) return;
        var packet = event.getPacket();
        Minecraft.getInstance().execute(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof PresetEditorScreen editorScreen) {
                editorScreen.handleResourceValidation(
                        packet.isValid(),
                        packet.isCreateMode(),
                        packet.getName(),
                        packet.getHealth(),
                        packet.getSpeed(),
                        packet.getModel(),
                        packet.getTexture(),
                        packet.getAnimation()
                );
            } else if (screen instanceof PresetManagerScreen managerScreen) {
                managerScreen.handleResourceValidation(
                        packet.isValid(),
                        packet.isCreateMode(),
                        packet.getName(),
                        packet.getHealth(),
                        packet.getSpeed(),
                        packet.getModel(),
                        packet.getTexture(),
                        packet.getAnimation()
                );
            }
        });
    }
}