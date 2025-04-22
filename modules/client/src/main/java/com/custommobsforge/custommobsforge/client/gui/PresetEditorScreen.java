package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PresetEditorScreen extends Screen {
    private final boolean createMode;
    private EditBox nameField;
    private EditBox healthField;
    private EditBox speedField;
    private EditBox modelField;
    private EditBox textureField;
    private EditBox animationField;
    private List<String> models = new ArrayList<>();
    private List<String> textures = new ArrayList<>();
    private List<String> animations = new ArrayList<>();

    public PresetEditorScreen(boolean createMode) {
        super(Component.literal(createMode ? "Create Preset" : "Edit Preset"));
        this.createMode = createMode;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new EditBox(this.font, centerX - 100, centerY - 60, 200, 20, Component.literal("Name"));
        this.nameField.setResponder(text -> validateInput());
        this.addRenderableWidget(this.nameField);

        this.healthField = new EditBox(this.font, centerX - 100, centerY - 30, 200, 20, Component.literal("Health"));
        this.healthField.setResponder(text -> validateInput());
        this.addRenderableWidget(this.healthField);

        this.speedField = new EditBox(this.font, centerX - 100, centerY, 200, 20, Component.literal("Speed"));
        this.speedField.setResponder(text -> validateInput());
        this.addRenderableWidget(this.speedField);

        this.modelField = new EditBox(this.font, centerX - 100, centerY + 30, 200, 20, Component.literal("Model"));
        this.modelField.setResponder(text -> validateInput());
        this.addRenderableWidget(this.modelField);

        this.textureField = new EditBox(this.font, centerX - 100, centerY + 60, 200, 20, Component.literal("Texture"));
        this.textureField.setResponder(text -> validateInput());
        this.addRenderableWidget(this.textureField);

        this.animationField = new EditBox(this.font, centerX - 100, centerY + 90, 200, 20, Component.literal("Animation"));
        this.animationField.setResponder(text -> validateInput());
        this.addRenderableWidget(this.animationField);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
            NetworkHandler.sendToServer(new ValidateResourcesPacket(
                    createMode,
                    nameField.getValue(),
                    parseFloat(healthField.getValue(), 20.0f),
                    parseDouble(speedField.getValue(), 0.5),
                    modelField.getValue(),
                    textureField.getValue(),
                    animationField.getValue()
            ));
        }).pos(centerX - 100, centerY + 120).size(98, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.minecraft.setScreen(new PresetManagerScreen()))
                .pos(centerX + 2, centerY + 120).size(98, 20).build());

        NetworkHandler.sendToServer(new ResourceListRequestPacket("model"));
        NetworkHandler.sendToServer(new ResourceListRequestPacket("texture"));
        NetworkHandler.sendToServer(new ResourceListRequestPacket("animation"));
    }

    private void validateInput() {
        boolean valid = !nameField.getValue().isEmpty() &&
                isValidFloat(healthField.getValue()) &&
                isValidDouble(speedField.getValue()) &&
                !modelField.getValue().isEmpty() &&
                !textureField.getValue().isEmpty() &&
                !animationField.getValue().isEmpty();
        this.children().stream()
                .filter(widget -> widget instanceof Button && ((Button) widget).getMessage().getString().equals("Save"))
                .map(widget -> (Button) widget)
                .findFirst()
                .ifPresent(button -> button.active = valid);
    }

    private boolean isValidFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private float parseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void handleResourceList(String type, List<String> resources) {
        switch (type) {
            case "model" -> models = resources;
            case "texture" -> textures = resources;
            case "animation" -> animations = resources;
        }
    }

    public void handleResourceValidation(boolean valid, boolean createMode, String name, float health, double speed, String model, String texture, String animation) {
        if (valid) {
            if (createMode) {
                NetworkHandler.sendToServer(new PresetCreatePacket(name, health, speed, model, texture, animation));
            } else {
                NetworkHandler.sendToServer(new PresetEditPacket(name, health, speed, model, texture, animation));
            }
            this.minecraft.setScreen(new PresetManagerScreen());
        } else {
            this.minecraft.setScreen(new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.literal("Invalid resources. Please check the model, texture, and animation files.")));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Name:", this.width / 2 - 150, this.height / 2 - 55, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Health:", this.width / 2 - 150, this.height / 2 - 25, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Speed:", this.width / 2 - 150, this.height / 2 + 5, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Model:", this.width / 2 - 150, this.height / 2 + 35, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Texture:", this.width / 2 - 150, this.height / 2 + 65, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Animation:", this.width / 2 - 150, this.height / 2 + 95, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}