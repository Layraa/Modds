package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.Preset;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.SpawnMobPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SpawnMobScreen extends Screen {
    private final Preset preset;

    public SpawnMobScreen(Preset preset) {
        super(Component.literal("Spawn Mob: " + preset.getName()));
        this.preset = preset;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Spawn"), button -> {
            NetworkHandler.sendToServer(new SpawnMobPacket(preset.getName()));
            this.minecraft.setScreen(null);
        }).pos(centerX - 50, centerY).size(100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.minecraft.setScreen(new PresetManagerScreen()))
                .pos(centerX - 50, centerY + 30).size(100, 20).build());
    }
}