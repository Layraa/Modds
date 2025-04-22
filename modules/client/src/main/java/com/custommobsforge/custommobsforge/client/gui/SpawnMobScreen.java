package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.client.clien.ClientNetworkHandler;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.network.SpawnMobPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SpawnMobScreen extends Screen {
    private final List<Preset> presets;

    public SpawnMobScreen(List<Preset> presets) {
        super(Component.literal("Spawn Mob"));
        this.presets = presets;
    }

    @Override
    protected void init() {
        int startY = this.height / 4;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = (this.width - buttonWidth) / 2;

        for (int i = 0; i < presets.size(); i++) {
            Preset preset = presets.get(i);
            this.addRenderableWidget(Button.builder(Component.literal("Spawn " + preset.name()), button -> {
                ClientNetworkHandler.sendToServer(new SpawnMobPacket(preset.name()));
                this.minecraft.setScreen(null);
            }).pos(centerX, startY + i * (buttonHeight + 5)).size(buttonWidth, buttonHeight).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> this.minecraft.setScreen(new PresetManagerScreen()))
                .pos(centerX, startY + presets.size() * (buttonHeight + 5)).size(buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}