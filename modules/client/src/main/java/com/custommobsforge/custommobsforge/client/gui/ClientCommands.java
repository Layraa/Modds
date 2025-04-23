package com.custommobsforge.custommobsforge.client.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ClientCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("custommobs")
                        .executes(context -> {
                            // Открываем GUI
                            Minecraft.getInstance().setScreen(new PresetGui());
                            return 1;
                        })
        );
    }
}