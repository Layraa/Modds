package com.custommobsforge.custommobsforge.common;

import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.OpenGuiPacket;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CustomMobsForgeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("custommobsforge")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            NetworkHandler.sendToPlayer(new OpenGuiPacket(), player);
                            return 1;
                        })
        );
    }
}