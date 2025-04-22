package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.PresetCreatePacket;
import com.custommobsforge.custommobsforge.common.network.PresetEditPacket;
import com.custommobsforge.custommobsforge.common.network.ValidateResourcesPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PresetEditorScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    public EditBox nameField; // Сделали public
    public ForgeSlider healthSlider; // Сделали public
    public ForgeSlider speedSlider; // Сделали public
    public EditBox modelField; // Сделали public
    public EditBox textureField; // Сделали public
    public EditBox animationField; // Сделали public

    public PresetEditorScreen() {
        super(Component.literal("Edit Preset"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int fieldWidth = 150;
        int fieldHeight = 20;
        int spacing = 30;

        this.nameField = new EditBox(this.font, centerX - fieldWidth / 2, startY, fieldWidth, fieldHeight, Component.literal("Preset Name"));
        this.nameField.setMaxLength(32);
        this.addRenderableWidget(this.nameField);

        this.healthSlider = new ForgeSlider(centerX - fieldWidth / 2, startY + spacing, fieldWidth, fieldHeight, Component.literal(""), Component.literal(""), 1.0, 100.0, 20.0, 1.0, 0, true);
        this.addRenderableWidget(this.healthSlider);

        this.speedSlider = new ForgeSlider(centerX - fieldWidth / 2, startY + spacing * 2, fieldWidth, fieldHeight, Component.literal(""), Component.literal(""), 0.1, 2.0, 0.5, 0.1, 1, true);
        this.addRenderableWidget(this.speedSlider);

        this.modelField = new EditBox(this.font, centerX - fieldWidth / 2, startY + spacing * 3, fieldWidth, fieldHeight, Component.literal("Model"));
        this.modelField.setMaxLength(32);
        this.addRenderableWidget(this.modelField);

        this.textureField = new EditBox(this.font, centerX - fieldWidth / 2, startY + spacing * 4, fieldWidth, fieldHeight, Component.literal("Texture"));
        this.textureField.setMaxLength(32);
        this.addRenderableWidget(this.textureField);

        this.animationField = new EditBox(this.font, centerX - fieldWidth / 2, startY + spacing * 5, fieldWidth, fieldHeight, Component.literal("Animation"));
        this.animationField.setMaxLength(32);
        this.addRenderableWidget(this.animationField);

        this.addRenderableWidget(Button.builder(Component.literal("Create"), button -> {
            validateAndCreatePreset(true);
        }).bounds(centerX - fieldWidth / 2, startY + spacing * 6, fieldWidth, fieldHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            Minecraft.getInstance().setScreen(new PresetManagerScreen());
        }).bounds(centerX - fieldWidth / 2, startY + spacing * 7, fieldWidth, fieldHeight).build());
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
        guiGraphics.drawString(this.font, "HEALTH:", centerX + labelOffsetX, startY + spacing + 5, 0xFFFFFF);
        guiGraphics.drawString(this.font, "SPEED:", centerX + labelOffsetX, startY + spacing * 2 + 5, 0xFFFFFF);
        guiGraphics.drawString(this.font, "MODEL:", centerX + labelOffsetX, startY + spacing * 3 + 5, 0xFFFFFF);
        guiGraphics.drawString(this.font, "TEXTURE:", centerX + labelOffsetX, startY + spacing * 4 + 5, 0xFFFFFF);
        guiGraphics.drawString(this.font, "ANIMATION:", centerX + labelOffsetX, startY + spacing * 5 + 5, 0xFFFFFF);
    }

    private void validateAndCreatePreset(boolean isCreateMode) {
        String name = this.nameField.getValue();
        float health = (float) this.healthSlider.getValue();
        double speed = this.speedSlider.getValue();
        String model = this.modelField.getValue();
        String texture = this.textureField.getValue();
        String animation = this.animationField.getValue();

        NetworkHandler.sendToServer(new ValidateResourcesPacket(model, texture, animation, isCreateMode, name, health, speed));
    }

    public void handleResourceValidation(boolean valid, boolean isCreateMode, String name, float health, double speed, String model, String texture, String animation) {
        if (valid) {
            if (isCreateMode) {
                NetworkHandler.sendToServer(new PresetCreatePacket(name, health, speed, model, texture, animation));
            } else {
                NetworkHandler.sendToServer(new PresetEditPacket(name, health, speed, model, texture, animation));
            }
            Minecraft.getInstance().setScreen(new PresetManagerScreen());
        } else {
            LOGGER.warn("Resource validation failed for model: {}, texture: {}, animation: {}", model, texture, animation);
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal(String.format("Invalid resources! Model: %s, Texture: %s, Animation: %s not found.", model, texture, animation)),
                    false
            );
        }
    }
}