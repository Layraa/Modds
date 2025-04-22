package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.RequestPresetsPacket;
import com.custommobsforge.custommobsforge.common.network.SpawnMobPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MainMenuScreen extends Screen {
    private EditBox presetNameField;

    protected MainMenuScreen() {
        super(Component.literal("Custom Mobs Forge Menu"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int fieldWidth = 150;
        int fieldHeight = 20;
        int spacing = 30;

        this.presetNameField = new EditBox(this.font, centerX - fieldWidth / 2, startY, fieldWidth, fieldHeight, Component.literal("Preset Name"));
        this.presetNameField.setMaxLength(32);
        this.addRenderableWidget(this.presetNameField);

        this.addRenderableWidget(Button.builder(Component.literal("Spawn Mob"), button -> {
            String presetName = this.presetNameField.getValue();
            if (!presetName.isEmpty()) {
                NetworkHandler.sendToServer(new SpawnMobPacket(presetName));
            }
        }).bounds(centerX - fieldWidth / 2, startY + spacing, fieldWidth, fieldHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("Manage Presets"), button -> {
            NetworkHandler.sendToServer(new RequestPresetsPacket());
            Minecraft.getInstance().setScreen(new PresetManagerScreen());
        }).bounds(centerX - fieldWidth / 2, startY + spacing * 2, fieldWidth, fieldHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> {
            Minecraft.getInstance().setScreen(null);
        }).bounds(centerX - fieldWidth / 2, startY + spacing * 3, fieldWidth, fieldHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        int startY = this.height / 4;
        int spacing = 30;
        int labelOffsetX = -80;

        guiGraphics.drawString(this.font, "PRESET NAME:", centerX + labelOffsetX, startY + 5, 0xFFFFFF);
    }
}