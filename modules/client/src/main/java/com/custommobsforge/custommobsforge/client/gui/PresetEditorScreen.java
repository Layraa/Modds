package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.client.ClientNetworkHandler;
import com.custommobsforge.custommobsforge.common.network.ResourceListRequestPacket;
import com.custommobsforge.custommobsforge.common.network.ValidateResourcesPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class PresetEditorScreen extends Screen {
    private final boolean createMode;
    private EditBox nameField;
    private EditBox healthField;
    private EditBox speedField;
    private EditBox sizeWidthField;
    private EditBox sizeHeightField;
    private EditBox modelField;
    private EditBox textureField;
    private EditBox animationField;
    private List<String> modelOptions = new ArrayList<>();
    private List<String> textureOptions = new ArrayList<>();
    private List<String> animationOptions = new ArrayList<>();

    public PresetEditorScreen(boolean createMode) {
        super(Component.literal(createMode ? "Create Preset" : "Edit Preset"));
        this.createMode = createMode;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;

        this.nameField = new EditBox(this.font, centerX - 100, startY, 200, 20, Component.literal("Name"));
        this.nameField.setValue(createMode ? "" : PresetManagerScreen.selectedPresetName);
        this.addWidget(this.nameField);

        this.healthField = new EditBox(this.font, centerX - 100, startY + 30, 200, 20, Component.literal("Health"));
        this.healthField.setValue(createMode ? "20.0" : String.valueOf(PresetManagerScreen.getSelectedPreset().health()));
        this.addWidget(this.healthField);

        this.speedField = new EditBox(this.font, centerX - 100, startY + 60, 200, 20, Component.literal("Speed"));
        this.speedField.setValue(createMode ? "0.2" : String.valueOf(PresetManagerScreen.getSelectedPreset().speed()));
        this.addWidget(this.speedField);

        this.sizeWidthField = new EditBox(this.font, centerX - 100, startY + 90, 200, 20, Component.literal("Size Width"));
        this.sizeWidthField.setValue(createMode ? "0.6" : String.valueOf(PresetManagerScreen.getSelectedPreset().sizeWidth()));
        this.addWidget(this.sizeWidthField);

        this.sizeHeightField = new EditBox(this.font, centerX - 100, startY + 120, 200, 20, Component.literal("Size Height"));
        this.sizeHeightField.setValue(createMode ? "1.95" : String.valueOf(PresetManagerScreen.getSelectedPreset().sizeHeight()));
        this.addWidget(this.sizeHeightField);

        this.modelField = new EditBox(this.font, centerX - 100, startY + 150, 200, 20, Component.literal("Model"));
        this.modelField.setValue(createMode ? "" : PresetManagerScreen.getSelectedPreset().modelName());
        this.addWidget(this.modelField);

        this.textureField = new EditBox(this.font, centerX - 100, startY + 180, 200, 20, Component.literal("Texture"));
        this.textureField.setValue(createMode ? "" : PresetManagerScreen.getSelectedPreset().textureName());
        this.addWidget(this.textureField);

        this.animationField = new EditBox(this.font, centerX - 100, startY + 210, 200, 20, Component.literal("Animation"));
        this.animationField.setValue(createMode ? "" : PresetManagerScreen.getSelectedPreset().animationName());
        this.addWidget(this.animationField);

        this.modelField.setSuggestionProvider((field, text) -> modelOptions.stream()
                .filter(option -> option.toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList()));
        this.textureField.setSuggestionProvider((field, text) -> textureOptions.stream()
                .filter(option -> option.toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList()));
        this.animationField.setSuggestionProvider((field, text) -> animationOptions.stream()
                .filter(option -> option.toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList()));

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
            try {
                float health = Float.parseFloat(this.healthField.getValue());
                double speed = Double.parseDouble(this.speedField.getValue());
                float sizeWidth = Float.parseFloat(this.sizeWidthField.getValue());
                float sizeHeight = Float.parseFloat(this.sizeHeightField.getValue());
                ClientNetworkHandler.sendToServer(new ValidateResourcesPacket(
                        createMode,
                        this.nameField.getValue(),
                        health,
                        speed,
                        sizeWidth,
                        sizeHeight,
                        this.modelField.getValue(),
                        this.textureField.getValue(),
                        this.animationField.getValue()
                ));
            } catch (NumberFormatException e) {
                this.minecraft.setScreen(new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.literal("Invalid number format in health or speed")));
            }
        }).pos(centerX - 100, startY + 240).size(200, 20).build());

        ClientNetworkHandler.sendToServer(new ResourceListRequestPacket("model"));
        ClientNetworkHandler.sendToServer(new ResourceListRequestPacket("texture"));
        ClientNetworkHandler.sendToServer(new ResourceListRequestPacket("animation"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        guiGraphics.drawString(this.font, "Name:", this.width / 2 - 110, this.height / 4, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Health:", this.width / 2 - 110, this.height / 4 + 30, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Speed:", this.width / 2 - 110, this.height / 4 + 60, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Size Width:", this.width / 2 - 110, this.height / 4 + 90, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Size Height:", this.width / 2 - 110, this.height / 4 + 120, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Model:", this.width / 2 - 110, this.height / 4 + 150, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Texture:", this.width / 2 - 110, this.height / 4 + 180, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Animation:", this.width / 2 - 110, this.height / 4 + 210, 0xFFFFFF);

        this.nameField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.healthField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.speedField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.sizeWidthField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.sizeHeightField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.modelField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.textureField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.animationField.render(guiGraphics, mouseX, mouseY, partialTicks);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void handleResourceList(String type, List<String> resources) {
        switch (type) {
            case "model" -> {
                modelOptions.clear();
                modelOptions.addAll(resources);
            }
            case "texture" -> {
                textureOptions.clear();
                textureOptions.addAll(resources);
            }
            case "animation" -> {
                animationOptions.clear();
                animationOptions.addAll(resources);
            }
        }
    }

    public void handleResourceValidation(boolean valid, boolean createMode, String name, float health, double speed, float sizeWidth, float sizeHeight, String model, String texture, String animation) {
        if (valid) {
            this.minecraft.setScreen(new PresetManagerScreen());
        } else {
            this.minecraft.setScreen(new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.literal("Invalid resources. Please check the model, texture, and animation files.")));
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new PresetManagerScreen());
    }
}