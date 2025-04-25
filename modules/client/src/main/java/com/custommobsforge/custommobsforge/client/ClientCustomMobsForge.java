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
import com.custommobsforge.custommobsforge.client.render.ResourceValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ClientCustomMobsForge.MOD_ID)
@Mod.EventBusSubscriber(modid = ClientCustomMobsForge.MOD_ID, value = Dist.CLIENT)
public class ClientCustomMobsForge {
    public static final String MOD_ID = "custommobsforge_client";
    private static final Logger LOGGER = LogManager.getLogger("custommobsforge_client");
    private static final int FULL_UPDATE_INTERVAL = 6000; // 5 минут
    private static final int TIMEOUT_TICKS = 100; // 5 секунд (20 тиков = 1 секунда)
    private static int tickCounter = 0;
    private static boolean waitingForServerResponse = false;
    private static boolean serverHasMod = false;

    public ClientCustomMobsForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::clientSetup);
        CustomMobsForge.createEntityRegistry().register(modEventBus);
        CustomMobsForge.LOGGER.info("ClientCustomMobsForge initialized");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            SimpleChannel channel = CustomMobsForge.CHANNEL;
            int packetId = 0;

            channel.messageBuilder(PresetSavePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(PresetSavePacket::encode)
                    .decoder(PresetSavePacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            channel.messageBuilder(PresetDeletePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(PresetDeletePacket::encode)
                    .decoder(PresetDeletePacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            channel.messageBuilder(SpawnMobPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(SpawnMobPacket::encode)
                    .decoder(SpawnMobPacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            channel.messageBuilder(PresetPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(PresetPacket::encode)
                    .decoder(PresetPacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        LOGGER.info("Received PresetPacket with operation: {}, presets: {}, presetNames: {}",
                                msg.getOperation(), msg.getPresets().size(), msg.getPresetNames().size());
                        ClientPresetHandler.applyPresetUpdate(msg);
                        waitingForServerResponse = false; // Получили ответ от сервера
                        serverHasMod = true; // Серверный мод присутствует
                        ctx.get().setPacketHandled(true);
                    })
                    .add();

            channel.messageBuilder(RequestPresetsPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(RequestPresetsPacket::encode)
                    .decoder(RequestPresetsPacket::decode)
                    .consumerMainThread((msg, ctx) -> {
                        ctx.get().setPacketHandled(true);
                    })
                    .add();
        });

        CustomMobsForge.LOGGER.info("Client setup completed");
    }

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        // Сбрасываем состояние
        waitingForServerResponse = true;
        serverHasMod = false;
        tickCounter = 0;

        // Отправляем тестовый пакет для проверки наличия серверного мода
        CustomMobsForge.LOGGER.debug("Client logging in, sending test packet to check server mod");
        CustomMobsForge.CHANNEL.sendToServer(new RequestPresetsPacket());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().getConnection() != null) {
            // Проверяем, ждём ли ответа от сервера
            if (waitingForServerResponse) {
                tickCounter++;
                if (tickCounter >= TIMEOUT_TICKS) {
                    // Тайм-аут: ответа от сервера нет, считаем, что серверный мод отсутствует
                    LOGGER.warn("No response from server after {} ticks. Server does not have the required server mod (custommobsforge_server). Disconnecting client.", TIMEOUT_TICKS);
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.sendSystemMessage(Component.literal("This server does not have the required server mod (custommobsforge_server). Disconnecting..."));
                    }
                    Connection connection = Minecraft.getInstance().getConnection().getConnection();
                    if (connection != null) {
                        connection.disconnect(Component.literal("This server does not have the required server mod (custommobsforge_server)."));
                    }
                    waitingForServerResponse = false;
                    return;
                }
            }

            // Обычная логика обновления пресетов (если серверный мод присутствует)
            if (serverHasMod) {
                tickCounter++;
                if (tickCounter >= FULL_UPDATE_INTERVAL) {
                    CustomMobsForge.LOGGER.debug("Requesting full preset update from server");
                    CustomMobsForge.CHANNEL.sendToServer(new RequestPresetsPacket());
                    tickCounter = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ResourceValidator.clearCache();
        ClientPresetHandler.clear();
        waitingForServerResponse = false;
        serverHasMod = false;
        tickCounter = 0;
        CustomMobsForge.LOGGER.debug("Cleared ResourceValidator cache and ClientPresetHandler presets on client logout");
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
                Preset preset = ClientPresetHandler.getPresetByName(presetName);
                if (preset != null) {
                    customMob.setCustomPreset(preset);
                    CustomMobsForge.LOGGER.debug("Set preset " + presetName + " for CustomMob " + customMob.getId());
                } else {
                    CustomMobsForge.LOGGER.warn("Preset " + presetName + " not found for CustomMob " + customMob.getId());
                }
            } else {
                CustomMobsForge.LOGGER.warn("Preset name is empty for CustomMob " + customMob.getId());
            }
        }
    }
}