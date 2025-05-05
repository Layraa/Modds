package com.custommobsforge.custommobsforge.common;

import com.custommobsforge.custommobsforge.common.registry.EntityRegistry;
import com.custommobsforge.custommobsforge.common.registry.ItemRegistry;
import com.custommobsforge.custommobsforge.common.registry.PacketRegistry;
import com.custommobsforge.custommobsforge.common.utils.CommonConstants;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Основной класс для модуля common CustomMobsForge.
 * Содержит общую логику, регистрацию сущностей и сетевых пакетов.
 */
@Mod(CommonConstants.MODID)
public class CustomMobsForgeCommon {
    // Логгер для мода
    public static final Logger LOGGER = LogManager.getLogger(CommonConstants.MODID);

    /**
     * Конструктор основного класса мода.
     * Регистрирует все необходимые события и компоненты.
     */
    public CustomMobsForgeCommon() {
        LOGGER.info("Initializing CustomMobsForge Common Module");

        // Получаем шину событий Forge
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрируем события жизненного цикла
        modEventBus.addListener(this::commonSetup);

        // Регистрируем сущности, предметы и пакеты
        EntityRegistry.init(modEventBus);
        ItemRegistry.init(modEventBus);

        // Регистрируем события в шине Forge
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Выполняется на стадии общей настройки (обе стороны: клиент и сервер).
     * Регистрирует сетевые пакеты и другую общую логику.
     *
     * @param event Событие общей настройки
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CustomMobsForge Common Setup");

        // Инициализируем сетевые пакеты
        event.enqueueWork(PacketRegistry::init);
    }
}