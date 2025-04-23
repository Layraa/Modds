package com.custommobsforge.custommobsforge.server;

import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.server.PresetManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class CustomCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("spawncustommob")
                        .then(Commands.argument("preset_name", StringArgumentType.string())
                                .executes(context -> {
                                    String presetName = StringArgumentType.getString(context, "preset_name");
                                    CommandSourceStack source = context.getSource();
                                    Player player = source.getPlayerOrException();

                                    Preset preset = PresetManager.getPresets().stream()
                                            .filter(p -> p.getName().equals(presetName))
                                            .findFirst()
                                            .orElse(null);

                                    if (preset == null) {
                                        source.sendFailure(Component.literal("Preset not found: " + presetName));
                                        return 0;
                                    }

                                    ServerLevel level = source.getLevel();
                                    ServerMobHandler.spawnCustomMob(level, preset, player.getX(), player.getY(), player.getZ());
                                    source.sendSuccess(() -> Component.literal("Spawned custom mob: " + presetName), true);
                                    return 1;
                                }))
        );

        dispatcher.register(
                Commands.literal("listpresets")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            source.sendSuccess(() -> Component.literal("Available Presets:"), false);
                            for (Preset preset : PresetManager.getPresets()) {
                                source.sendSuccess(() -> Component.literal("- " + preset.getName()), false);
                            }
                            return 1;
                        })
        );
    }
}