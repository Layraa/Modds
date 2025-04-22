package com.custommobsforge.custommobsforge.common.network;

import com.custommobsforge.custommobsforge.common.event.OpenGuiEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class OpenGuiPacket {
    private static final Logger LOGGER = LogManager.getLogger();

    public OpenGuiPacket() {
    }

    public OpenGuiPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        LOGGER.info("Received OpenGuiPacket on client");
        context.get().enqueueWork(() -> {
            LOGGER.info("Posting OpenGuiEvent");
            OpenGuiEvent event = new OpenGuiEvent();
            boolean posted = MinecraftForge.EVENT_BUS.post(event);
            LOGGER.info("OpenGuiEvent posted: {}", posted);
        });
        context.get().setPacketHandled(true);
    }
}