package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.preset.SpawnMobPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class ClientCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Команда для открытия GUI
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("custommobs")
                        .executes(context -> {
                            if (Minecraft.getInstance().getConnection() == null) {
                                context.getSource().sendFailure(Component.literal("This command can only be used on a server."));
                                return 0;
                            }
                            Minecraft.getInstance().setScreen(new PresetGui());
                            return 1;
                        })
        );

        // Команда для спавна мобов
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("spawnmob")
                        .then(Commands.argument("preset", MessageArgument.message())
                                .executes(context -> {
                                    if (Minecraft.getInstance().getConnection() == null) {
                                        context.getSource().sendFailure(Component.literal("This command can only be used on a server."));
                                        return 0;
                                    }
                                    String presetName = MessageArgument.getMessage(context, "preset").getString();
                                    try {
                                        if (Minecraft.getInstance().player != null) {
                                            Vec3 position = Minecraft.getInstance().player.position().add(0, 1, 0);
                                            CustomMobsForge.CHANNEL.sendToServer(new SpawnMobPacket(presetName, position));
                                            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Requested to spawn preset: " + presetName));
                                            return 1;
                                        } else {
                                            context.getSource().sendFailure(Component.literal("Player not found."));
                                            return 0;
                                        }
                                    } catch (Exception e) {
                                        CustomMobsForge.LOGGER.error("Failed to execute spawnmob command: " + e.getMessage(), e);
                                        context.getSource().sendFailure(Component.literal("Error spawning mob: " + e.getMessage()));
                                        return 0;
                                    }
                                }))
        );
    }
}