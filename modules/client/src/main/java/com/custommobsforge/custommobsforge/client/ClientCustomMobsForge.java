package com.custommobsforge.custommobsforge.client;

import com.custommobsforge.custommobsforge.client.gui.ClientCommands;
import com.custommobsforge.custommobsforge.client.gui.PresetGui;
import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.entity.CustomMob;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetPacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.RequestPresetsPacket;
import com.custommobsforge.custommobsforge.common.preset.SpawnMobPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value = ClientCustomMobsForge.MOD_ID)
@Mod.EventBusSubscriber(modid = ClientCustomMobsForge.MOD_ID, value = Dist.CLIENT)
public class ClientCustomMobsForge {
    public static final String MOD_ID = "custommobsforge";

    public ClientCustomMobsForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::clientSetup);
        CustomMobsForge.createEntityRegistry().register(modEventBus);
        CustomMobsForge.LOGGER.info("ClientCustomMobsForge initialized");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        int packetId = 0;
        CustomMobsForge.CHANNEL.messageBuilder(PresetPacket.class, packetId++)
                .encoder(PresetPacket::encode)
                .decoder(PresetPacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ClientPresetHandler.setPresets(msg.getPresets());
                        CustomMobsForge.LOGGER.info("Received " + msg.getPresets().size() + " presets from server");
                    });
                    ctx.get().setPacketHandled(true);
                })
                .add();
        CustomMobsForge.CHANNEL.messageBuilder(PresetSavePacket.class, packetId++)
                .encoder(PresetSavePacket::encode)
                .decoder(PresetSavePacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();
        CustomMobsForge.CHANNEL.messageBuilder(PresetDeletePacket.class, packetId++)
                .encoder(PresetDeletePacket::encode)
                .decoder(PresetDeletePacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();
        CustomMobsForge.CHANNEL.messageBuilder(SpawnMobPacket.class, packetId++)
                .encoder(SpawnMobPacket::encode)
                .decoder(SpawnMobPacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();
        CustomMobsForge.CHANNEL.messageBuilder(RequestPresetsPacket.class, packetId++)
                .encoder(RequestPresetsPacket::encode)
                .decoder(RequestPresetsPacket::decode)
                .consumerMainThread((msg, ctx) -> {
                    ctx.get().setPacketHandled(true);
                })
                .add();

        CustomMobsForge.LOGGER.info("Client setup completed");
    }

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        CustomMobsForge.LOGGER.info("Client logging in, requesting presets from server");
        CustomMobsForge.CHANNEL.sendToServer(new RequestPresetsPacket());
    }

    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        ClientCommands.register(event.getDispatcher());
        CustomMobsForge.LOGGER.info("Client commands registered");
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof CustomMob customMob) {
            String presetName = customMob.getPresetName();
            if (!presetName.isEmpty()) {
                Preset preset = ClientPresetHandler.getPresets().stream()
                        .filter(p -> p.getName().equals(presetName))
                        .findFirst()
                        .orElse(null);
                if (preset != null) {
                    customMob.setCustomPreset(preset);
                    CustomMobsForge.LOGGER.info("Set preset " + presetName + " for CustomMob " + customMob.getId());
                } else {
                    CustomMobsForge.LOGGER.warn("Preset " + presetName + " not found for CustomMob " + customMob.getId());
                    // Повторно запрашиваем пресеты
                    CustomMobsForge.CHANNEL.sendToServer(new RequestPresetsPacket());
                }
            } else {
                CustomMobsForge.LOGGER.warn("Preset name is empty for CustomMob " + customMob.getId());
            }
        }
    }
}