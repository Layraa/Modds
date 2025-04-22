package com.custommobsforge.custommobsforge;

import com.custommobsforge.custommobsforge.common.ModEntities;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.OpenGuiPacket;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomMobsForge.MOD_ID)
public class CustomMobsForge {
    public static final String MOD_ID = "custommobsforge";
    private static final Logger LOGGER = LogManager.getLogger();

    public CustomMobsForge() {
        LOGGER.info("CustomMobsForge: Initializing");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // Регистрируем сущности
        ModEntities.register(modEventBus);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CustomMobsForge: Common setup");
        NetworkHandler.register();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEventHandler {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            LOGGER.info("CustomMobsForge: Registering commands");
            event.getDispatcher().register(
                    Commands.literal("custommobsforge")
                            .executes(context -> {
                                NetworkHandler.sendToPlayer(new OpenGuiPacket(), context.getSource().getPlayerOrException());
                                return 1;
                            })
            );
        }
    }
}