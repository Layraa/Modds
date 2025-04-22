package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.network.SpawnMobPacket;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SpawnMobScreen extends Screen {
    private EditBox presetNameField;
    private PresetListWidget presetList;

    public SpawnMobScreen() {
        super(Component.literal("Spawn Mob"));
    }

    @Override
    protected void init() {
        super.init();
        this.presetNameField = new EditBox(font, width / 2 - 100, 50, 200, 20, Component.literal("Preset Name"));
        this.addRenderableWidget(presetNameField);

        this.presetList = new PresetListWidget(width / 2 - 100, 80, 200, 100, preset -> {
            presetNameField.setValue(preset.name());
        });
        this.addRenderableWidget(presetList);

        this.addRenderableWidget(new CustomButton(width / 2 - 100, 190, 200, 20, Component.literal("Spawn Mob"), button -> {
            String presetName = presetNameField.getValue();
            if (presetName.isEmpty()) {
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.sendSystemMessage(Component.literal("Preset name cannot be empty!"));
                }
                return;
            }
            NetworkHandler.sendToServer(new SpawnMobPacket(presetName));
        }));

        this.addRenderableWidget(new CustomButton(width / 2 - 100, 220, 200, 20, Component.literal("Back"), button -> {
            this.minecraft.setScreen(new MainMenuScreen());
        }));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, "Spawn Mob", width / 2, 20, 0xFFFFFF);

        guiGraphics.drawString(font, "Preset Name:", width / 2 - 100, 40, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}