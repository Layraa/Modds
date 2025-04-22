package com.custommobsforge.custommobsforge;

import com.custommobsforge.custommobsforge.common.entity.ModEntities;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import net.minecraftforge.eventbus.api.IEventBus;

public class CustomMobsForge {
    public static final String MODID = "custommobsforge";

    public CustomMobsForge() {
        // Пустой конструктор, инициализация будет вызываться из client/server
    }

    public void register(IEventBus modEventBus) {
        ModEntities.ENTITIES.register(modEventBus);
        NetworkHandler.register();
    }
}